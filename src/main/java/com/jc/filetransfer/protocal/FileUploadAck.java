package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class FileUploadAck implements ResponseMessage {
    public final String fileId;
    public final boolean canUpload;
    public final String error;

    public FileUploadAck(String fileId, boolean canUpload, String error) {
        this.fileId = fileId;
        this.canUpload = canUpload;
        this.error = error;
    }

    public static FileUploadAck decode(ByteBuf buf) {
        int fileidLength = buf.readInt();
        byte[] fileIdBytes = new byte[fileidLength];
        buf.readBytes(fileIdBytes);
        String fileId = new String(fileIdBytes, StandardCharsets.UTF_8);
        boolean canUpload = buf.readBoolean();
        int errorLength = buf.readInt();
        byte[] errorBytes = new byte[errorLength];
        buf.readBytes(errorBytes);
        String error = new String(errorBytes, StandardCharsets.UTF_8);

        return new FileUploadAck(fileId, canUpload, error);
    }

    @Override
    public Type type() {
        return Type.FileUploadAck;
    }

    @Override
    public int encodeLength() {
        return fileId.length() + 4 + 1 + 4 + error.length();
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(fileId.length());
        buf.writeBytes(fileId.getBytes());
        buf.writeBoolean(canUpload);
        buf.writeInt(error.length());
        buf.writeBytes(error.getBytes());
    }

    @Override
    public String toString() {
        return "FileUploadAck{" +
                "fileId='" + fileId + '\'' +
                ", canUpload=" + canUpload +
                ", error='" + error + '\'' +
                '}';
    }
}
