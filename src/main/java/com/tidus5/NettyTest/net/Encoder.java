package com.tidus5.NettyTest.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<java.nio.ByteBuffer> {

	@Override
	protected void encode(ChannelHandlerContext ctx, java.nio.ByteBuffer data, ByteBuf out) throws Exception {
		
		if(data instanceof java.nio.ByteBuffer){
			java.nio.ByteBuffer buf = (java.nio.ByteBuffer) data;
			
			short sip = buf.getShort();
			int len = buf.remaining();
			
			out.writeShort(sip);
			out.writeShort(len);
			
			byte[] array = new byte[buf.remaining()];
			buf.get(array);
			out.writeBytes(array);
			
		}
	}

}
