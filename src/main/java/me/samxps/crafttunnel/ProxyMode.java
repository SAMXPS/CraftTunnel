package me.samxps.crafttunnel;

import me.samxps.crafttunnel.netty.TunnelServer;

public enum ProxyMode {

	/** 
	 * Default proxy mode. On this mode, while {@link TunnelServer} is listening for connections,
	 * each incoming connection will generate a new direct connection from the master server to
	 * the configured remote host and port.
	 * */
	PROXY_ONLY, 
	
	/**
	 * Master and slave mode. On this mode, master {@link TunnelServer} will listen for slave servers
	 * connections, then, once at least one slave server is connected, every incoming connection
	 * will be tunneled to the slave server and the slave server will be responsible for creating
	 * new connections to the remote hosts. 
	 * */
	MULTI_PROXY_TUNNEL;
	
}
