package com.jc.filetransfer.server;


import com.jc.filetransfer.client.SSLFactory;
import com.jc.filetransfer.protocal.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.util.Properties;


/**
 * Created by Hunter on 2017/02/08.
 */
public class FileServer {
    public static final String fileBase;
    public static final String storeFile;
    public static final String storePass;
    public static final String keyPass;
    /**
     * 日志组件
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileServer.class);

    static {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("E:\\bdproxy-parent\\bdproxy-filetransfer\\src\\main\\resources\\SSLServer.properties"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        storeFile = props.getProperty("ssl.keystore");
        storePass = props.getProperty("ssl.storepass");
        keyPass = props.getProperty("ssl.keypass");
        fileBase = props.getProperty("file.base.dir");
    }

    /**
     * 监听端口
     */
    private int listenPort = 20001;

    public FileServer(int listenPort) {
        this.listenPort = listenPort;
    }

    public static void main(String[] args) throws Exception {
        new FileServer(10010).start();
    }

    /**
     * 启动文件服务器
     */
    public void start() throws Exception {


        final SSLEngine sslEngine = SSLFactory.createSSLEngine(storeFile, storePass, keyPass, false);


        TransportRequestHandler transportRequestHandler = new TransportRequestHandler();
        TransportResponseHandler transportResponseHandler = new TransportResponseHandler();

        final TransportChannelHandler transportChannelHandler = new TransportChannelHandler(transportRequestHandler, transportResponseHandler);


        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .option(ChannelOption.SO_TIMEOUT, 1)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //ch.pipeline().addLast("SSLHandler",new SslHandler(sslEngine));
                            ch.pipeline().addLast("ChunkedHandler", new ChunkedWriteHandler());
                            ch.pipeline().addLast("encoder", new MessageEncoder());
                            ch.pipeline().addLast("FrameDecoder", new TransportFrameDecoder());
                            ch.pipeline().addLast("decoder", new MessageDecoder());
                            ch.pipeline().addLast("handler", transportChannelHandler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture f = bootstrap.bind(listenPort).sync();
            LOGGER.info("Server started,listen port:" + listenPort);
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            LOGGER.error("Netty not start：", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }


}
