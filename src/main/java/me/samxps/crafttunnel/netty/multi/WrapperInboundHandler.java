package me.samxps.crafttunnel.netty.multi;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketEncoder;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket.WrapperPacketType;

@RequiredArgsConstructor
public class WrapperInboundHandler extends ChannelInboundHandlerAdapter{
	private final InetSocketAddress clientAddress;
	private final Channel proxyChannel;
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		proxyChannel.write(
			new WrapperPacket(
				clientAddress,
				WrapperPacketType.CONNECTION_START,
				null
			).encode()
		);
		proxyChannel.flush();
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
		proxyChannel.flush();
	}
	
	 @Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		proxyChannel.flush();
		super.channelReadComplete(ctx);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		proxyChannel.write(new WrapperPacket(clientAddress, WrapperPacketType.CONNECTION_CLOSE, null).encode());
		proxyChannel.flush();
		super.channelInactive(ctx);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
