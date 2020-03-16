package me.samxps.crafttunnel.netty.multi;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class ProxyPacket {

	private int lenght = 0;
	@NonNull private ByteBuf buf;
	
	public void write(byte[] data) {
		buf.writeBytes(data);
		lenght += data.length;
	}
	
}
