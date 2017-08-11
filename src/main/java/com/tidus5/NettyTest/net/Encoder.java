package com.tidus5.NettyTest.net;


import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<ByteBuffer> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuffer data, ByteBuf out) throws Exception {
		
		if(data instanceof ByteBuffer){
			ByteBuffer buf = (ByteBuffer) data;
			
			short sip = buf.getShort();
			int len = buf.remaining();
			
			out.writeShort(sip);
			out.writeShort(len);
			
			byte[] array = new byte[buf.remaining()];
			buf.get(array);
			out.writeBytes(array);
			ctx.flush();
		}
	}

}
