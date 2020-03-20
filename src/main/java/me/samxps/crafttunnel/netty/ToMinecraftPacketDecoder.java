package me.samxps.crafttunnel.netty;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;

public class ToMinecraftPacketDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int ix = in.readerIndex();
		int l = MinecraftPacket.readVarInt(in);
		if (in.readableBytes() < l) {
			in.readerIndex(ix);
			return;
		}
		// TODO: make these index calculations prettier 
		int a = in.readerIndex();
		int packetID = MinecraftPacket.readVarInt(in);
		int b = in.readerIndex();
		out.add(new MinecraftPacket(packetID, in.readBytes(l + (a-b))));
		
	}
	
}
