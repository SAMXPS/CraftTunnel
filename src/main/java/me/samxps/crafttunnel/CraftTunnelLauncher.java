package me.samxps.crafttunnel;

import java.io.IOException;

public class CraftTunnelLauncher {

	public static void main(String[] args) {
		CraftTunnel tunnel = new CraftTunnel();
		try {
			tunnel.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
