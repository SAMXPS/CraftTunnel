package me.samxps.crafttunnel;

import java.net.Socket;

public class Client {

	public Socket socket;
	public Socket server;
	
	public Client(Socket socket) {
		this.socket = socket;
	}
	
}
