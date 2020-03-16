package me.samxps.crafttunnel.netty;

import java.util.concurrent.FutureTask;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.netty.multi.MasterHandler;

@RequiredArgsConstructor
public class TunnelServerConnector {

	@NonNull private MasterHandler master;
	@NonNull private String host;
	@NonNull private Integer port;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	public ChannelFuture init(Channel clientChannel) throws Exception{
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
		
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
		 .channel(NioServerSocketChannel.class)
		 .childHandler(new ChannelInitializer<SocketChannel>() {
			 protected void initChannel(SocketChannel ch) throws Exception {
				 ch.pipeline().addLast(new ServerChannelHandler(clientChannel));
			 };
		 })
		 .option(ChannelOption.SO_BACKLOG, 128)
		 .childOption(ChannelOption.SO_KEEPALIVE, true);
		return master.newChannel(b.bind(0));
	}
	
	/**
	 * Releases all workers
	 * */
	public Future<?> exit() throws Exception {
		bossGroup.shutdownGracefully();
		return workerGroup.shutdownGracefully();
	}
	
}
