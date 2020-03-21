package me.samxps.crafttunnel.protocol.multi;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;

@RequiredArgsConstructor
public class MagicPacket {
	
	@Getter
	private final static int magicPacketID = -715827805;
	private final long timeCode;
	
	public MagicPacket() {
		this.timeCode = generateTimeCode(0);
	}
	
	public boolean validateTimeCode() {
		return timeCode == generateTimeCode(0) || timeCode == generateTimeCode(-1);
	}
	
	public static long generateTimeCode(int diff) {
		return new Random((System.currentTimeMillis() / 10000) + diff).nextLong();
	}
	
	public static MagicPacket fromMinecraftPacket(MinecraftPacket p) {
		if (p.getPacketID() == magicPacketID) {
			p = p.clone();
			long timeCode = p.readVarLong();
			p.getData().release();
			return new MagicPacket(timeCode);
		}
		return null;
	}
	
	public MinecraftPacket toMinecraftPacket() {
		ByteBuf b = Unpooled.buffer();
		MinecraftPacket.writeVarLong(magicPacketID, b);
		return new MinecraftPacket(magicPacketID, b);
	}
	
}
