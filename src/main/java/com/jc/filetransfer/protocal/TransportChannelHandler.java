package com.jc.filetransfer.protocal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Hunter on 2017/04/14.
 */

@ChannelHandler.Sharable
public class TransportChannelHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportChannelHandler.class);

    private final TransportRequestHandler requestHandler;
    private final TransportResponseHandler responseHandler;

    public TransportChannelHandler(TransportRequestHandler requestHandler, TransportResponseHandler responseHandler) {
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
    }

    public static String getRemoteAddress(Channel channel) {
        if (channel != null && channel.remoteAddress() != null) {
            return channel.remoteAddress().toString();
        }
        return "<unknown remote>";
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof RequestMessage) {
            requestHandler.handler(ctx, msg);
        } else {
            responseHandler.handler(ctx, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            requestHandler.channelInactive(ctx);
        } catch (RuntimeException e) {
            LOGGER.error("Exception from request handler while unregistering channel", e);
        }
        try {
            responseHandler.channelInactive(ctx);
        } catch (RuntimeException e) {
            LOGGER.error("Exception from response handler while unregistering channel", e);
        }
        LOGGER.error("Channel channelInactive from" + getRemoteAddress(ctx.channel()));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.warn("Exception in connection from " + getRemoteAddress(ctx.channel()),
                cause);
        requestHandler.exceptionCaught(ctx, cause);
        responseHandler.exceptionCaught(ctx, cause);

        ctx.close();
    }


}
