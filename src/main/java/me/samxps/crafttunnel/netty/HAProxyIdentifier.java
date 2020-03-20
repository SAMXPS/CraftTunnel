package me.samxps.crafttunnel.netty;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HAProxyIdentifier extends ChannelInboundHandlerAdapter {

	private final InetSocketAddress clientAddress;
	private final InetSocketAddress serverAddress;
	
	private String getHAProxyHeader() {
		return String.format("PROXY %s %s %s %d %d\r\n",
				clientAddress.getAddress() instanceof Inet4Address ? "TCP4" : "TCP6",
				clientAddress.getAddress().getHostAddress(),
				serverAddress.getAddress().getHostAddress(),
				clientAddress.getPort(),
				serverAddress.getPort()
		);
	}	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		String h = getHAProxyHeader();
		ctx.channel().write(ctx.alloc().buffer(h.length()).writeBytes(h.getBytes()));	
		super.channelActive(ctx);
		ctx.pipeline().remove(this);
	}
	
	public static HAProxyIdentifier fromClientChannel(Channel clientChannel, String serverHost, int serverPort) {
		return new HAProxyIdentifier(
				 ((InetSocketAddress) clientChannel.remoteAddress()), 
				 new InetSocketAddress(serverHost, serverPort));
	}
	
}
