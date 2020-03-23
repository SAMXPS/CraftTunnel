package me.samxps.crafttunnel.netty.proxy;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelLinker extends ChannelInboundHandlerAdapter {

	private final ChannelFuture linkedFuture;
	private Channel linkedChannel;
	private Queue<Object> queue;
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		linkedChannel = linkedFuture.channel();
		if (!linkedFuture.channel().isActive()) {
			queue = new LinkedBlockingQueue<Object>();
			
			linkedFuture.addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!future.isSuccess()) {
						// Closes channel if the linked connection failed
						ctx.channel().close();
					} else {
						linkedChannel.eventLoop().execute(new Runnable() {
							
							@Override
							public void run() {	
								// Once the connection is made, send the queued messages.
								while (!queue.isEmpty()) {
									linkedChannel.write(queue.poll());
									linkedChannel.flush();
								}
							}
						});

					}
				}
			});
		}
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {		
		// Forwards the received message
		if (linkedChannel.isActive()) {
			linkedChannel.write(msg);
			linkedChannel.flush();
		} else {
			// If the channel is not active, add message to the sending queue
			queue.add(msg);
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// Closes the connection
		linkedChannel.close();
		super.channelInactive(ctx); 
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}
