package me.samxps.crafttunnel.connection;

import java.io.IOException;

public interface WrappedConnection {

	public ConnectionWrapper getConnection();
	
	public default byte[] readBytes() throws IOException {
		return getConnection().readBytes();
	}
	
	public default int sendBytes(byte[] data) throws IOException {
		return getConnection().sendBytes(data);
	}
	
}
