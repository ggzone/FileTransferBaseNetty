package com.jc.filetransfer.protocal;

import io.netty.buffer.ByteBuf;

/**
 * Created by Hunter on 2017/04/14.
 */
public interface Encodable {
    int encodeLength();

    void encode(ByteBuf buf);
}
