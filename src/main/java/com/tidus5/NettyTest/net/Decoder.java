package com.tidus5.NettyTest.net;

import java.nio.ByteBuffer;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class Decoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf data, List<Object> in) throws Exception {
		while(data.readableBytes() > 4){
			data.markReaderIndex();	
			short sip = data.readShort();
			short len = data.readShort();
			if(data.readableBytes() >= len){
				
				byte[] array = new byte[len];
				data.readBytes(array);
				
				ByteBuffer buf = ByteBuffer.allocate(len+2);
				buf.putShort(sip);
				buf.put(array);
				in.add(buf.flip());
				data.discardReadBytes();
			}else{
				data.resetReaderIndex();
				return;
			}
		}
	}

}
