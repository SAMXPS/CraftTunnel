package me.samxps.crafttunnel.netty.multi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface MultiProxyHandler {

    default void writeCommand(Channel ch, String cmd) {
    	ByteBuf buf = ch.alloc().buffer(cmd.length() + 4);
    	buf.writeInt(cmd.length());
    	buf.writeBytes(cmd.getBytes());
    	ch.write(buf);
    }
    
    default String readCommand(ByteBuf buf) {
    	int len = buf.readInt();
		byte[] str = new byte[len];
		buf.readBytes(str);
		return new String(str);
    }
}
