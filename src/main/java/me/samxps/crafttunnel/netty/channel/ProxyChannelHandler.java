package me.samxps.crafttunnel.netty.channel;

import java.util.HashSet;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.samxps.crafttunnel.ServerType;
import me.samxps.crafttunnel.netty.connector.ServerConnector;

/**
 * {@link ProxyChannelHandler} will be responsible for sending and receiving
 * data from remote {@link ServerType#EXIT_POINT}.
 * */
public class ProxyChannelHandler extends ChannelInboundHandlerAdapter {

	private static HashSet<ProxyChannelHandler> instances = new HashSet<ProxyChannelHandler>();
	private Channel proxyChannel;
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		instances.add(this);
		this.proxyChannel = ctx.channel();
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		instances.remove(this);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		instances.remove(this);
		super.channelInactive(ctx);
	}
	
	/**
	 * This will return a new virtual server connector
	 * to send data trough the multiproxy tunnel
	 * */
	public static ServerConnector generateConnector() {
		return null;
	}
	
}
