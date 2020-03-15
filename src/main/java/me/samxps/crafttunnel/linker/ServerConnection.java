package me.samxps.crafttunnel.linker;

import java.io.IOException;
import java.net.Socket;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.connection.ConnectionWrapper;
import me.samxps.crafttunnel.connection.WrappedConnection;

@RequiredArgsConstructor
public class ServerConnection implements WrappedConnection {

	private final String host;
	private final int port;
	private ConnectionWrapper con;
	
	public boolean isConnected() {
		return con != null && con.isConnected();
	}
	
	public boolean connect() throws IOException {
		if (con != null) {
			con.close();
		}
		con = new ConnectionWrapper(new Socket(host, port));
		return true;
	}
	
	public ConnectionWrapper getConnection() {
		return con;
	}
	
}
