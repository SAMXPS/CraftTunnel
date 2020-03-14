package me.samxps.crafttunnel.connection;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.sql.Savepoint;
import java.util.LinkedList;
import java.util.List;

import me.samxps.crafttunnel.CraftTunnel;
import me.samxps.crafttunnel.IncomingConnectionHandler;

public class ClientServerLinker implements IncomingConnectionHandler {

	private List<ClientConnection> clients = new LinkedList<ClientConnection>();
	
	private void safelyCloseSocket(Socket socket) {
		try {
			socket.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public synchronized void closeClientConnection(ClientConnection client) {
		clients.remove(client);
		
		try {
			client.getConnection().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			client.getServer().getConnection().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void handle(Socket socket) {
		CraftTunnel.getLogger().log(Level.INFO, "Initial handler " + clients.size());
		try {
			final ClientConnection client = new ClientConnection(socket);
			clients.add(client);
			client.clientThread = new Thread(new Runnable() {
				public void run() {
					loop(client);
				}
			});
			client.clientThread.start();
		} catch (IOException e) {
			safelyCloseSocket(socket);
			e.printStackTrace();
		}
	}
		
	private void loop(ClientConnection client) {
		try {
			client.connectToServer();
			ServerConnection server = client.getServer();
			long loopst = System.currentTimeMillis();
			int bytes = 0;
			
			// TODO: Improve CPU usage performance on this loop
			// without sacrificing latency
			while (client.isConnected() && server.isConnected()) {
				bytes += server.sendBytes(client.readBytes());
				bytes += client.sendBytes(server.readBytes());
				if (System.currentTimeMillis() - loopst > 5) {
					if (bytes == 0) {
						Thread.sleep(5);
					}
					bytes = 0;
					loopst = System.currentTimeMillis();
				}
			}
			
		} catch (IOException e) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeClientConnection(client);
	}
	
}
