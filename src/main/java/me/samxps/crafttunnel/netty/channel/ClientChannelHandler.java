package me.samxps.crafttunnel.netty.channel;

import java.util.logging.Level;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.netty.connector.ServerConnector;

/**
 * This will handle incoming packets from the connecting player and forward them
 * to the server linker.
 * */
@RequiredArgsConstructor
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

	private final ServerConnector remote;
	private Channel serverChannel;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// channelActive is called when the connection is made and ready to generate traffic.
		
		CraftTunnel.getLogger().log(Level.INFO, "[{0}] new connection from {1}", new Object[] {
				"ClientChannelHandler", ctx.channel().remoteAddress().toString()
		});
		
		// Initiates the connection to the remote server and waits
		ChannelFuture f = remote.init(ctx.channel()).sync(); 
		
		if (f.isSuccess()) {
			serverChannel = f.channel();
			
			// TODO: Initial handshake and sending IP information of the player
			// NOTE: This maybe will need to be wrapped inside Minecraft protocol packets
			// Suggestion: use HAProxy protocol instead? http://www.haproxy.org/download/1.8/doc/proxy-protocol.txt
		} else {
			// Closes client connection if the server connection failed
			ctx.channel().close();
		}
		
		super.channelActive(ctx); // TODO: verify if this super call is necessary
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// Closes the connection with the server
		remote.exit();
		
		super.channelInactive(ctx); // TODO: verify if this super call is necessary
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// channelRead is called every time a message is received
		
		serverChannel.write(msg);
		serverChannel.flush();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
