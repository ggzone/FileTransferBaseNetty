package com.jc.filetransfer.protocal;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Created by Hunter on 2017/04/14.
 */
public class TransportFrameDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportFrameDecoder.class);

    private static final int UNKNOWN_FRAME_SIZE = -1;
    private static final int LENGTH_SIZE = 8;
    private static final int MAX_FRAME_SIZE = Integer.MAX_VALUE;
    private final ByteBuf frameLenBuf = Unpooled.buffer(LENGTH_SIZE, LENGTH_SIZE);
    private LinkedList<ByteBuf> buffers = new LinkedList<>();
    private long totalSize = 0;
    private long nextFrameSize = UNKNOWN_FRAME_SIZE;
    private volatile Interceptor interceptor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        buffers.add(buf);
        totalSize += buf.readableBytes();

        while (!buffers.isEmpty()) {
            if (interceptor != null) {
                ByteBuf first = buffers.getFirst();
                int available = first.readableBytes();
                if (feedInterceptor(first)) {
                    assert !first.isReadable() : "Interceptor still active but buffer has data.";
                }

                int read = available - first.readableBytes();
                if (read == available)
                    buffers.removeFirst().release();

                totalSize -= read;

            } else {
                ByteBuf frame = decodeNext();
                if (frame == null)
                    break;

                ctx.fireChannelRead(frame);
            }
        }
    }

    /**
     * 用8为字节保存消息总大小信息，存在一个ByteBuf不够8字节的情况
     *
     * @return, 两种情况：下一个消息长度，-1
     */
    private long decodeFrameSize() {
        //可能上一次解析到了消息长度，但是剩下buf长度不够拼接出一个完整的消息
        if (nextFrameSize != UNKNOWN_FRAME_SIZE || totalSize < LENGTH_SIZE)
            return nextFrameSize;

        //第一个ByteBuf数据超过8个字节情况
        ByteBuf first = buffers.getFirst();
        if (first.readableBytes() >= LENGTH_SIZE) {
            nextFrameSize = first.readLong() - LENGTH_SIZE;
            totalSize -= LENGTH_SIZE;
            //如果bytebuf中没有数据就移除
            if (!first.isReadable())
                buffers.removeFirst().release();

            return nextFrameSize;
        }

        //循环用以从多个buf中读出8字节，每个buf不够8字节情况下
        while (frameLenBuf.readableBytes() < LENGTH_SIZE) {
            ByteBuf next = buffers.getFirst();
            //本次循环将要读取的字节数
            int toRead = Math.min(next.readableBytes(), LENGTH_SIZE - frameLenBuf.readableBytes());
            frameLenBuf.writeBytes(next, toRead);
            if (!next.isReadable())
                buffers.removeFirst().release();
        }

        nextFrameSize = frameLenBuf.readLong() - LENGTH_SIZE;
        totalSize -= LENGTH_SIZE;
        frameLenBuf.clear();
        return nextFrameSize;
    }


    private ByteBuf decodeNext() throws Exception {
        long frameSize = decodeFrameSize();
        //返回了-1，或者数据不够组成一个完整消息
        if (frameSize == UNKNOWN_FRAME_SIZE || totalSize < frameSize)
            return null;

        // Reset size for next frame.
        nextFrameSize = UNKNOWN_FRAME_SIZE;

        Preconditions.checkArgument(frameSize < MAX_FRAME_SIZE, "Too large frame: %s", frameSize);
        Preconditions.checkArgument(frameSize > 0, "Frame length should be positive: %s", frameSize);

        int remaining = (int) frameSize;
        if (buffers.getFirst().readableBytes() >= remaining) {
            return nextBufferForFrame(remaining);
        }

        CompositeByteBuf frame = buffers.getFirst().alloc().compositeBuffer(remaining);
        while (remaining > 0) {
            ByteBuf next = nextBufferForFrame(remaining);
            remaining -= next.readableBytes();
            frame.addComponent(next).writerIndex(frame.readableBytes() + next.readableBytes());
        }
        assert remaining == 0;
        return frame;
    }


    private ByteBuf nextBufferForFrame(int bytesToRead) {
        ByteBuf buf = buffers.getFirst();
        ByteBuf frame;
        if (buf.readableBytes() > bytesToRead) {
            frame = buf.retain().readSlice(bytesToRead);
            totalSize -= bytesToRead;
        } else {
            frame = buf;
            buffers.removeFirst();
            totalSize -= bytesToRead;
        }
        return frame;
    }

    private boolean feedInterceptor(ByteBuf buf) throws Exception {
        //当已经接收完毕时，志空
        if (interceptor != null && !interceptor.handle(buf)) {
            interceptor = null;
        }
        return interceptor != null;
    }

    public void setInterceptor(Interceptor interceptor) {
        Preconditions.checkState(this.interceptor == null, "Already have an interceptor.");
        this.interceptor = interceptor;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (interceptor != null) {
            interceptor.exceptionCaught(cause);
        }
        super.exceptionCaught(ctx, cause);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        for (ByteBuf buf : buffers)
            buf.release();

        if (interceptor != null)
            interceptor.channelInactive();

        frameLenBuf.release();
        super.channelInactive(ctx);
    }

    public interface Interceptor {

        //消费流的一个方法
        boolean handle(ByteBuf data) throws Exception;

        //管道异常调用的方法
        void exceptionCaught(Throwable cause) throws Exception;

        void channelInactive() throws Exception;

    }

}
