package me.samxps.crafttunnel;

import java.util.logging.Logger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.server.ProxyServer;

@RequiredArgsConstructor
@Getter
public class CraftTunnel {
	
	// private final String host; TODO: configurable local binding
	private final int port;
	private final String remoteHost;
	private final int remotePort;
	@Getter
	private static CraftTunnel instance;
	@Getter
	private static ProxyServer server;
	@Getter
	private static final Logger logger = Logger.getLogger("CraftTunnelLogger");
	
	public void init() throws Exception {
		instance = this;
		server = new ProxyServer(new ProxyConfiguration().setBindPort(port));
		server.run();
	}
	
}
