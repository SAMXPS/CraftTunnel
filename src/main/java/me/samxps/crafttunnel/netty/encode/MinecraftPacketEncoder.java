package me.samxps.crafttunnel.netty.encode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;

public class MinecraftPacketEncoder extends MessageToByteEncoder<MinecraftPacket>{
	
	@Override
	protected void encode(ChannelHandlerContext ctx, MinecraftPacket msg, ByteBuf out) throws Exception {
		ByteBuf pidbuf = Unpooled.buffer();
		MinecraftPacket.writeVarInt(msg.getPacketID(), pidbuf);
		MinecraftPacket.writeVarInt(pidbuf.readableBytes() + msg.getData().readableBytes(), out);
		out.writeBytes(pidbuf);
		out.writeBytes(msg.getData());
		pidbuf.release();
	}
}
