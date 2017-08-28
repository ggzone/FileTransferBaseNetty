package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * Created by Hunter on 2017/04/14.
 */
public class FileUpload implements RequestMessage {
    public final String fileId;
    public final long byteCount;

    public FileUpload(String fileId, long byteCount) {
        this.fileId = fileId;
        this.byteCount = byteCount;
    }

    public static FileUpload decode(ByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        String fileId = new String(bytes, StandardCharsets.UTF_8);
        long byteCount = buf.readLong();
        return new FileUpload(fileId, byteCount);
    }

    @Override
    public int encodeLength() {
        return fileId.length() + 4 + 8;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(fileId.length());
        buf.writeBytes(fileId.getBytes());
        buf.writeLong(byteCount);
    }

    @Override
    public Message.Type type() {
        return Type.FileUpload;
    }


    @Override
    public String toString() {
        return "FileUpload{" +
                "fileId='" + fileId + '\'' +
                ", byteCount=" + byteCount +
                '}';
    }
}
