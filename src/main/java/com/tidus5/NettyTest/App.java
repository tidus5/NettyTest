package com.tidus5.NettyTest;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tidus5.NettyTest.client.NettyClient;
import com.tidus5.NettyTest.server.NettyServer;

/**
 * Hello world!
 *
 */
public class App {

	final static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws InterruptedException {
		new Thread(() -> {
			try {
				new NettyServer().startServer();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		
		Thread.sleep(1000L);
		
//		new Thread(() -> {
//			try {
//				new NettyClient().run();
//			} catch (InterruptedException | IOException e) {
//				e.printStackTrace();
//			}
//		}).start();
		System.out.println("Hello World!");
		logger.info("OK");
	}
}
