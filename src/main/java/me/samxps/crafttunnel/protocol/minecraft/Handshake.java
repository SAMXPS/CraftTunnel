package me.samxps.crafttunnel.protocol.minecraft;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Handshake {

	private int protocolVersion;
	private String serverAddress;
	private int serverPort;
	private int nextState;
	
	public static Handshake fromPacket(MinecraftPacket p) {
		if (p.getPacketID() == 0 && p.getData().isReadable()) { 
			p = p.clone();
			int proto_version = p.readVarInt();
			String sv_add = p.readString();
			int sv_port = p.readUnsignedShort();
			int nx_state = p.readVarInt();
			p.getData().release();
			return new Handshake(proto_version, sv_add, sv_port, nx_state);
		}
		return null;
	}
	
}
