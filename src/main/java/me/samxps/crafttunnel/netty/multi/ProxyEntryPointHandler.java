package me.samxps.crafttunnel.netty.multi;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.ServerType;
import me.samxps.crafttunnel.netty.channel.ClientChannelHandler;
import me.samxps.crafttunnel.netty.channel.ServerChannelHandler;
import me.samxps.crafttunnel.netty.connector.ServerConnector;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketEncoder;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket.WrapperPacketType;

/**
 * {@link ProxyEntryPointHandler} will be responsible for sending and receiving
 * data from remote {@link ServerType#EXIT_POINT}.
 * */
public class ProxyEntryPointHandler extends ChannelInboundHandlerAdapter {

	private static HashSet<ProxyEntryPointHandler> instances = new HashSet<ProxyEntryPointHandler>();
	private HashMap<InetSocketAddress, EmbeddedChannel> virtual = new HashMap<InetSocketAddress, EmbeddedChannel>();
	
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
		EmbeddedChannel vchannel = virtual.get(w.getClientAddress());
		
		if (vchannel == null)
			 return;
		
		if (w.getType() == WrapperPacketType.CONNECTION_CLOSE) {
			vchannel.close();
		} else if (w.getType() == WrapperPacketType.DATA_BYTES) {
			vchannel.writeOneInbound(w.getData());
		} else if (w.getType() == WrapperPacketType.DATA_PACKET) {
			vchannel.writeOneInbound(w.getData());
		}
	}
	
	private ChannelFuture newVirtualConnection(Channel clientChannel) throws Exception {
		InetSocketAddress clientAddress = ClientChannelHandler.getClientAddress(clientChannel);
		
		EmbeddedChannel ch = new EmbeddedChannel(
				new ServerChannelHandler(clientChannel), 
				new WrapperOutboundHandler(clientAddress, proxyChannel)
		);
		
		virtual.put(clientAddress, ch);

		ch.closeFuture().addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				virtual.remove(clientAddress);
			}
		});
		
		proxyChannel.write(
			new WrapperPacket(
				clientAddress,
				WrapperPacketType.CONNECTION_START,
				null
			).encode()
		);
		proxyChannel.flush();
		
		return ch.newSucceededFuture();
	}
	
	public int getScore() {
		return virtual.size();
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
	
	/**
	 * This will return a new virtual server connector
	 * to send data trough the multiproxy tunnel
	 * */
	public static ServerConnector generateConnector() {
		ProxyEntryPointHandler handler = loadBalance();
		if (handler == null) return null;
		
		return new ServerConnector() {
			
			private Channel ch;
			
			@Override
			public ChannelFuture init(Channel clientChannel) throws Exception {
				ChannelFuture f = handler.newVirtualConnection(clientChannel);
				this.ch = f.channel();
				clientChannel.closeFuture().addListener(new ChannelFutureListener() {
					
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						f.channel().close();
					}
				});
				return f;
			}
			
			@Override
			public Future<?> exit() throws Exception {
				return ch.close();
			}
		};
	}
	
}
