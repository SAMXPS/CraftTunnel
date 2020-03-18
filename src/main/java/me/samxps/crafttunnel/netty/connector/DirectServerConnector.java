package me.samxps.crafttunnel.netty.connector;

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
import me.samxps.crafttunnel.netty.channel.ServerChannelHandler;

@RequiredArgsConstructor
public class DirectServerConnector implements ServerConnector {

	@NonNull private String host;
	@NonNull private Integer port;
	private EventLoopGroup workerGroup;
	
	public ChannelFuture init(Channel clientChannel) throws Exception{
		workerGroup = new NioEventLoopGroup();
		
		Bootstrap b = new Bootstrap();
		b.group(workerGroup)
		 .option(ChannelOption.SO_KEEPALIVE, true)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
			 protected void initChannel(SocketChannel ch) throws Exception {
				 ch.pipeline().addLast(new ServerChannelHandler(clientChannel));
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
	 * Creates a new instance of {@link DirectServerConnector} using the current
	 * host and port configuration of {@link CraftTunnel} instance.
	 * */
	public static DirectServerConnector newDefault() {
		CraftTunnel main = CraftTunnel.getInstance();
		return new DirectServerConnector(main.getRemoteHost(), main.getRemotePort());
	}
	
}
