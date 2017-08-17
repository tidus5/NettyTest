package com.tidus5.NettyTest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tidus5.NettyTest.net.ClientHandler;
import com.tidus5.NettyTest.net.Decoder;
import com.tidus5.NettyTest.net.Encoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

	private static Logger logger = LoggerFactory.getLogger(ClientHandler.class);

	public String host = "127.0.0.1";
	public int port = 7878;

	private NioEventLoopGroup group;
	private Bootstrap bootstrap;
	private Channel ch;
	private boolean started;
	private Map<String, Channel> channelsMap = new HashMap<>();
	public static ExecutorService threadPool = Executors.newCachedThreadPool();
	public static ScheduledExecutorService scheduleThreadPool = Executors.newScheduledThreadPool(20);

	public NettyClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void init() {
		started = true;
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(group);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new Decoder());
				pipeline.addLast(new Encoder());
				pipeline.addLast(new ClientHandler(NettyClient.this));
			}
		});

	}

	public void close() {
		started = false;
		group.shutdownGracefully();
	}

	public void run() throws InterruptedException, IOException {
		init();
		doConnect();
	}

	public void doConnect() {
		if (!started)
			return;
		ChannelFuture future = bootstrap.connect(host, port);
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
//					System.out.println("connect success!" + host + ":" + port);
					addChannels(f.channel());
				} else {
//					System.out.println("connecting..." + host + ":" + port);
					f.channel().eventLoop().schedule(() -> doConnect(), 1, TimeUnit.SECONDS);
				}
			}
		});
	}

	public Map<String, Channel> getChannelsMap() {
		return channelsMap;
	}

	public void addChannels(Channel channel) {
		channelsMap.put(channel.id().asShortText(), channel);
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
		NettyClient client = new NettyClient("127.0.0.1", 7878);
		client.init();
		for (int i = 0; i < 3000; i++)
			client.doConnect();
		scheduleThreadPool.scheduleAtFixedRate(() -> logger.info("channel size:" + client.getChannelsMap().size()), 1,
				5, TimeUnit.SECONDS);

		scheduleThreadPool.scheduleAtFixedRate(() -> {
			for (Channel channel : client.getChannelsMap().values()) {
				short sip = 1;
				ByteBuffer sendMsg = ByteBuffer.allocate(2);
				sendMsg.putShort(sip);
				byte[] data = new byte[0];
				sendMsg.put(data);
				channel.attr(ClientHandler.LAST_SEND_TIME).set(System.currentTimeMillis());
				channel.writeAndFlush(sendMsg.flip());
			}
		}, 1, 3, TimeUnit.SECONDS);
	}
}