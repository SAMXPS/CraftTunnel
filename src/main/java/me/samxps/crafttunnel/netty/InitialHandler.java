package me.samxps.crafttunnel.netty;

import java.util.logging.Level;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
		CraftTunnel.getLogger().log(Level.INFO, "[{0}] {1} Initial handler connected", new Object[] {
				"ClientChannelHandler", ProxyEntryPointHandler.getClientAddress(ctx.channel()).toString()
		});
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {		
		if (active && msg instanceof MinecraftPacket) {
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
		if (h != null) {
			
			if (h.getNextState() == 1) {
				state = ProtocolState.STATUS;
			} else if (h.getNextState() == 2) {
				state = ProtocolState.LOGIN;
				active = false;
			} else {
				return false;
			}
			
			if (config.getProxyMode() == ProxyMode.MULTI_PROXY_TUNNEL) {
				if (!ProxyEntryPointHandler.handleClientChannel(ctx.channel()))
					return false;
			} else {
				nextPipeline(
					ctx, "client", new ChannelLinker(ServerConnector.newDefault().init(ctx.channel()))
				);
			}
			
			// TODO: log successfull connection ?
			return true;
		} 
		return false;
	}
	
	private void handleMagicPacket(ChannelHandlerContext ctx, MinecraftPacket p) {
		// Verify if multi-proxy is enabled
		if (config.getProxyMode() == ProxyMode.MULTI_PROXY_TUNNEL) {
			MagicPacket magic = MagicPacket.fromMinecraftPacket(p);

			if (magic != null && magic.validateTimeCode()) {
				active = false;
				nextPipeline(ctx, "proxy", new ProxyEntryPointHandler());
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
		ctx.channel().pipeline().addAfter("initial", handlerName, handler);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
		super.exceptionCaught(ctx, cause);
	}
}
