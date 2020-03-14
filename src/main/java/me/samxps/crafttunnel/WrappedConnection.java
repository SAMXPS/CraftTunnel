package me.samxps.crafttunnel;

import java.io.IOException;

import me.samxps.crafttunnel.connection.ConnectionWrapper;

public interface WrappedConnection {

	public ConnectionWrapper getConnection();
	
	public default byte[] readBytes() throws IOException {
		return getConnection().readBytes();
	}
	
	public default int sendBytes(byte[] data) throws IOException {
		return getConnection().sendBytes(data);
	}
	
}
