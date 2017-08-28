package com.jc.filetransfer.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Hunter on 2017/03/29.
 */
public class ClientChannelInitializer extends ChannelInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientChannelInitializer.class);

    private String remoteFile;
    private String localFile;
    private String storeFile = "";
    private String storePass = "";
    private String keyPass = "";

    public ClientChannelInitializer(TransportClient transportClient, String remoteFile, String localFile, String storeFile, String storePass, String keyPass) {

        this.remoteFile = remoteFile;
        this.localFile = localFile;
        this.storeFile = storeFile;
        this.storePass = storePass;
        this.keyPass = keyPass;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {


    }
}
