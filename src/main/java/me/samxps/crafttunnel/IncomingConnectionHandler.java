package me.samxps.crafttunnel;

import java.net.Socket;

public interface IncomingConnectionHandler {

	public void handle(Socket socket);
	
}
