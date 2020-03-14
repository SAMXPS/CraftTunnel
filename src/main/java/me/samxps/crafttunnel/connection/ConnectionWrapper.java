package me.samxps.crafttunnel.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import lombok.Getter;
import lombok.Setter;

public class ConnectionWrapper {
	
	private Socket socket;
	private boolean connected;
	private boolean eof = false;
	private long lastPacketReceived;
	private InputStream input;
	private OutputStream output;
	@Getter @Setter private long timeout = 5000;
	
	public ConnectionWrapper(Socket socket) throws IOException {
		this.socket = socket;
		this.connected = true;
		this.input  = socket.getInputStream();
		this.output = socket.getOutputStream();
		this.lastPacketReceived = System.currentTimeMillis();
	}
	
	public boolean isConnected() {
		return !eof && connected && !hasTimedOut();
	}
	
	public boolean hasTimedOut() {
		return System.currentTimeMillis() - lastPacketReceived > timeout;
	}	
	
	public byte[] readBytes() throws IOException {
		int bytes = input.available();
		
		if (bytes > 0) {
			byte[] data = new byte[bytes];
			int n = input.read(data);
			if (n < 0) eof = true;
			else lastPacketReceived = System.currentTimeMillis();
			return data;
		} 
		
		return null;
	}
	
	public int sendBytes(byte[] data) throws IOException {
		if (data != null && data.length > 0) {
			output.write(data);
			return data.length;
		}
		return 0;
	}
	
	public void close() {
		try {
			socket.close();
		} catch (Exception e) {
			
		}
		this.connected = false;
		this.eof = true;
	}

}
