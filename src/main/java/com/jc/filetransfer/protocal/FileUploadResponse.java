package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Created by Hunter on 2017/04/14.
 */
public class FileUploadResponse implements RequestMessage {
    public final String fileId;
    public final long byteCount;
    public final File file;

    public FileUploadResponse(String fileId, long byteCount, File file) {
        this.fileId = fileId;
        this.byteCount = byteCount;
        this.file = file;
    }

    public static FileUploadResponse decode(ByteBuf buf) {
        long byteCount = buf.readLong();
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        String fileId = new String(bytes, StandardCharsets.UTF_8);
        return new FileUploadResponse(fileId, byteCount, null);
    }

    @Override
    public Type type() {
        return Type.FileUploadResponse;
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
    public String toString() {
        return "FileUploadResponse{" +
                "fileId='" + fileId + '\'' +
                ", byteCount=" + byteCount +
                ", file=" + file +
                '}';
    }
}
