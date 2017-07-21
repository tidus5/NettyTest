package com.tidus5.NettyTest.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

public class ConnectionListener implements ChannelFutureListener {

	private NettyClient client;

	public ConnectionListener(NettyClient client) {
		this.client = client;
	}

	@Override

	public void operationComplete(ChannelFuture channelFuture) throws Exception {
		if (!channelFuture.isSuccess()) {
			System.out.println("Reconnect");
			final EventLoop loop = channelFuture.channel().eventLoop();
			loop.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						client.init();
						client.run();
					} catch (InterruptedException | IOException e) {
						e.printStackTrace();
					}
				}
			}, 1L, TimeUnit.SECONDS);

		}

	}

}