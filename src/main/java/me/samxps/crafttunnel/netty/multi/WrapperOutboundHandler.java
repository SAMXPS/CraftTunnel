package me.samxps.crafttunnel.netty.multi;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketEncoder;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket.WrapperPacketType;

@RequiredArgsConstructor
public class WrapperOutboundHandler extends ChannelOutboundHandlerAdapter {

	private final InetSocketAddress clientAddress;
	private final Channel proxyChannel;
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		ByteBuf buf;
		if (msg instanceof ByteBuf) {
			buf = (ByteBuf) msg;
		} else if (msg instanceof MinecraftPacket) {
			buf = ctx.alloc().buffer();
			MinecraftPacketEncoder.encode((MinecraftPacket) msg, buf);
		} else {
			return;
		}
		
		WrapperPacket w = new WrapperPacket(clientAddress, WrapperPacketType.DATA_BYTES, buf);
		proxyChannel.write(w.encode());
	}
	
	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		proxyChannel.flush();
		super.flush(ctx);
	}
	
	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		proxyChannel.write(new WrapperPacket(clientAddress, WrapperPacketType.CONNECTION_CLOSE, null).encode());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}
