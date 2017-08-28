package com.jc.filetransfer.client;

import com.jc.filetransfer.protocal.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Hunter on 2017/02/09.
 */
public class TransportClient {
    /**
     * 日志组件
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportClient.class);
    public static ConcurrentHashMap<String, File> uploadingFiles = new ConcurrentHashMap<>();
    TransportResponseHandler responseHandler;
    private Channel channel = null;
    private EventLoopGroup group = null;
    private String host = null;
    private int port = -1;

    public TransportClient(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public void start(final SSLEngine sslEngine) throws InterruptedException, IOException {
        Bootstrap b = new Bootstrap();
        group = new NioEventLoopGroup();

        TransportRequestHandler requestHandler = new TransportRequestHandler();
        responseHandler = new TransportResponseHandler();

        final TransportChannelHandler transportChannelHandler = new TransportChannelHandler(requestHandler, responseHandler);

        b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .option(ChannelOption.SO_TIMEOUT, 1)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //ch.pipeline().addLast("SSLHandler",new SslHandler(sslEngine));
                        ch.pipeline().addLast("ChunkedHandler", new ChunkedWriteHandler());
                        ch.pipeline().addLast("encoder", new MessageEncoder());
                        ch.pipeline().addLast("FrameDecoder", new TransportFrameDecoder());
                        ch.pipeline().addLast("decoder", new MessageDecoder());
                        ch.pipeline().addLast("FileClientHandler", transportChannelHandler);
                    }
                });

        ChannelFuture channelFuture = null;
        try {
            channelFuture = b.connect(host, port).sync();
            LOGGER.info("Server starts，remote server address:" + host + ":" + port);
            channel = channelFuture.channel();

        } catch (InterruptedException e) {
            LOGGER.error("TransportClient connection error:" + e.getMessage());
            throw new InterruptedException("TransportClient connection error:" + e.getMessage());
        }
    }

    public void downloadFile(final String fileId, StreamCallback callback) {
        final FileDownload fileDownload = new FileDownload(fileId);

        responseHandler.addStreamCallback(callback);
        channel.writeAndFlush(fileDownload).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    LOGGER.trace("Send FileDownload {} success.", fileDownload);
                } else {
                    LOGGER.trace("Send FileDownload {} failed.", fileDownload);
                }
            }
        });
    }


    public void uploadFile(final String fileId, final File file) {
        long byteCount = file.length();
        final FileUpload fileUpload = new FileUpload(fileId, byteCount);

        channel.writeAndFlush(fileUpload).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    LOGGER.debug("Send FileUpload {} success.", fileUpload);
                    uploadingFiles.putIfAbsent(fileId, file);
                } else {
                    LOGGER.debug("Send FileUpload {} failed.", fileUpload);
                    uploadingFiles.putIfAbsent(fileId, file);
                }
            }

        });

    }

    private void disconnec() throws InterruptedException {

        channel.closeFuture().sync();
        group.shutdownGracefully();

    }

}
