package me.samxps.crafttunnel;

import java.io.IOException;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CraftTunnelLauncher {

	public static void main(String[] args) {
		
		String bindhost = "0.0.0.0";
		int bindport = 25564;
		String svhost = "localhost";
		int svport = 25565;
		
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.acceptsAll( Arrays.asList( "svhost" ), "Sets the remote server host" );
        parser.acceptsAll( Arrays.asList( "svport" ), "Sets the remote server port" );
        parser.acceptsAll( Arrays.asList( "bindhost" ), "Sets the server bind host" );
        parser.acceptsAll( Arrays.asList( "bindport" ), "Sets the server bind port" );

        OptionSet options = parser.parse( args );
		
        if (options.has("svhost")) {
        	svhost = (String) options.valueOf("svhost");
        }
        if (options.has("bindhost")) {
        	bindhost = (String) options.valueOf("svhost");
        }
        if (options.has("svport")) {
        	svport = (int) options.valueOf("svhost");
        }
        if (options.has("bindport")) {
        	bindport = (int) options.valueOf("svhost");
        }
        
		CraftTunnel tunnel = new CraftTunnel(svhost, svport);
		try {
			tunnel.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
