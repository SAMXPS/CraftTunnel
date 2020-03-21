package me.samxps.crafttunnel.protocol.multi;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;

/**
 * A WrapperPacket will be used in the connection between
 * proxy's entrypoints and exitpoints.
 * */
@AllArgsConstructor
@Getter
public class WrapperPacket {

	@Getter private static final int packetID = 0x01;
	private InetSocketAddress clientAddress;
	private WrapperPacketType type;
	private ByteBuf data;

	private WrapperPacket() {}
	
	/**
	 * Note: this method will consume the packet bytebuf
	 * */
	public static WrapperPacket decode(MinecraftPacket p) {
		if (p.getPacketID() == packetID) {
			WrapperPacket w = new WrapperPacket();
			w.clientAddress = new InetSocketAddress(p.readString(), p.readUnsignedShort());
			w.type = WrapperPacketType.fromID(p.getData().readByte());
			w.data = p.getData().discardReadBytes();
			return w;
		}
		return null;
	}
	
	public MinecraftPacket encode() {
		ByteBuf head = Unpooled.buffer();
		MinecraftPacket.writeString(clientAddress.getAddress().getHostAddress(), head);
		head.writeShort(clientAddress.getPort());
		ByteBuf packetData = data == null ? head : head.writeBytes(data);
		return new MinecraftPacket(packetID, packetData);
	}
	
	public static enum WrapperPacketType {
		CONNECTION_START(0x00),
		CONNECTION_CLOSE(0x01),
		DATA_BYTES(0x02),
		DATA_PACKET(0x03);

		private static WrapperPacketType[] vec = new WrapperPacketType[4];
		@Getter private byte ID;
		
		static {
			// Mapping enum values to vector, like a Map<int, enum> without hashing
			for (WrapperPacketType t : WrapperPacketType.values()) {
				vec[t.ID] = t;
			}
		}
		
		private WrapperPacketType(int id) {
			this.ID = (byte) id;
		}
		
		public static WrapperPacketType fromID(int id) {
			return (id < vec.length && id >= 0) ? vec[id] : null;
		}
	}
	
}
