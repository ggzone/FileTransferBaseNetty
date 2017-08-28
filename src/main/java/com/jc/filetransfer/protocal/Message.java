package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;

/**
 * Created by Hunter on 2017/04/14.
 */
public interface Message extends Encodable {

    Type type();

    enum Type implements Encodable {
        FileDownload(0), FileDownloadResponse(1), FileDownloadFailure(2),
        FileUpload(3), FileUploadResponse(4), FileUploadFailure(5), FileUploadAck(6);

        private final byte id;

        Type(int id) {
            assert id < 128 : "Cannot have more than 128 message types";
            this.id = (byte) id;
        }

        public static Type decode(ByteBuf buf) {
            byte id = buf.readByte();
            switch (id) {
                case 0:
                    return FileDownload;
                case 1:
                    return FileDownloadResponse;
                case 2:
                    return FileDownloadFailure;
                case 3:
                    return FileUpload;
                case 4:
                    return FileUploadResponse;
                case 5:
                    return FileUploadFailure;
                case 6:
                    return FileUploadAck;
                case -1:
                    throw new IllegalArgumentException("User type messages cannot be decoded.");
                default:
                    throw new IllegalArgumentException("Unknown message type: " + id);
            }
        }

        @Override
        public int encodeLength() {
            return 1;
        }

        @Override
        public void encode(ByteBuf buf) {
            buf.writeByte(id);
        }

    }
}
