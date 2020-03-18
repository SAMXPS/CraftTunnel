package me.samxps.crafttunnel.netty.util;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerBuilder {

	public static ServerBootstrap buildServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup,
			ChannelInitializer<?> init) {
		return new ServerBootstrap()
				 .group(bossGroup, workerGroup)
				 .channel(NioServerSocketChannel.class)
				 .childHandler(init)
				 .option(ChannelOption.SO_BACKLOG, 128)
				 .childOption(ChannelOption.SO_KEEPALIVE, true);
	}
	
}
