package me.samxps.crafttunnel.server;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;

public interface Server {

	public default void run() throws Exception {
		init().channel().closeFuture().sync();
		exit().sync();
	}
	
	public ChannelFuture init() throws Exception;
	
	public Future<?> exit() throws Exception;
	
}
