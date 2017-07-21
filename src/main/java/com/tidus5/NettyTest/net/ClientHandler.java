package com.tidus5.NettyTest.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tidus5.NettyTest.client.NettyClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;

public class ClientHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(ServerHandler.class);

	private NettyClient client;

	public ClientHandler(NettyClient client) {
		this.client = client;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("connected to server.");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().eventLoop().schedule(() -> client.doConnect(), 1, TimeUnit.SECONDS);
//		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof ByteBuffer) {
			ByteBuffer buf = (ByteBuffer) msg;
			short sip = buf.getShort();

			byte[] data = new byte[buf.remaining()];
			buf.get(data);

			System.out.println("server say:" + new String(data, Charset.forName("UTF-8")));

			// ByteBuffer sendMsg = ByteBuffer.allocate(8);
			// sendMsg.putShort(sip);
			// sendMsg.put("Server recieved:".getBytes("UTF-8"));
			// sendMsg.put(data);
			// ctx.writeAndFlush(sendMsg);
		}

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
