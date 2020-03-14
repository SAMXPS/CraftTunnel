package me.samxps.crafttunnel.connection;

import java.io.IOException;
import java.net.Socket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.WrappedConnection;

@RequiredArgsConstructor
public class ClientConnection implements WrappedConnection {
	
	private final ConnectionWrapper con;
	@Getter private ServerConnection server;
	protected Thread clientThread;
	
	public ClientConnection(Socket socket) throws IOException {
		this(new ConnectionWrapper(socket));
	}
	
	public void connectToServer() throws IOException {
		if (server == null) {
			CraftTunnel main = CraftTunnel.getInstance();
			server = new ServerConnection(main.getRemoteHost(), main.getRemotePort());
			server.connect();
			// TODO: Send wrapped client IP address to the server
		}
	}
	
	public boolean isConnected() {
		return con.isConnected();
	}
	
	public ConnectionWrapper getConnection() {
		return con;
	}
	
}
