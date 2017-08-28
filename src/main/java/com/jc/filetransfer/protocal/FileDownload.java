package com.jc.filetransfer.protocal;


import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * Created by Hunter on 2017/04/14.
 */
public class FileDownload implements RequestMessage {
    public final String fileId;

    public FileDownload(String fileId) {
        this.fileId = fileId;
    }

    public static FileDownload decode(ByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        String fileId = new String(bytes, StandardCharsets.UTF_8);
        return new FileDownload(fileId);
    }

    @Override
    public int encodeLength() {
        return fileId.length() + 4;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(fileId.length());
        buf.writeBytes(fileId.getBytes());
    }

    @Override
    public Type type() {
        return Type.FileDownload;
    }


    @Override
    public String toString() {
        return "FileDownload{" +
                "fileId='" + fileId + '\'' +
                '}';
    }
}
