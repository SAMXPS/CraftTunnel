package me.samxps.crafttunnel.netty;

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
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.ProxyMode;
import me.samxps.crafttunnel.ServerType;
import me.samxps.crafttunnel.TunnelConfiguration;
import me.samxps.crafttunnel.netty.multi.MasterServer;
import me.samxps.crafttunnel.netty.multi.SlaveHandler;

/**
 * TunnelServer is the implementation of CraftTunnel using netty
 * as the network connection manager.
 * */
@RequiredArgsConstructor
public class TunnelServer {

	@NonNull private TunnelConfiguration config;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private MasterServer master;
	
	public ServerBootstrap prepareServer(ChannelInitializer<?> init) {
		return new ServerBootstrap()
		 .group(bossGroup, workerGroup)
		 .channel(NioServerSocketChannel.class)
		 .childHandler(init)
		 .option(ChannelOption.SO_BACKLOG, 128)
		 .childOption(ChannelOption.SO_KEEPALIVE, true);
	}
	
	public void run() throws Exception {
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		
		try {
			// Concept: gate
			if (config.getServerType() == ServerType.MASTER) {
				
				if (config.getProxyMode() == ProxyMode.MULTI_PROXY_TUNNEL) {
					master = new MasterServer(this);
					master.init().sync();
				}
				
				ServerBootstrap gate = prepareServer(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new ClientChannelHandler(DirectServerConnector.newDefault()));
					}
				 });
				
				// Bind and wait for connections
				ChannelFuture f = gate.bind(config.getBindAddress()).sync();
				
				if (f.isSuccess()) {
					CraftTunnel.getLogger().log(Level.INFO, "[{0}] Listening for connections on {1}", new Object[] {
						"TunnelServer", config.getBindAddress()
					});
				} else {
					CraftTunnel.getLogger().log(Level.SEVERE, "Unable to bind connection listener:");
					f.cause().printStackTrace();
				}
				
				// Wait until the server socket closes
				f.channel().closeFuture().sync();
			}
			
			if (config.getServerType() == ServerType.SLAVE) {

				Bootstrap slave = new Bootstrap()
				 .group(workerGroup)
				 .option(ChannelOption.SO_KEEPALIVE, true)
				 .channel(NioSocketChannel.class)
				 .handler(new ChannelInitializer<SocketChannel>() {
					 protected void initChannel(SocketChannel ch) throws Exception {
						 ch.pipeline().addLast(new SlaveHandler());
					 };
				 });
				
				ChannelFuture sl = slave.connect(config.getMasterAddress()).sync();
				sl.channel().closeFuture().sync();
			}
			
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
}
