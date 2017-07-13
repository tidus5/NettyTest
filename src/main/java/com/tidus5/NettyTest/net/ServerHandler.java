package com.tidus5.NettyTest.net;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(ServerHandler.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("connedtion active");
		
		
		String str = "Server welcomes.";
		byte[] array = str.getBytes(Charset.forName("UTF-8"));
		
		ByteBuffer sendMsg = ByteBuffer.allocate(array.length+2);
		sendMsg.putShort((short) 0);
		sendMsg.put(array);
		ctx.writeAndFlush(sendMsg.flip());
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
			
			System.out.println("client says:"+new String(data,Charset.forName("UTF-8")));

			
			byte[] array = "Server recieved-".getBytes("UTF-8");
			ByteBuffer sendMsg = ByteBuffer.allocate(array.length+data.length+2);
			sendMsg.putShort(sip);
			sendMsg.put(array);
			sendMsg.put(data);
			ctx.writeAndFlush(sendMsg.flip());
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
