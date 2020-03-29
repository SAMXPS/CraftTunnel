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
	
	public static final String HANDLER_ENCODER = "encoder";
	public static final String HANDLER_INITIAL = "initial";
	public static final String HANDLER_CLIENT = "client";
	public static final String HANDLER_DECODER = "decoder";
	public static final String HANDLER_PROXY_EXIT = "exit";
	public static final String HANDLER_PROXY_ENTRY = "proxy";
	public static final String HANDLER_WRAPPER = "wrapper";
	
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
						if (config.isMinecraftProtocolOnly())
							ch.pipeline().addLast(HANDLER_DECODER, new MinecraftPacketDecoder());
						ch.pipeline().addLast(HANDLER_INITIAL, new InitialHandler(config));
						ch.pipeline().addLast(HANDLER_ENCODER, new MinecraftPacketEncoder());
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
						if (config.isMinecraftProtocolOnly())
							ch.pipeline().addLast(HANDLER_DECODER, new MinecraftPacketDecoder());
						ch.pipeline().addLast(HANDLER_PROXY_EXIT,    new ProxyExitPointHandler(config));
						ch.pipeline().addLast(HANDLER_ENCODER, new MinecraftPacketEncoder());
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
