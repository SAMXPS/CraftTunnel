package me.samxps.crafttunnel.netty.multi;

import java.util.HashSet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.netty.ServerConnector;
import me.samxps.crafttunnel.netty.TunnelServer;

@RequiredArgsConstructor
public class MasterServer {

	@Getter
	private TunnelServer parent;
	private HashSet<MasterHandler> handlers = new HashSet<MasterHandler>();
	
	public ChannelFuture init() throws Exception {
		
		ServerBootstrap b = parent.prepareServer(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new MasterHandler(MasterServer.this));
			}
		});
		
		return b.bind();
	}
	
	public MasterHandler getAHandler() {
		for (MasterHandler h : handlers)
			return h;
	    return null;
	}
	
	protected void addHandler(MasterHandler handler) {
		handlers.add(handler);
	}
	
	protected void removeHandler(MasterHandler handler) {
		handlers.remove(handler);
	}
	
}
