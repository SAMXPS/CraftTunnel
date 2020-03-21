package me.samxps.crafttunnel.protocol.minecraft;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.nio.NioChannelOption;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MinecraftPacket implements Cloneable {

	private int packetID;
	private ByteBuf data;
	
	public int readVarInt() {
		return readVarInt(data);
	}
	
	public void writeVarInt(int value) {
		writeVarInt(value, data);
	}
	
	public long readVarLong() {
		return readVarLong(data);
	}
	
	public void writeVarLong(long value) {
		writeVarLong(value);
	}
	
	public String readString() {
		return readString(data);
	}
	
	public void writeString(String str) {
		writeString(str, data);
	}
	
	public int readUnsignedShort() {
		return data.readUnsignedShort();
	}
	
	@Override
	public MinecraftPacket clone() {
		return new MinecraftPacket(packetID, data.copy());
	}
	
	public static int readVarInt(ByteBuf data) {
	    int numRead = 0;
	    int result = 0;
	    byte read;
	    do {
	        read = data.readByte();
	        int value = (read & 0b01111111);
	        result |= (value << (7 * numRead));

	        numRead++;
	        if (numRead > 5) {
	            throw new RuntimeException("VarInt is too big");
	        }
	    } while ((read & 0b10000000) != 0);

	    return result;
	}
	
	public static long readVarLong(ByteBuf data) {
	    int numRead = 0;
	    long result = 0;
	    byte read;
	    do {
	        read = data.readByte();
	        int value = (read & 0b01111111);
	        result |= (value << (7 * numRead));

	        numRead++;
	        if (numRead > 10) {
	            throw new RuntimeException("VarLong is too big");
	        }
	    } while ((read & 0b10000000) != 0);

	    return result;
	}
	
	public static void writeVarInt(int value, ByteBuf buf) {
	    do {
	        byte temp = (byte)(value & 0b01111111);
	        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
	        value >>>= 7;
	        if (value != 0) {
	            temp |= 0b10000000;
	        }
	        buf.writeByte(temp);
	    } while (value != 0);
	}
	
	public static void writeVarLong(long value, ByteBuf buf) {
	    do {
	        byte temp = (byte)(value & 0b01111111);
	        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
	        value >>>= 7;
	        if (value != 0) {
	            temp |= 0b10000000;
	        }
	        buf.writeByte(temp);
	    } while (value != 0);
	}
	
	public static String readString(ByteBuf data) {
		int len = readVarInt(data);
		byte[] str = new byte[len];
		data.readBytes(str);
		return new String(str, StandardCharsets.UTF_8);
	}
	
	public static void writeString(String str, ByteBuf buf) {
		writeVarInt(str.length(), buf);
		buf.writeBytes(str.getBytes(StandardCharsets.UTF_8));
	}
}
