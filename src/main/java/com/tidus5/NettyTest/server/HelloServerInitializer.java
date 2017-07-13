package com.tidus5.NettyTest.server;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

//继承Netty提供的初始化类，只要复写其中的方法就可以了
public class HelloServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
       //开启传输通道，这个通道的作用就是管理控制器，形成一个责任链式管理
        ChannelPipeline pipeline = ch.pipeline();

        // 以("\n")为结尾分割的 解码器
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));

        // 字符串解码 和 编码
        pipeline.addLast("decoder", new StringDecoder()); 
        pipeline.addLast("encoder", new StringEncoder());

        // 加入自定义的Handler
        pipeline.addLast("handler", new HelloServerHandler());
        //初始化类一般都是先加入编码解码器来解读传输来的消息，然后加入自定义类来处理业务逻辑
    }
}