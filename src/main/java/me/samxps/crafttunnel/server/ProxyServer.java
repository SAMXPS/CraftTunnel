package me.samxps.crafttunnel.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.ProxyConfiguration;
import me.samxps.crafttunnel.ServerType;
import me.samxps.crafttunnel.netty.InitialHandler;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketDecoder;
import me.samxps.crafttunnel.netty.encode.MinecraftPacketEncoder;
import me.samxps.crafttunnel.netty.multi.ProxyExitPointHandler;

/**
 * ProxyServer is the implementation of CraftTunnel using netty
 * as the network connection manager.
 * */
@RequiredArgsConstructor
public class ProxyServer implements Server{

	@NonNull private ProxyConfiguration config;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	private ServerBootstrap buildServer(ChannelInitializer<?> init) {
		return new ServerBootstrap()
				 .group(bossGroup, workerGroup)
				 .channel(NioServerSocketChannel.class)
				 .childHandler(init)
				 .option(ChannelOption.SO_BACKLOG, 128)
				 .childOption(ChannelOption.SO_KEEPALIVE, true);
	}
	
	public ChannelFuture init() throws Exception {
		workerGroup = new NioEventLoopGroup();
		
		if (config.getServerType() == ServerType.ENTRY_POINT) {
			bossGroup = new NioEventLoopGroup();
			
			ServerBootstrap gate = buildServer(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast("decoder", new MinecraftPacketDecoder())
						             .addLast("initial", new InitialHandler(config))
						             .addLast("encoder", new MinecraftPacketEncoder());
					}
				 });
			
			// Bind and wait for connections
			return gate.bind(config.getBindAddress());
		}
		
		if (config.getServerType() == ServerType.EXIT_POINT) {
			
			return new Bootstrap()
				.group(workerGroup)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast("decoder", new MinecraftPacketDecoder())
			             			 .addLast("exit",    new ProxyExitPointHandler(config))
			             			 .addLast("encoder", new MinecraftPacketEncoder());
					}
				})
				.connect(config.getMasterAddress());
		}
		
		return null;
	}
	
	@Override
	public Future<?> exit() throws Exception {
		// TODO: Know how to join multiple futures into one
		bossGroup.shutdownGracefully();
		return workerGroup.shutdownGracefully();
	}
	
	
	
}
