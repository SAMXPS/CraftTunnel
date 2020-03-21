package me.samxps.crafttunnel.netty.multi;

import java.net.InetSocketAddress;
import java.util.HashMap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.ProxyConfiguration;
import me.samxps.crafttunnel.netty.channel.ClientChannelHandler;
import me.samxps.crafttunnel.netty.connector.DirectServerConnector;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.multi.MagicPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket.WrapperPacketType;

@RequiredArgsConstructor
public class ProxyExitPointHandler extends ChannelInboundHandlerAdapter {

	private final ProxyConfiguration config;
	private HashMap<InetSocketAddress, Channel> virtual = new HashMap<InetSocketAddress, Channel>();
	public static AttributeKey<InetSocketAddress> PROXIED_CLIENT_ADDRESS = AttributeKey.valueOf("PROXIED_CLIENT_ADDRESS");
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Sends magic packet to proxy entrypoint
		ctx.write(new MagicPacket().toMinecraftPacket());
		ctx.flush();
		super.channelActive(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof MinecraftPacket) {
			MinecraftPacket p = (MinecraftPacket) msg;
			
			if (p.getPacketID() == WrapperPacket.getPacketID()) {
				handleWrapper(WrapperPacket.decode(p));
			} else {
				// discard
			}
		}
		// TODO: release unknown messages ?
	}
	
	private void handleWrapper(WrapperPacket w) {
		// TODO: map methods, direct access?
		
		if (w.getType() == WrapperPacketType.CONNECTION_START) {
			EmbeddedChannel ch = new EmbeddedChannel(new ClientChannelHandler(DirectServerConnector.newDefault()));
			ch.attr(PROXIED_CLIENT_ADDRESS).set(w.getClientAddress());
			virtual.put(w.getClientAddress(), ch);
		} else {
			Channel vchannel = virtual.get(w.getClientAddress());
			
			if (vchannel == null) return;
			
			if (w.getType() == WrapperPacketType.CONNECTION_CLOSE) {
				
			} else if (w.getType() == WrapperPacketType.DATA_BYTES) {
				
			} else if (w.getType() == WrapperPacketType.DATA_PACKET) {
				
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}
