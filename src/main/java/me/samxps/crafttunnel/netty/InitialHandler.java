package me.samxps.crafttunnel.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.ProxyConfiguration;
import me.samxps.crafttunnel.ProxyMode;
import me.samxps.crafttunnel.netty.channel.ClientChannelHandler;
import me.samxps.crafttunnel.netty.connector.DirectServerConnector;
import me.samxps.crafttunnel.netty.connector.ServerConnector;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketDecoder;
import me.samxps.crafttunnel.netty.multi.ProxyEntryPointHandler;
import me.samxps.crafttunnel.protocol.minecraft.Handshake;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.minecraft.ProtocolState;
import me.samxps.crafttunnel.protocol.multi.MagicPacket;

@RequiredArgsConstructor
public class InitialHandler extends ChannelInboundHandlerAdapter{

	private final ProxyConfiguration config;
	private boolean active = true;
	private ProtocolState state = ProtocolState.HANDSHAKE;
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {		
		if (active && msg instanceof MinecraftPacket) {
			MinecraftPacket p = (MinecraftPacket) msg;
			
			if (state == ProtocolState.HANDSHAKE) {
				if (p.getPacketID() == 0) {
					Handshake h = Handshake.fromPacket(p);
					if (h != null) {
						
						if (h.getNextState() == 1) {
							state = ProtocolState.STATUS;
						} else if (h.getNextState() == 2) {
							state = ProtocolState.LOGIN;
							active = false;
						} else {
							ctx.close();
							return;
						}
						
						ServerConnector connector = getServerConnector();
						
						if (connector != null)
							nextPipeline(
								ctx, "client", 
								new ClientChannelHandler(connector)
							);
						else ctx.close();
					}
				}
				
				// Magic packet for multi-proxy mode
				if (p.getPacketID() == MagicPacket.getMagicPacketID()) {
					
					// Verify if multi-proxy is enabled
					if (config.getProxyMode() != ProxyMode.MULTI_PROXY_TUNNEL) {
						ctx.close();
						return;
					}
					
					MagicPacket magic = MagicPacket.fromMinecraftPacket(p);
					if (magic != null && magic.validateTimeCode()) {
						active = false;
						nextPipeline(ctx, "proxy", new ProxyEntryPointHandler());
					}
				}
			}			
			if (state == ProtocolState.STATUS && p.getPacketID() == 1) {
				// PING and PONG
				ctx.channel().writeAndFlush(p);
				return;
			}
			
		}
		
		super.channelRead(ctx, msg);
		
		if (!active){
			ctx.channel().pipeline().remove(this);
			ctx.channel().pipeline().remove(MinecraftPacketDecoder.class);
		}
	}
	
	private void nextPipeline(ChannelHandlerContext ctx, String handlerName, ChannelHandler handler) {
		ctx.channel().pipeline().addAfter("initial", handlerName, handler);
	}
	
	private ServerConnector getServerConnector() {
		if (config.getProxyMode() == ProxyMode.PROXY_ONLY) {
			return DirectServerConnector.newDefault();
		} else if (config.getProxyMode() == ProxyMode.MULTI_PROXY_TUNNEL) {
			return ProxyEntryPointHandler.generateConnector();
		}
		return null;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
		super.exceptionCaught(ctx, cause);
	}
}
