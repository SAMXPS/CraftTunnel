package me.samxps.crafttunnel.server;

import java.util.ArrayList;
import java.util.HashSet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.netty.connector.ServerConnector;
import me.samxps.crafttunnel.netty.multi.MasterHandler;
import me.samxps.crafttunnel.netty.util.ServerBuilder;

@RequiredArgsConstructor
/**
 * 
 * */
public class MasterServer {

	@Getter @NonNull private ProxyServer parent;
	private ArrayList<Channel> slaveChannels;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel channel;
	
	public ChannelFuture init() throws Exception {
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		
		ServerBootstrap b = ServerBuilder.buildServer(bossGroup, workerGroup,
			new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new MasterHandler(MasterServer.this));
				}
			});
		
		ChannelFuture f = b.bind();
		this.channel = f.channel();
		return f;
	}
	
	public Future<?> exit() throws Exception {
		// TODO: join futures
		bossGroup.shutdownGracefully();
		return workerGroup.shutdownGracefully();
	}
	
	public void onCommand(Channel ch, String command) {
		// TODO: Handle commands and messages from SlaveServer.
	}
	
	public void addSlaveChannel(Channel ch) {
		this.slaveChannels.add(ch);
	}
	
	public void removeSlaveChannel(Channel ch) {
		this.slaveChannels.remove(ch);
	}
	
}
