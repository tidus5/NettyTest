package com.tidus5.NettyTest.client;
import com.tidus5.NettyTest.net.ClientHandler;
import com.tidus5.NettyTest.net.Decoder;
import com.tidus5.NettyTest.net.Encoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        
//        pipeline.addLast(new Decoder());
//        pipeline.addLast(new Encoder());
//        pipeline.addLast(new ClientHandler());
        
    }
}