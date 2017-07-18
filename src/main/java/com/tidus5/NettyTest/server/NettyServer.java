package com.tidus5.NettyTest.server;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

public class NettyServer {

	private ServerBootstrap bootstrap;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;
	/**
	 * 服务端监听的端口地址
	 */
	private static final int portNumber = 7878;

	public void initBootstrap() throws InterruptedException {
		// 开启两个事件循环组，事件循环组会自动构建EventLoop，服务器一般开启两个，提高效率
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		ResourceLeakDetector.setLevel(Level.SIMPLE);

		// Netty的引导类，用于简化开发
		bootstrap = new ServerBootstrap();
		// 把事件循环组加入引导程序
		bootstrap.group(bossGroup, workerGroup);
		// 开启socket
		bootstrap.channel(NioServerSocketChannel.class);
		
		bootstrap.option(ChannelOption.SO_BACKLOG, 2500)
		.option(ChannelOption.TCP_NODELAY, true)
		.option(ChannelOption.SO_REUSEADDR, true)
		.option(ChannelOption.SO_RCVBUF, 1024*256)
        .option(ChannelOption.SO_SNDBUF, 1024*256)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
		.childOption(ChannelOption.SO_KEEPALIVE, true);
		
		// 加入业务控制器，这里是加入一个初始化类，其中包含了很多业务控制器
		bootstrap.childHandler(new ServerInitializer());
		

	}
	
	public void startServer() throws InterruptedException {
		startServer(portNumber);
	}

	public void startServer(int port) throws InterruptedException {
		try {
			
			initBootstrap();
			
			// 服务器绑定端口监听
			ChannelFuture f = bootstrap.bind(port).sync();
			System.out.println(" server started.");
			// 监听服务器关闭监听
			f.channel().closeFuture().sync();
			// 可以简写为
			/* b.bind(portNumber).sync().channel().closeFuture().sync(); */
		} finally {
			// Netty优雅退出
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static void main(String args[]) throws InterruptedException{
		new NettyServer().startServer();
	}
}