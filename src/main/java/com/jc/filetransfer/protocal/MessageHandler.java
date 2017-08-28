package com.jc.filetransfer.protocal;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Hunter on 2017/04/14.
 */
public interface MessageHandler {

    void handler(ChannelHandlerContext ctx, Message msg);

    void channelInactive(ChannelHandlerContext ctx) throws Exception;

    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}
