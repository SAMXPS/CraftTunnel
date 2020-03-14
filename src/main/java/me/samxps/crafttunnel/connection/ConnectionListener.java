package me.samxps.crafttunnel.connection;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.IncomingConnectionHandler;

@RequiredArgsConstructor
public class ConnectionListener {

	private final IncomingConnectionHandler handler;
	private final String host;
	private final int port;
	private ServerSocket socket;
	private Thread listener;
	private boolean listening;
	
	public ConnectionListener(IncomingConnectionHandler handler, int port) {
		this(handler, "localhost", port);
	}
	
	public void bind() throws IOException {
		if (socket == null) {
			socket = new ServerSocket(port, 50, InetAddress.getByName(host));
		}
		CraftTunnel.getLogger().log(Level.INFO, "Server bind on " + host + ":" + port);
	}
	
	public void listen() {
		if (listener == null) {
			listener = new Thread(new Runnable() {
				
				public void run() {
					loop();
				}
			});
		}
		listening = true;
		listener.start();

		CraftTunnel.getLogger().log(Level.INFO, "Listening on " + host + ":" + port);
	}
	
	private void loop() {
		while (listening) {
			try {
				handler.handle(socket.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
