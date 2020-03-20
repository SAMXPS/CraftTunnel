package me.samxps.crafttunnel.server;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;

public interface Server {

	/**
	 * This method will initiate the server and sync the current thread to the
	 * server's lifecycle, that is, the current thread will be locked until
	 * the server is closed. 
	 * */
	public default void run() throws Exception {
		init().channel().closeFuture().sync();
		exit().sync();
	}
	
	public ChannelFuture init() throws Exception;
	
	public Future<?> exit() throws Exception;
	
}
