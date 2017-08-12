package com.tidus5.NettyTest.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tidus5.NettyTest.net.Decoder;
import com.tidus5.NettyTest.net.Encoder;
import com.tidus5.NettyTest.net.ServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

public class NettyServer {

	private ServerBootstrap bootstrap;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;
	private ChannelFuture serverChannelFuture;
	
	public static ExecutorService threadPool = Executors.newCachedThreadPool();

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

		// option These options will be set on the Server Channel when bind() or
		// connect() method is called. This channel is one per server.
		// parameters apply to the server socket (Server channel) that is
		// listening for connections
		// childOption which gets created once the serverChannel accepts a
		// client connection. This channel is per client (or per client socket).
		// apply to the socket that gets created once the connection is accepted
		// by the server socket.

		bootstrap.option(ChannelOption.SO_BACKLOG, 2500).option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000).childOption(ChannelOption.SO_RCVBUF, 1024 * 32)
				.childOption(ChannelOption.SO_SNDBUF, 1024 * 32).childOption(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.SO_KEEPALIVE, true);

		// 加入业务控制器，这里是加入一个初始化类，其中包含了很多业务控制器
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
				pipeline.addLast(new Decoder());
				pipeline.addLast(new Encoder());
				pipeline.addLast(new ServerHandler());
			}
		});

	}

	public void startServer() throws InterruptedException {
		startServer(portNumber);
	}

	public void startServer(int port) throws InterruptedException {

		initBootstrap();

		// 服务器绑定端口监听
		ChannelFuture f = bootstrap.bind(port).sync();
		System.out.println(" server started.");
		// 监听服务器关闭监听
		serverChannelFuture = f;

	}

	public void addConsoleSendThread() {
		Thread thread = new Thread() { // 读取控制台输入
			@Override
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					String line = null;
					while ((line = br.readLine()) != null) {
						if (line.equals("close")) {
							System.out.println("close .. will exit ....");
							System.exit(0);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("application will exit ....");
				try {
					serverChannelFuture.channel().close().sync();
					
					int cd = 5;
					while (cd-- > 0) {
						try {
							Thread.sleep(1000L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println(" waiting to shutdown " + cd);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// Netty优雅退出
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
					threadPool.shutdown();
				}
			}
		});
	}

	public static void main(String args[]) throws InterruptedException {
		NettyServer server = new NettyServer();
		server.startServer();
		server.addConsoleSendThread();
	}
}