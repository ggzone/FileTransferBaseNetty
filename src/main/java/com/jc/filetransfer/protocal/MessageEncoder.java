package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.stream.ChunkedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Hunter on 2017/04/14.
 */
public class MessageEncoder extends MessageToMessageEncoder<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageEncoder.class);

    private static int headerLength = 8;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {

        int msgLength = headerLength + msg.type().encodeLength() + msg.encodeLength();
        ByteBuf buf = ctx.alloc().heapBuffer(headerLength);
        buf.writeLong(msgLength);
        msg.type().encode(buf);
        msg.encode(buf);
        out.add(buf);

        if (msg.type() == Message.Type.FileDownloadResponse) {
            FileDownloadResponse fileDownloadResponse = (FileDownloadResponse) msg;
            LOGGER.debug("Send File:{}", fileDownloadResponse.toString());

            //ctx.write只对pipe后面的有效，channel对整个pipe有效
            //ctx.writeAndFlush(defaultFileRegion);
            //out.add(new ChunkedFile(fileDownloadResponse.file));

            if (ctx.pipeline().get("SSLHandler") == null) {
                // SSL not enabled - can use zero-copy file transfer.
                DefaultFileRegion defaultFileRegion = new DefaultFileRegion(fileDownloadResponse.file, 0, fileDownloadResponse.file.length());
                out.add(defaultFileRegion);
                LOGGER.debug("SSL not enabled - can use zero-copy file transfer.");
            } else {
                // SSL enabled - cannot use zero-copy file transfer.
                out.add(new ChunkedFile(fileDownloadResponse.file));
                LOGGER.debug("SSL enabled - cannot use zero-copy file transfer.");
            }


        } else if (msg.type() == Message.Type.FileUploadResponse) {
            FileUploadResponse fileUploadResponse = (FileUploadResponse) msg;
            LOGGER.debug("Upload File:{}", fileUploadResponse.toString());
            if (ctx.pipeline().get("SSLHandler") == null) {
                // SSL not enabled - can use zero-copy file transfer.
                DefaultFileRegion defaultFileRegion = new DefaultFileRegion(fileUploadResponse.file, 0, fileUploadResponse.file.length());
                out.add(defaultFileRegion);
                LOGGER.debug("SSL not enabled - can use zero-copy file transfer.");
            } else {
                // SSL enabled - cannot use zero-copy file transfer.
                out.add(new ChunkedFile(fileUploadResponse.file));
                LOGGER.debug("SSL enabled - cannot use zero-copy file transfer.");
            }
        }

//        }
    }


//    protected long doWriteFileRegion(ChannelHandlerContext ctx,FileRegion region) throws Exception {
//        final long position = region.transfered();
//        return region.transferTo(ctx, position);
//    }
//
//
//    protected void doWrite(FileRegion region) throws Exception {
//        int writeSpinCount = -1;
//
//        boolean done = region.transfered() >= region.count();
//
//        if (!done) {
//            long flushedAmount = 0;
//            if (writeSpinCount == -1) {
//                writeSpinCount = 16;
//            }
//
//            for (int i = writeSpinCount - 1; i >= 0; i--) {
//                long localFlushedAmount = doWriteFileRegion(region);
//                if (localFlushedAmount == 0) {
//                    break;
//                }
//
//                flushedAmount += localFlushedAmount;
//                if (region.transfered() >= region.count()) {
//                    done = true;
//                    break;
//                }
//            }
//        }
//
//    }
//


}
