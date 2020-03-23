package me.samxps.crafttunnel.netty.proxy;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.netty.HAProxyIdentifier;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketEncoder;

@RequiredArgsConstructor
public class ServerConnector {

	@NonNull private String host;
	@NonNull private Integer port;
	@NonNull private Boolean sendHAProxyHeader;
	private EventLoopGroup workerGroup;
	
	public ChannelFuture init(Channel clientChannel) throws Exception {
		return this.init(clientChannel, new NioEventLoopGroup());
	}
	
	public ChannelFuture init(Channel clientChannel, EventLoopGroup workerGroup) throws Exception{
		this.workerGroup = workerGroup;
		
		Bootstrap b = new Bootstrap();
		b.group(workerGroup)
		 .option(ChannelOption.SO_KEEPALIVE, true)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
			 protected void initChannel(SocketChannel ch) throws Exception {
				 if (sendHAProxyHeader) {
					 ch.pipeline().addLast(HAProxyIdentifier.fromClientChannel(clientChannel, host, port));
				 }
				 ch.pipeline().addLast(
					new ChannelLinker(clientChannel.newSucceededFuture()), 
					new MinecraftPacketEncoder()
				 );
			 };
		});
		
		return b.connect(host, port);
	}
	
	/**
	 * Releases all workers
	 * */
	public Future<?> exit() throws Exception {
		return workerGroup.shutdownGracefully();
	}
	
	/**
	 * Creates a new instance of {@link ServerConnector} using the current
	 * host and port configuration of {@link CraftTunnel} instance.
	 * */
	public static ServerConnector newDefault() {
		CraftTunnel main = CraftTunnel.getInstance();
		return new ServerConnector(main.getRemoteHost(), main.getRemotePort(), true);
	}
	
}
