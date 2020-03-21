package me.samxps.crafttunnel.netty.multi;

import java.util.HashSet;
import java.util.logging.Level;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.ServerType;
import me.samxps.crafttunnel.netty.connector.ServerConnector;

/**
 * {@link ProxyEntryPointHandler} will be responsible for sending and receiving
 * data from remote {@link ServerType#EXIT_POINT}.
 * */
public class ProxyEntryPointHandler extends ChannelInboundHandlerAdapter {

	private static HashSet<ProxyEntryPointHandler> instances = new HashSet<ProxyEntryPointHandler>();
	private Channel proxyChannel;
	
	private void info(String msg, Object data) {
		CraftTunnel.getLogger().log(Level.INFO, "[ProxyHandler] " + msg, data);
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		instances.add(this);
		this.proxyChannel = ctx.channel();
		info("Remote proxy exit point {0} connected.", ctx.channel().remoteAddress().toString());
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
