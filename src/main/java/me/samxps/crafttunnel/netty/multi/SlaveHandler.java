package me.samxps.crafttunnel.netty.multi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class SlaveHandler extends ChannelInboundHandlerAdapter implements MultiProxyHandler{

	private static final String MAGIC = "36782A5015779AC19CEAF2144939A7FA"; // md5("TunnelServer");
	private boolean handshake = false;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		final ByteBuf magic = ctx.alloc().buffer(MAGIC.length());
		magic.writeBytes(MAGIC.getBytes());
		
		ctx.writeAndFlush(magic).sync();
	}
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		onCommand(ctx, readCommand((ByteBuf) msg));
		ReferenceCountUtil.release(msg);
    }
	
    private void onCommand(ChannelHandlerContext ctx, String cmd) {
    	if (!handshake) {
    		if (cmd.equals("HANDSHAKE_OK")) {
    			this.handshake = true;
    			return;
    		}
    		// Handshake error
            ctx.close();
    	}
    	if (cmd.startsWith("CONNECT")) {
    		int port = Integer.valueOf(cmd.split(" ")[1]);
    		
    	}
    }
    
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
        ctx.close();
	}
	
}
