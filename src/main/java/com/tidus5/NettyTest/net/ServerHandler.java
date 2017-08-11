package com.tidus5.NettyTest.net;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(ServerHandler.class);

	private ExecutorService threadPool = Executors.newCachedThreadPool();

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
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

					ByteBuffer sendMsg = ByteBuffer.allocate(2 + data.length);
					sendMsg.putShort((short) (sip + 1));
					sendMsg.put(data);
					ctx.writeAndFlush(sendMsg.flip());
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

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
				System.out.println("read idle");
			} else if (event.state() == IdleState.WRITER_IDLE) {
				System.out.println("write idle");
			} else if (event.state() == IdleState.ALL_IDLE) {
				System.out.println("all idle");
			}

			// //0x7002,
			// NetBuffer data =
			// NetBuffer.createNetBuff(SystemControl.instance.RESPONSE_KEEPALIVE,
			// MsgType.KEEP_ALIVE);
			// String account = (String)
			// ctx.channel().attr(AttributeKey.valueOf(Constants.ACCOUNT_KEY)).get();
			// if(account != null){
			//// LogUtil.info(" send hearbeat account :"+account);
			// Context.instance.dealIndexWithSendingMsg(data,
			// Context.instance.getSessionTextId(ctx.channel()));
			// ctx.channel().writeAndFlush(data).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			// }else{
			// //TODO 怎么检测空socket
			// LogUtil.info("heartbeat send failed . no account");
			//// ctx.channel().writeAndFlush(data).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			// }
		}
	}
}
