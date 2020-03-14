package me.samxps.crafttunnel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class CraftTunnel {
	
	ServerSocket server;
	Thread incomingThread, connectedThread;
	List<Client> clients = new ArrayList<Client>();
	
	public void init() {
		try {
			server = new ServerSocket(25564);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("I run!!");
		
		incomingThread = new Thread(new Runnable() {
			
			public void run() {
				while (server.isBound() && !server.isClosed()) {
					System.out.println("Im bound");
					try {
						Socket client = server.accept();
						clients.add(new Client(client));

						System.out.println(String.format("accepted %d", clients.size()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		connectedThread = new Thread(new Runnable() {
			
			public void run() {
				while (true) {
					for (Client c : new ArrayList<Client>(clients)) {
						try {
							if (c.socket.isConnected() && !c.socket.isClosed()) {
								if (c.server == null) {
									c.server = new Socket("localhost", 25565);
								} else if (c.server.isClosed()) {
									c.socket.close();
								} else {
									while (c.socket.getInputStream().available() > 0) {
										int data = c.socket.getInputStream().read();
										if (data == -1) c.socket.close();
										c.server.getOutputStream().write(data);
									}
									while (c.server.getInputStream().available() > 0) {
										int data = c.server.getInputStream().read();
										if (data == -1) c.socket.close();
										c.socket.getOutputStream().write(data);
									}
								}
							} else {
								if (c.server.isConnected() || !c.server.isConnected())
									c.server.close();
								clients.remove(c);
							}
						} catch (Exception e) {
							System.out.println("being handled here");
							// TODO: handle exception
							e.printStackTrace();
							try {
								c.socket.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
			}
		});
		
		incomingThread.start();
		connectedThread.start();
	}
	
}
