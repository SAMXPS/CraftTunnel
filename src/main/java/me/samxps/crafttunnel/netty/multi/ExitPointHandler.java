package me.samxps.crafttunnel.netty.multi;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.ProxyConfiguration;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.multi.MagicPacket;

@RequiredArgsConstructor
public class ExitPointHandler extends ChannelInboundHandlerAdapter {

	private final ProxyConfiguration config;
	
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
			// TODO: handle incoming packets
		} else {
			// Invalid data
		}
		super.channelRead(ctx, msg);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}
