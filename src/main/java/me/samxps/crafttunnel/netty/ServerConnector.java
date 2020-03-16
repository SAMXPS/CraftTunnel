package me.samxps.crafttunnel.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;

public interface ServerConnector {

	public ChannelFuture init(Channel clientChannel) throws Exception;
	
	public Future<?> exit() throws Exception;
	
}
