package com.tidus5.NettyTest.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HelloServer {

	/**
	 * 服务端监听的端口地址
	 */
	private static final int portNumber = 7878;

	public static void main(String[] args) throws InterruptedException {
		// 开启两个事件循环组，事件循环组会自动构建EventLoop，服务器一般开启两个，提高效率
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			// Netty的引导类，用于简化开发
			ServerBootstrap b = new ServerBootstrap();
			// 把事件循环组加入引导程序
			b.group(bossGroup, workerGroup);
			// 开启socket
			b.channel(NioServerSocketChannel.class);
			// 加入业务控制器，这里是加入一个初始化类，其中包含了很多业务控制器
			b.childHandler(new HelloServerInitializer());

			// 服务器绑定端口监听
			ChannelFuture f = b.bind(portNumber).sync();
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
}