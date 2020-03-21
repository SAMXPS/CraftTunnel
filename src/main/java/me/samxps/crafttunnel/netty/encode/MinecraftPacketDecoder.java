package me.samxps.crafttunnel.netty.encode;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;

public class MinecraftPacketDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int ix = in.readerIndex();
		int l = MinecraftPacket.readVarInt(in);
		if (in.readableBytes() < l) {
			in.readerIndex(ix);
			return;
		}
		out.add(decode(l, in));
	}

	public static MinecraftPacket decode(int l, ByteBuf in) {
		// TODO: make these index calculations prettier 
		int a = in.readerIndex();
		int packetID = MinecraftPacket.readVarInt(in);
		int b = in.readerIndex();
		return new MinecraftPacket(packetID, in.readBytes(l + (a-b)));
	}
	
	public static MinecraftPacket decode(ByteBuf in) {
		return decode(MinecraftPacket.readVarInt(in), in);
	}
	
}
