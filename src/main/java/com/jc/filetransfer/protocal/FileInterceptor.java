package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

/**
 * Created by Hunter on 2017/04/18.
 */
public class FileInterceptor implements TransportFrameDecoder.Interceptor {
    private final MessageHandler handler;
    private final String fieldId;
    private final long byteCount;
    private long bytesRead;
    private StreamCallback callback;

    public FileInterceptor(
            MessageHandler handler,
            String streamId,
            long byteCount,
            StreamCallback callback) {
        this.handler = handler;
        this.fieldId = streamId;
        this.byteCount = byteCount;
        this.callback = callback;
        this.bytesRead = 0;
    }


    @Override
    public boolean handle(ByteBuf data) throws Exception {
        int toRead = (int) Math.min(data.readableBytes(), byteCount - bytesRead);
        ByteBuffer nioBuffer = data.readSlice(toRead).nioBuffer();

        int available = nioBuffer.remaining();
        callback.onData(fieldId, nioBuffer);
        bytesRead += available;
        if (bytesRead > byteCount) {
            RuntimeException re = new IllegalStateException(String.format(
                    "Read too many bytes? Expected %d, but read %d.", byteCount, bytesRead));
            callback.onFailure(fieldId, re);
            throw re;
        } else if (bytesRead == byteCount) {

            callback.onComplete(fieldId);
        }
        return bytesRead != byteCount;
    }

    @Override
    public void exceptionCaught(Throwable cause) throws Exception {
        callback.onFailure(fieldId, cause);
    }

    @Override
    public void channelInactive() throws Exception {
        callback.onFailure(fieldId, new ClosedChannelException());
    }
}
