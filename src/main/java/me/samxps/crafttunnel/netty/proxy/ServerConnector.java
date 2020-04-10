package me.samxps.crafttunnel.netty.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.netty.HAProxyIdentifier;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketEncoder;
import me.samxps.crafttunnel.protocol.TransportType;

@RequiredArgsConstructor
public class ServerConnector {

	@NonNull private String host;
	@NonNull private Integer port;
	@NonNull private TransportType transportType;
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
		 .channel(transportType.getChannelClass())
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
	
}
