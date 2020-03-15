package me.samxps.crafttunnel.connection;

import java.net.Socket;

public interface IncomingConnectionHandler {

	public void handle(Socket socket);
	
}
