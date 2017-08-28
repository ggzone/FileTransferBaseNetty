package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * Created by Hunter on 2017/04/14.
 */
public class FileDownloadFailure implements ResponseMessage {
    public final String streamId;
    public final String error;

    public FileDownloadFailure(String streamId, String error) {
        this.streamId = streamId;
        this.error = error;
    }

    public static FileDownloadFailure decode(ByteBuf buf) {
        int streamIdLength = buf.readInt();
        byte[] streamIdbytes = new byte[streamIdLength];
        buf.readBytes(streamIdbytes);

        String streamId = new String(streamIdbytes, StandardCharsets.UTF_8);
        int errorLength = buf.readInt();
        byte[] errorBytes = new byte[errorLength];
        buf.readBytes(errorBytes);
        String error = new String(errorBytes, StandardCharsets.UTF_8);
        return new FileDownloadFailure(streamId, error);
    }

    @Override
    public Type type() {
        return Type.FileDownloadFailure;
    }

    @Override
    public int encodeLength() {
        return streamId.length() + error.length() + 4 + 4;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(streamId.length());
        buf.writeBytes(streamId.getBytes());
        buf.writeInt(error.length());
        buf.writeBytes(error.getBytes());
    }

    @Override
    public String toString() {
        return "FileDownloadFailure{" +
                "streamId='" + streamId + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
