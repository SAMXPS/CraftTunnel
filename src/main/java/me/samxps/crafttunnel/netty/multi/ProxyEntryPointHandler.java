package me.samxps.crafttunnel.netty.multi;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.ServerType;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket.WrapperPacketType;

/**
 * {@link ProxyEntryPointHandler} will be responsible for sending and receiving
 * data from remote {@link ServerType#EXIT_POINT}.
 * */
public class ProxyEntryPointHandler extends ChannelInboundHandlerAdapter {

	private static HashSet<ProxyEntryPointHandler> instances = new HashSet<ProxyEntryPointHandler>();
	private HashMap<InetSocketAddress, Channel> clients = new HashMap<InetSocketAddress, Channel>();
	
	private Channel proxyChannel;
	private EventLoopGroup eventLoop;
	
	private static void info(String msg, Object data) {
		CraftTunnel.getLogger().log(Level.INFO, "[ProxyHandler] " + msg, data);
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().deregister().addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				eventLoop = new NioEventLoopGroup();
				eventLoop.register(future.channel()).sync();
			}
		});

		instances.add(this);
		this.proxyChannel = ctx.channel();
		info("Remote proxy exit point {0} connected.", ctx.channel().remoteAddress().toString());
		info("Current handler count: {0}", instances.size());
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
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof MinecraftPacket) {
			MinecraftPacket p = (MinecraftPacket) msg;

			if (p.getPacketID() == WrapperPacket.getPacketID()) {
				handlePacket(WrapperPacket.decode(p));
			}
		}
	}
	
	private void handlePacket(WrapperPacket w) {
		Channel clientChannel = clients.get(w.getClientAddress());
		
		if (clientChannel == null)
			 return;
		
		if (w.getType() == WrapperPacketType.CONNECTION_CLOSE) {
			clientChannel.close();
		} else if (w.getType() == WrapperPacketType.DATA_BYTES) {
			clientChannel.write(w.getData());
			clientChannel.flush();
		} else if (w.getType() == WrapperPacketType.DATA_PACKET) {
			clientChannel.write(w.getData());
			clientChannel.flush();
		}
	}
	
	public int getScore() {
		return clients.size();
	}
	
	
	public static boolean handleClientChannel(Channel clientChannel) {
		ProxyEntryPointHandler instance = loadBalance();
		if (instance == null)
			return false;
		
		InetSocketAddress clientAddress = getClientAddress(clientChannel);
		
		clientChannel.pipeline().addAfter("decoder", "wrapper", new WrapperInboundHandler(
				clientAddress, instance.proxyChannel));

		
		instance.clients.put(clientAddress, clientChannel);
		
		clientChannel.closeFuture().addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				instance.clients.remove(clientAddress);
			}
		});
		
		return true;
	}
	
	/**
	 * Returns the handler with the lowest number of connections
	 * */
	public static ProxyEntryPointHandler loadBalance() {
		ProxyEntryPointHandler best = null;
		
		for (ProxyEntryPointHandler s : instances) {
			if (best == null || s.getScore() < best.getScore()) {
				best = s;
			} 
		}
		
		return best;
	}

	public static InetSocketAddress getClientAddress(Channel clientChannel) {
		if (clientChannel.remoteAddress() instanceof InetSocketAddress)
			return (InetSocketAddress) clientChannel.remoteAddress();
		return null;
	}
	
}
