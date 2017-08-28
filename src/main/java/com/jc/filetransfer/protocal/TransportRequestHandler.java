package com.jc.filetransfer.protocal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by Hunter on 2017/04/14.
 */
public class TransportRequestHandler implements MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportRequestHandler.class);


    @Override
    public void handler(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileDownload) {
            FileDownload fileDownload = (FileDownload) msg;
            String fileId = fileDownload.fileId;
            LOGGER.debug("Receive download request:{}", fileDownload.toString());
            File file = new File("E:\\winscp.rar");
            if (file.exists()) {
                response(new FileDownloadResponse(fileId, file.length(), file), ctx.channel());
            } else {
                response(new FileDownloadFailure(fileId, "File not found"), ctx.channel());
            }

        }
        if (msg instanceof FileUpload) {
            FileUpload fileUpload = (FileUpload) msg;
            LOGGER.debug("Receive upload request:{}", fileUpload.toString());
            response(new FileUploadAck(fileUpload.fileId, true, ""), ctx.channel());

        }
        if (msg instanceof FileUploadResponse) {
            FileUploadResponse fileUploadResponse = (FileUploadResponse) msg;
            LOGGER.debug("Receive upload request:{}", fileUploadResponse.toString());
            TransportFrameDecoder frameDecoder = (TransportFrameDecoder) ctx.pipeline().get("FrameDecoder");

            RandomAccessFile raf = null;
            try {
                new File(fileUploadResponse.fileId).mkdir();
                raf = new RandomAccessFile(fileUploadResponse.fileId + SystemUtils.FILE_SEPARATOR + "out.zip", "rw");
                LOGGER.info("Upload file path:" + new File(fileUploadResponse.fileId + SystemUtils.FILE_SEPARATOR + "out.zip").getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            final RandomAccessFile finalRaf = raf;
            StreamCallback callback = new StreamCallback() {

                @Override
                public void onData(String streamId, ByteBuffer buf) throws IOException {
                    int remaining = buf.remaining();
                    if (remaining > 0) {
                        byte[] bytes = new byte[remaining];
                        buf.get(bytes);
                        finalRaf.write(bytes);
                    }
                }

                @Override
                public void onComplete(String streamId) throws IOException {
                    finalRaf.close();
                    System.out.println("File send finished!");
                }

                @Override
                public void onFailure(String streamId, Throwable cause) throws IOException {
                    finalRaf.close();
                    System.out.println("File send failed!");
                }
            };
            frameDecoder.setInterceptor(new FileInterceptor(this, fileUploadResponse.fileId, fileUploadResponse.byteCount, callback));
        }
    }

    public void response(final Encodable msg, final Channel channel) {
        final SocketAddress socketAddress = channel.remoteAddress();
        channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    LOGGER.trace("Sent result {} to client {}", msg, socketAddress);
                } else {
                    LOGGER.error(String.format("Error sending result %s to %s; closing connection",
                            msg, socketAddress), channelFuture.cause());
                }
            }
        });
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }
}
