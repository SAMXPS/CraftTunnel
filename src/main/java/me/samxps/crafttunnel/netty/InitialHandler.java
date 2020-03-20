package me.samxps.crafttunnel.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.ProxyConfiguration;
import me.samxps.crafttunnel.netty.channel.ClientChannelHandler;
import me.samxps.crafttunnel.netty.connector.DirectServerConnector;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketDecoder;
import me.samxps.crafttunnel.protocol.minecraft.Handshake;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.minecraft.ProtocolState;

@RequiredArgsConstructor
public class InitialHandler extends ChannelInboundHandlerAdapter{

	//private final ProxyConfiguration config;
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
						ctx.channel().pipeline().addAfter("initial", "client", new ClientChannelHandler(DirectServerConnector.newDefault()));
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
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
		super.exceptionCaught(ctx, cause);
	}
}
