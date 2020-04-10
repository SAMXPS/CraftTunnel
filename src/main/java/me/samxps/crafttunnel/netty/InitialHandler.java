package me.samxps.crafttunnel.netty;

import java.util.logging.Level;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.ProxyConfiguration;
import me.samxps.crafttunnel.ProxyMode;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketDecoder;
import me.samxps.crafttunnel.netty.multi.ProxyEntryPointHandler;
import me.samxps.crafttunnel.netty.proxy.ChannelLinker;
import me.samxps.crafttunnel.netty.proxy.ServerConnector;
import me.samxps.crafttunnel.protocol.minecraft.Handshake;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.minecraft.ProtocolState;
import me.samxps.crafttunnel.protocol.multi.MagicPacket;
import me.samxps.crafttunnel.server.ProxyServer;

/**
 * InitalHandler will handle all incoming connections on proxy entry-point.
 * It will also register the next handler on the pipeline according to the
 * type of connection being made and current {@link ProxyConfiguration}. 
 * */
@RequiredArgsConstructor
public class InitialHandler extends ChannelInboundHandlerAdapter{

	private final ProxyConfiguration config;
	private boolean active = true;
	private ProtocolState state = ProtocolState.HANDSHAKE;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		CraftTunnel.getLogger().log(Level.INFO, "[{0}] {1} has connected", new Object[] {
				"InitialHandler", ProxyEntryPointHandler.getClientAddress(ctx.channel()).toString()
		});
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {		
		if (!active) {
			
		} else if (msg instanceof MinecraftPacket) {
			// When minecraft protocol is enabled
			
			MinecraftPacket p = (MinecraftPacket) msg;
			
			if (state == ProtocolState.HANDSHAKE) {
				
				// Minecraft handshake packet
				if (p.getPacketID() == 0) {
					if (!handleHandshake(ctx, Handshake.fromPacket(p))) {
						ctx.close();
						return;
					}
				}
				
				// Magic packet for multi-proxy mode
				if (p.getPacketID() == MagicPacket.getMagicPacketID()) {
					handleMagicPacket(ctx, p);
					return;
				}
			}			
			
			if (state == ProtocolState.STATUS && p.getPacketID() == 1) {
				// PING and PONG
				ctx.channel().writeAndFlush(p);
				return;
			}
		} else if (msg instanceof ByteBuf && !config.isMinecraftProtocolOnly()) {
			// When minecraft protocol is disabled
			ByteBuf cpy = null;
			try {
				// Copy the message
				cpy = ((ByteBuf)msg).copy();
				// Try to decode as a Minecraft Packet
				MinecraftPacket p = MinecraftPacketDecoder.decode(cpy);
				
				// Verify if it is related to multi-proxy mode
				if (p.getPacketID() == MagicPacket.getMagicPacketID()) {
					handleMagicPacket(ctx, p);
					return;
				}
			} catch (Exception e) {
				// If it is not a minecraft packet, just continue
			} finally {
				if (cpy != null) cpy.release();
			}
			
			if (!proxyConnection(ctx)) {
				ctx.close();
				return;
			}
		}
		
		// Forward to next handler in the pipeline
		super.channelRead(ctx, msg);
		
		if (!active){
			ctx.channel().pipeline().remove(this);
			ctx.channel().pipeline().remove(MinecraftPacketDecoder.class);
		}
	}
	
	/**
	 * This method will handle incoming handshake request from Minecraft client.
	 * @return true if the handshake was accepted and data should be sent over
	 * to the next handlers; false if the connection should be closed.
	 * */
	private boolean handleHandshake(ChannelHandlerContext ctx, Handshake h) throws Exception {
		if (h == null) return false;
			
		if (h.getNextState() == 1) {
			state = ProtocolState.STATUS;
		} else if (h.getNextState() == 2) {
			state = ProtocolState.LOGIN;
			active = false;
		} else {
			return false;
		}
		
		return proxyConnection(ctx);
	}
	
	/**
	 * This method will add next handlers on the pipeline to proxy the connection,
	 * depending on this {@link #config}.
	 * */
	private boolean proxyConnection(ChannelHandlerContext ctx) throws Exception {
		if (config.getProxyMode() == ProxyMode.MULTI_PROXY_TUNNEL) {
			if (!ProxyEntryPointHandler.handleClientChannel(ctx.channel()))
				return false;
		} else {
			nextPipeline(
				ctx, ProxyServer.HANDLER_CLIENT, 
				new ChannelLinker(
					new ServerConnector(
						config.getServerHost(), 
						config.getServerPort(), 
						config.getTransportType(), 
						config.isHAProxyHeaderEnabled()
					).init(ctx.channel())
				)
			);
		}

		CraftTunnel.getLogger().log(Level.INFO, "[{0}] {1} handshake accepted", new Object[] {
				"InitialHandler", ProxyEntryPointHandler.getClientAddress(ctx.channel()).toString()
		});
		
		return true;
	}
	
	private void handleMagicPacket(ChannelHandlerContext ctx, MinecraftPacket p) {
		// Verify if multi-proxy is enabled
		if (config.getProxyMode() == ProxyMode.MULTI_PROXY_TUNNEL) {
			MagicPacket magic = MagicPacket.fromMinecraftPacket(p);

			if (magic != null && magic.validateTimeCode()) {
				active = false;
				
				// adds minecraft decoder if not present
				if (ctx.pipeline().get(ProxyServer.HANDLER_DECODER) == null) {
					ctx.pipeline().addBefore(ProxyServer.HANDLER_INITIAL, ProxyServer.HANDLER_DECODER, new MinecraftPacketDecoder());
				}
				
				nextPipeline(ctx, ProxyServer.HANDLER_PROXY_ENTRY, new ProxyEntryPointHandler());
				ctx.channel().pipeline().remove(this);
				return;
			}
		}
		// Close connection if multi proxy mode is disabled
		// or the MagicPacket was invalid
		ctx.close();
	}
	
	/**
	 * Adds the next {@link ChannelHandler} on the pipeline.
	 * */
	private void nextPipeline(ChannelHandlerContext ctx, String handlerName, ChannelHandler handler) {
		ctx.channel().pipeline().addAfter(ProxyServer.HANDLER_INITIAL, handlerName, handler);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
		super.exceptionCaught(ctx, cause);
	}
}
