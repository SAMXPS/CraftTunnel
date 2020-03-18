package me.samxps.crafttunnel.netty.multi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * ProxyPacket is an idea for future packet handling between master and slave
 * servers. It will help sized payload to be sent over Netty connections.
 * */
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
	
    public static void writeCommand(Channel ch, String cmd) {
    	ByteBuf buf = ch.alloc().buffer(cmd.length() + 4);
    	buf.writeInt(cmd.length());
    	buf.writeBytes(cmd.getBytes());
    	ch.write(buf);
    }
    
    public static String readCommand(ByteBuf buf) {
    	int len = buf.readInt();
		byte[] str = new byte[len];
		buf.readBytes(str);
		return new String(str);
    }
	
}
