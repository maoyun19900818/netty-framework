package com.netty.core.server;

import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP server that sends back the content of the received HTTP request in a
 * pretty plaintext form.
 */
public class HttpServer {
	
	Logger log = LoggerFactory.getLogger(HttpServer.class);
	
	
	private final int port;

	public HttpServer(int port) {
		this.port = port;
	}

	public void run() throws Exception {
		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup,workGroup)
							.channel(NioServerSocketChannel.class)
							.childHandler(new ChannelInitializer<SocketChannel>() {
								
								@Override
				                 public void initChannel(SocketChannel ch) throws Exception {
									// Uncomment the following line if you want HTTPS
//									SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
//									engine.setUseClientMode(false);
//									ch.pipeline().addLast("ssl", new SslHandler(engine));
									
									ch.pipeline().addLast(new HttpRequestDecoder());
									
									ch.pipeline().addLast(new HttpResponseEncoder());
									
									ch.pipeline().addLast(new ChunkedWriteHandler());
									
				                    ch.pipeline().addLast(new DiscardServerHandler());
				
				                 }

							}).option(ChannelOption.SO_BACKLOG, 128)
							.childOption(ChannelOption.SO_KEEPALIVE, true);
			
			// Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
		}finally{
			workGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {
		int port;
		
		if(args.length > 0){
			port = Integer.valueOf(args[0]);
		}else{
			port = 8080;
		}
		new HttpServer(port).run();;
	}

}