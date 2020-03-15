package me.samxps.crafttunnel;

import java.lang.System.Logger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.netty.TunnelServer;

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
	private static TunnelServer server;
	@Getter
	private static final Logger logger = System.getLogger("CraftTunnelLogger");
	
	public void init() throws Exception {
		instance = this;
		server = new TunnelServer(port);
		server.run();
	}
	
}
