package com.tidus5.NettyTest.net;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(ServerHandler.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("connected to server.");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if(msg instanceof ByteBuffer){
			ByteBuffer buf = (ByteBuffer) msg;
			short sip = buf.getShort();

			byte[] data = new byte[buf.remaining()];
			buf.get(data);
			
			System.out.println("server say:" + new String(data, Charset.forName("UTF-8")));

//			ByteBuffer sendMsg = ByteBuffer.allocate(8);
//			sendMsg.putShort(sip);
//			sendMsg.put("Server recieved:".getBytes("UTF-8"));
//			sendMsg.put(data);
//			ctx.writeAndFlush(sendMsg);
		}
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if(cause instanceof java.io.IOException){
			logger.error(cause.getMessage());
		}else{
			logger.error(cause.getMessage(),cause);
		}
	}
}
