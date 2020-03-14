package me.samxps.crafttunnel.connection;

import java.io.IOException;
import java.net.Socket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
			// TODO: configurable host and port
			server = new ServerConnection("localhost", 25565);
			server.connect();
		}
	}
	
	public boolean isConnected() {
		return con.isConnected();
	}
	
	public ConnectionWrapper getConnection() {
		return con;
	}
	
}
