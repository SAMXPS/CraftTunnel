package me.samxps.crafttunnel.netty.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

	private final Channel clientChannel;
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {		
		// Forwards the received message to client
		clientChannel.write(msg);
		clientChannel.flush();
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// Closes the connection with the client
		clientChannel.close();

		super.channelInactive(ctx); // TODO: verify if this super call is necessary
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}