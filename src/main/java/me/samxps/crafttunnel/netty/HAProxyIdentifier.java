package me.samxps.crafttunnel.netty;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.netty.channel.ClientChannelHandler;
import me.samxps.crafttunnel.netty.multi.ProxyEntryPointHandler;
import me.samxps.crafttunnel.netty.multi.ProxyExitPointHandler;

@RequiredArgsConstructor
public class HAProxyIdentifier extends ChannelInboundHandlerAdapter {

	private final InetSocketAddress clientAddress;
	private final InetSocketAddress serverAddress;
	
	private static String getHAProxyHeader(InetSocketAddress clientAddress, InetSocketAddress serverAddress) {
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
		writeIndentifier(ctx.channel(), clientAddress, serverAddress);
		super.channelActive(ctx);
		ctx.pipeline().remove(this);
	}
	
	public static void writeIndentifier(Channel ch, InetSocketAddress clientAddress, InetSocketAddress serverAddress) {
		String h = getHAProxyHeader(clientAddress, serverAddress);
		ch.write(ch.alloc().buffer(h.length()).writeBytes(h.getBytes()));	
	}
	
	public static HAProxyIdentifier fromClientChannel(Channel clientChannel, String serverHost, int serverPort) {
		return new HAProxyIdentifier(
			ProxyEntryPointHandler.getClientAddress(clientChannel), 
			new InetSocketAddress(serverHost, serverPort)
		);
	}
	
}
