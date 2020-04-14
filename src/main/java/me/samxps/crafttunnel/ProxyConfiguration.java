package me.samxps.crafttunnel;

import java.net.InetSocketAddress;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.samxps.crafttunnel.protocol.TransportType;
import me.samxps.crafttunnel.server.ProxyServer;

/**
 * A ProxyConfiguration is used to create {@link ProxyServer} instances.
 * It contains all configuration required, such as hosts and ports to use,
 * what kind of {@link #proxyMode}, {@link #serverType}, and so on.
 * */
@Getter
@Setter
@Accessors(chain = true)
public class ProxyConfiguration implements Cloneable {

	/** The port ProxyEntryPoint should use */
	private int bindPort;
	/** The hostname ProxyEntryPoint should bind */
	private String bindHost = null;
	
	/** Transport layer protocol to be used */
	private TransportType transportType = TransportType.TCP;
	private ProxyMode proxyMode = ProxyMode.PROXY_ONLY;
	private ServerType serverType = ServerType.ENTRY_POINT;
	
	/** Used by ProxyExitPoint to know the entry point (i.e. master) hostname*/
	private String masterHost = null;
	/** Used by ProxyExitPoint to know the entry point (i.e. master) port*/
	private int masterPort = 25881;
	
	/** Should ProxyEntryPoint decode Minecraft packets? */
	private boolean minecraftProtocolOnly = true;
	/** Should ServerConnector send HAProxy header? */ 
	private boolean HAProxyHeaderEnabled = true;
	
	/** What server host should ServerConnector connect to? */
	private String serverHost = "localhost";
	/** What server port should ServerConnector connect to? */
	private int serverPort = 25565;
	
	/**
	 * This method returns the InetSocketAddres that the ProxyEntryPoint
	 * should bind to.
	 * */
	public InetSocketAddress getBindAddress() {
		if (bindHost == null)
			return new InetSocketAddress(bindPort);
		return new InetSocketAddress(bindHost, bindPort);
	}
	
	/**
	 * This method returns the InetSocketAddress of the ProxyEntryPoint.
	 * It is used by ProxyExitPoint.
	 * */
	public InetSocketAddress getMasterAddress() {
		if (masterHost == null)
			return new InetSocketAddress(masterPort);
		return new InetSocketAddress(masterHost, masterPort);
	}
	
	/**
	 * This method returns a combination of the serverHost and serverPort
	 * into a InetSocketAddress.
	 * */
	public InetSocketAddress getServerAddress() {
		return new InetSocketAddress(serverHost, serverPort);
	}
	
}
