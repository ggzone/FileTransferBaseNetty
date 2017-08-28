package com.jc.filetransfer.protocal;


import com.jc.filetransfer.client.TransportClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by Hunter on 2017/04/14.
 */
public class TransportResponseHandler implements MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportResponseHandler.class);

    private final Queue<StreamCallback> streamCallbacks;

    public TransportResponseHandler() {
        this.streamCallbacks = new ConcurrentLinkedQueue<>();
    }

    public void addStreamCallback(StreamCallback callback) {
        this.streamCallbacks.offer(callback);
    }


    @Override
    public void handler(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileDownloadResponse) {
            FileDownloadResponse fileDownloadResponse = (FileDownloadResponse) msg;
            LOGGER.debug("Receive response:{}.", fileDownloadResponse.toString());
            TransportFrameDecoder frameDecoder = (TransportFrameDecoder) ctx.pipeline().get("FrameDecoder");
            StreamCallback callback = streamCallbacks.poll();
            if (null != callback) {
                if (fileDownloadResponse.byteCount > 0) {
                    try {
                        frameDecoder.setInterceptor(new FileInterceptor(this, fileDownloadResponse.fileId, fileDownloadResponse.byteCount, callback));
                    } catch (Exception e) {
                        LOGGER.error("Error installing stream handler.", e);
                    }
                } else {
                    try {
                        callback.onComplete(fileDownloadResponse.fileId);
                    } catch (IOException e) {
                        LOGGER.warn("Error in stream handler onComplete().", e);
                    }
                }
            } else {
                LOGGER.error("Could not find callback for StreamResponse.");
            }

        } else if (msg instanceof FileDownloadFailure) {

        } else if (msg instanceof FileUploadResponse) {

        } else if (msg instanceof FileUploadFailure) {

        } else if (msg instanceof FileUploadAck) {
            FileUploadAck ack = (FileUploadAck) msg;
            LOGGER.debug("Receive FileUploadAck:" + ack);
            ConcurrentHashMap<String, File> uploadingFiles = TransportClient.uploadingFiles;
            File file = uploadingFiles.remove(ack.fileId);
            if (null == file) {
                throw new IllegalArgumentException("Can't find file information from uploadingFiles");
            }

            final FileUploadResponse fileUploadResponse = new FileUploadResponse(ack.fileId, file.length(), file);
            ctx.writeAndFlush(fileUploadResponse).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    LOGGER.info("Send FileUploadResponse:" + fileUploadResponse);
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }
}
