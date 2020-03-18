package me.samxps.crafttunnel.netty.multi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.server.MasterServer;

@RequiredArgsConstructor
/**
 * MasterHandler will handle incoming connections from slave servers to the master server.
 * Once a connection is made, master handler will perform initial handshake verification.
 * After the handshake is complete, this handler will register the control channel from the
 * slave server to the MasterServer.
 * */
public class MasterHandler extends ChannelInboundHandlerAdapter {

	@Getter private final MasterServer parent;
	private static final String MAGIC = "36782A5015779AC19CEAF2144939A7FA"; // md5("TunnelServer");
	private boolean handshake = false;
    private Channel ch;
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	this.ch = ctx.channel();
    	
    	this.ch.closeFuture().addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				parent.removeSlaveChannel(ch);
			}
		});
    }
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	ByteBuf buf = (ByteBuf) msg;
    	String cmd = ProxyPacket.readCommand(buf);
		ReferenceCountUtil.release(msg);
    	if (!handshake) {
			if (cmd.equals(MAGIC)) {
				ProxyPacket.writeCommand(ctx.channel(), "HANDSHAKE_OK");
				handshake = true;
				parent.addSlaveChannel(ctx.channel());
			}
			else ctx.close();
    	} else {
    		parent.onCommand(ch, cmd);
    	}
    }
  
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
        ctx.close();
	}
	
}
