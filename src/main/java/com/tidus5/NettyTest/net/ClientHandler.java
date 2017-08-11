package com.tidus5.NettyTest.net;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tidus5.NettyTest.client.NettyClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(ServerHandler.class);

	private ExecutorService threadPool = Executors.newCachedThreadPool();

	private NettyClient client;

	public ClientHandler(NettyClient client) {
		this.client = client;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ByteBuffer sendMsg = ByteBuffer.allocate(2);
		sendMsg.putShort((short) (1));
		sendMsg.put(new byte[0]);
		ctx.writeAndFlush(sendMsg.flip());
		System.out.println("send:" + 1);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().eventLoop().schedule(() -> client.doConnect(), 1, TimeUnit.SECONDS);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		threadPool.execute(() -> {
			try {
				if (msg instanceof ByteBuffer) {
					ByteBuffer buf = (ByteBuffer) msg;
					short sip = buf.getShort();

					byte[] data = new byte[buf.remaining()];
					buf.get(data);

					System.out.println(" rec:" + sip);
					
					Thread.sleep(1000L);

					sip++;
					ByteBuffer sendMsg = ByteBuffer.allocate(2 + data.length);
					sendMsg.putShort(sip);
					sendMsg.put(data);
					ctx.writeAndFlush(sendMsg.flip());
					System.out.println("send:" + sip);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof java.io.IOException) {
			logger.error(cause.getMessage());
		} else {
			logger.error(cause.getMessage(), cause);
		}
	}
}
