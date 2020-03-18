package me.samxps.crafttunnel.server;

import io.netty.channel.Channel;
import me.samxps.crafttunnel.ProxyMode;

/**
 * {@link SlaveServer} will connect to a remote {@link MasterServer} and
 * listen for commands. With commands, the master server can tell this
 * slave server to open connections and serve as an exit point from the
 * tunnel. On the {@link ProxyMode#MULTI_PROXY_TUNNEL} mode, the instance
 * running {@link MasterServer} will serve as the entry point to the tunnel.
 * */
public class SlaveServer {

	public void onCommand(Channel ch, String command) {
		// TODO: Handle commands and messages from SlaveServer.
	}
	
}
