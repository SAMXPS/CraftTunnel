package me.samxps.crafttunnel.protocol;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Supported transport layer protocols on TCP/IP stack.
 * This enum also provide respective classes for server and normal channels.
 * */
@AllArgsConstructor
@Getter
public enum TransportType {

	TCP(NioServerSocketChannel.class, NioSocketChannel.class), 
	UDP(NioDatagramChannel.class, NioDatagramChannel.class);
	
	private Class<? extends Channel> serverChannelClass;
	private Class<? extends Channel> channelClass;
	
}
