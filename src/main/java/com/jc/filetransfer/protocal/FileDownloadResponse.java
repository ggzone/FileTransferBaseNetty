package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Created by Hunter on 2017/04/14.
 */
public class FileDownloadResponse implements ResponseMessage {
    public final String fileId;
    public final long byteCount;
    public final File file;

    public FileDownloadResponse(String fileId, long byteCount, File file) {
        this.fileId = fileId;
        this.byteCount = byteCount;
        this.file = file;
    }

    public static FileDownloadResponse decode(ByteBuf buf) {
        long byteCount = buf.readLong();
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        String fileId = new String(bytes, StandardCharsets.UTF_8);
        return new FileDownloadResponse(fileId, byteCount, null);
    }

    @Override
    public int encodeLength() {
        return 8 + 4 + fileId.length();
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeLong(byteCount);
        buf.writeInt(fileId.length());
        buf.writeBytes(fileId.getBytes());
    }

    @Override
    public Type type() {
        return Type.FileDownloadResponse;
    }


    @Override
    public String toString() {
        return "FileDownloadResponse{" +
                "fileId='" + fileId + '\'' +
                ", byteCount=" + byteCount +
                ", file=" + file +
                '}';
    }
}
