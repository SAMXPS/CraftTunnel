package me.samxps.crafttunnel.netty.channel;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.netty.connector.ServerConnector;
import me.samxps.crafttunnel.netty.multi.ProxyEntryPointHandler;

/**
 * This will handle incoming packets from the connecting player and forward them
 * to the server linker.
 * */
@RequiredArgsConstructor
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

	private final ServerConnector remote;
	private Channel serverChannel;
	private Queue<Object> queue = new LinkedBlockingQueue<Object>();
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		
		CraftTunnel.getLogger().log(Level.INFO, "[{0}] new connection from {1}", new Object[] {
				"ClientChannelHandler", ProxyEntryPointHandler.getClientAddress(ctx.channel()).toString()
		});
		
		// Initiates the connection to the remote server
		ChannelFuture f = remote.init(ctx.channel())
				.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					// Closes client connection if the server connection failed
					ctx.channel().close();
				} else {
					if (serverChannel == null) serverChannel = future.channel();
					serverChannel.eventLoop().execute(new Runnable() {
						
						@Override
						public void run() {	
							// Once the connection to the server is made,
							// send the queued messages.
							while (!queue.isEmpty()) {
								serverChannel.write(queue.poll());
								serverChannel.flush();
							}
						}
					});

				}
			}
		}); 
		
		serverChannel = f.channel();
		
		super.channelActive(ctx);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// Closes the connection with the server
		remote.exit();
		
		super.channelInactive(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// channelRead is called every time a message is received
		
		if (serverChannel.isActive()) {
			serverChannel.write(msg);
			serverChannel.flush();
		} else {
			// If the connection to the server is still pending, queue the messages up 
			// for sending as soon as possible
			queue.add(msg);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
