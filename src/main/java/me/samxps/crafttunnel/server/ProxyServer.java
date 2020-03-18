package me.samxps.crafttunnel.server;

import java.util.logging.Level;

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
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.ProxyMode;
import me.samxps.crafttunnel.ServerType;
import me.samxps.crafttunnel.TunnelConfiguration;
import me.samxps.crafttunnel.netty.channel.ClientChannelHandler;
import me.samxps.crafttunnel.netty.connector.DirectServerConnector;
import me.samxps.crafttunnel.netty.multi.SlaveHandler;
import me.samxps.crafttunnel.netty.util.ServerBuilder;

/**
 * ProxyServer is the implementation of CraftTunnel using netty
 * as the network connection manager.
 * ProxyServer will instantiate all the necessary servers, such
 * as {@link MasterServer} or {@link SlaveServer} depending on
 * the corresponding {@link TunnelConfiguration}.
 * */
@RequiredArgsConstructor
public class ProxyServer implements Server{

	@NonNull private TunnelConfiguration config;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private MasterServer master;
	
	public ChannelFuture init() throws Exception {
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		
		if (config.getServerType() == ServerType.MASTER) {
			
			if (config.getProxyMode() == ProxyMode.MULTI_PROXY_TUNNEL) {
				master = new MasterServer(this);
				master.init().sync();
			}
			
			ServerBootstrap gate = ServerBuilder.buildServer(bossGroup, workerGroup, 
				new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new ClientChannelHandler(DirectServerConnector.newDefault()));
					}
				 });
			
			// Bind and wait for connections
			return gate.bind(config.getBindAddress());
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
