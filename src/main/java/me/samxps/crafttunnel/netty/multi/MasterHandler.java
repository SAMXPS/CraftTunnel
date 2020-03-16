package me.samxps.crafttunnel.netty.multi;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.netty.TunnelServerConnector;

@RequiredArgsConstructor
public class MasterHandler extends ChannelInboundHandlerAdapter implements MultiProxyHandler {

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
				parent.removeHandler(MasterHandler.this);
			}
		});
    }
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	ByteBuf buf = (ByteBuf) msg;
    	if (!handshake) {
    		try {
    			byte[] mg = new byte[32];
    			buf.readBytes(mg);
    			String resp = new String(mg);
    			if (resp.equals(MAGIC)) {
    				writeCommand(ctx.channel(), "HANDSHAKE_OK");
    				handshake = true;
    				parent.addHandler(this);
    			}
    			else ctx.close().sync();
    		} finally {
    			ReferenceCountUtil.release(msg);
			}
    	} else {
    		onCommand(readCommand(buf));
			ReferenceCountUtil.release(msg);
    	}
    }
    
    public ChannelFuture newChannel(ChannelFuture incoming) {
    	incoming.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				Channel r = future.channel();
				requestCon(((InetSocketAddress) r.localAddress()).getPort());
			}
		});
    	
    	return incoming;
    }
    
    private void requestCon(int master_port) {
    	writeCommand(ch, "CONNECT " + master_port);
    }
    
    private void onCommand(String cmd) {
    	
    }
    
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
        ctx.close();
	}
	
}
