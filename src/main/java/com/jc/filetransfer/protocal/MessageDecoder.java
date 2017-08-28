package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Created by Hunter on 2017/04/14.
 */
public class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf buf = msg;
        Message.Type msgType = Message.Type.decode(buf);
        switch (msgType) {
            case FileDownload:
                out.add(FileDownload.decode(buf));
                break;
            case FileDownloadResponse:
                out.add(FileDownloadResponse.decode(buf));
                break;
            case FileUpload:
                out.add(FileUpload.decode(buf));
                break;
            case FileUploadAck:
                out.add(FileUploadAck.decode(buf));
                break;
            case FileUploadResponse:
                out.add(FileUploadResponse.decode(buf));
                break;

        }

    }

}
