package com.tidus5.NettyTest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.tidus5.NettyTest.net.ClientHandler;
import com.tidus5.NettyTest.net.Decoder;
import com.tidus5.NettyTest.net.Encoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

	public static String host = "127.0.0.1";
	public static int port = 7878;

	private NioEventLoopGroup group;
	private Channel ch;
	private Bootstrap bootstrap;
	private boolean started;

	public void createBoostrap() {
		if (bootstrap == null)
			bootstrap = new Bootstrap();

		final ClientHandler handler = new ClientHandler(this);
		group = new NioEventLoopGroup();
		bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(new Decoder());
						pipeline.addLast(new Encoder());
						pipeline.addLast(handler);
					}
				});
		return;
	}

	public void run() throws InterruptedException, IOException {
		createBoostrap();
		run(host, port);
	}

	public void run(String serverHost, int port) throws InterruptedException, IOException {
		started = true;
		ChannelFuture closeFuture = null;
		try {
			ch = bootstrap.connect(host, port).sync().addListener(new ConnectionListener(this)).channel();
			closeFuture = ch.closeFuture().sync();
		} finally {
			if (started) {
				if (closeFuture != null && closeFuture.isSuccess()) {
					createBoostrap();
					bootstrap.connect(host, port);
				}
			} else {
				group.shutdownGracefully();
			}
		}

	}

	public void addConsoleSendThread() {
		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					while (true) {
						String line = in.readLine();
						if (line == null)
							continue;
						byte[] data = line.getBytes(Charset.forName("UTF-8"));

						ByteBuffer buf = ByteBuffer.allocate(data.length + 2);
						buf.putShort((short) 0);
						buf.put(data);
						ch.writeAndFlush(buf.flip());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void main(String args[]) throws InterruptedException, IOException {
		new NettyClient().run();
	}
}