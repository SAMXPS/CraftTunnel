package me.samxps.crafttunnel.netty.multi;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import me.samxps.crafttunnel.ProxyConfiguration;
import me.samxps.crafttunnel.netty.HAProxyIdentifier;
import me.samxps.crafttunnel.protocol.minecraft.MinecraftPacket;
import me.samxps.crafttunnel.protocol.multi.MagicPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket;
import me.samxps.crafttunnel.protocol.multi.WrapperPacket.WrapperPacketType;

@RequiredArgsConstructor
public class ProxyExitPointHandler extends ChannelInboundHandlerAdapter {

	private final ProxyConfiguration config;
	private HashMap<InetSocketAddress, Channel> servers = new HashMap<InetSocketAddress, Channel>();
	private HashMap<InetSocketAddress, Queue<Object>> queue = new HashMap<InetSocketAddress, Queue<Object>>();
	private Channel proxyChannel;
	private EventLoopGroup connectorEventLoop;
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		connectorEventLoop = new NioEventLoopGroup();
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		connectorEventLoop.shutdownGracefully();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Sends magic packet to proxy entrypoint
		proxyChannel = ctx.channel();
		ctx.channel().write(new MagicPacket().toMinecraftPacket());
		ctx.channel().flush();
		
		super.channelActive(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof MinecraftPacket) {
			MinecraftPacket p = (MinecraftPacket) msg;
			
			if (p.getPacketID() == WrapperPacket.getPacketID()) {
				handleWrapper(WrapperPacket.decode(p));
			} else {
				// discard
			}
		}

		// TODO: release unknown messages ?
	}
	
	private void handleWrapper(WrapperPacket w) throws Exception {
		// TODO: map methods, direct access?
		InetSocketAddress clientAddress = w.getClientAddress();
		
		if (w.getType() == WrapperPacketType.CONNECTION_START) {
			InetSocketAddress serverAddress = config.getServerAddress();
			
			Bootstrap b = new Bootstrap();
			b.group(connectorEventLoop)
			 .option(ChannelOption.SO_KEEPALIVE, true)
			 .channel(config.getTransportType().getChannelClass())
			 .handler(new ChannelInitializer<SocketChannel>() {
				 protected void initChannel(SocketChannel ch) throws Exception {
					 if (config.isHAProxyHeaderEnabled()) {
						 ch.pipeline().addLast(new HAProxyIdentifier(clientAddress, serverAddress));
					 }
					 ch.pipeline().addLast(
						new WrapperInboundHandler(clientAddress, proxyChannel)
					 );

				 };
			});
			
			ChannelFuture f = b.connect(config.getServerHost(), config.getServerPort());
			servers.put(clientAddress, f.channel());
			
			f.addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						future.channel().eventLoop().execute(new Runnable() {
							
							@Override
							public void run() {
								if (queue.containsKey(clientAddress)) {
									Queue<Object> queue = ProxyExitPointHandler.this.queue.get(clientAddress);
									while (!queue.isEmpty()) {
										future.channel().write(queue.poll());
										future.channel().flush();
									}
								}
								queue.remove(clientAddress);								
							}
						});
					} else {
						queue.remove(clientAddress);	
					}
				}
			});
			
			f.channel().closeFuture().addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					servers.remove(clientAddress);
				}
			});

		} else {
			Channel remote = servers.get(clientAddress);
			
			if (remote == null) return;
			
			if (w.getType() == WrapperPacketType.CONNECTION_CLOSE) {
				remote.close();
			} else if (w.getType() == WrapperPacketType.DATA_BYTES) {
				sendData(clientAddress, remote, w.getData());
			} else if (w.getType() == WrapperPacketType.DATA_PACKET) {
				// Should we decode the packet ?
				sendData(clientAddress, remote, w.getData());
			}
		}
	}
	
	private void sendData(InetSocketAddress client, Channel server, Object data) {
		if (server.isActive()) {
			server.write(data);
			server.flush();
		} else {
			if (!queue.containsKey(client)) {
				queue.put(client, new LinkedBlockingQueue<Object>());
			}
			queue.get(client).add(data);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}
