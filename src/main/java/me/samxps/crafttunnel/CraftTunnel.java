package me.samxps.crafttunnel;

import java.io.IOException;
import java.lang.System.Logger;

import lombok.Getter;
import me.samxps.crafttunnel.connection.ClientServerLinker;
import me.samxps.crafttunnel.connection.ConnectionListener;

@Getter
public class CraftTunnel {
	
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
