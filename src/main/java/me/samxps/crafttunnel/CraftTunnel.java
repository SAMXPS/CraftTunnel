package me.samxps.crafttunnel;

import java.io.IOException;
import java.lang.System.Logger;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.connection.ConnectionListener;
import me.samxps.crafttunnel.linker.ClientServerLinker;

@RequiredArgsConstructor
@Getter
public class CraftTunnel {
	
	private final String remoteHost;
	private final int remotePort;
	@Getter
	private static CraftTunnel instance;
	private ConnectionListener listener;
	private ClientServerLinker linker;
	@Getter
	private static final Logger logger = System.getLogger("CraftTunnelLogger");
	
	public void init() throws IOException {
		instance = this;
		linker = new ClientServerLinker();
		listener = new ConnectionListener(linker, 25564);
		listener.bind();
		listener.listen();
	}
	
	
	
	
}
