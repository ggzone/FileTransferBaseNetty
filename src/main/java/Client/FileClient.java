package Client;


import Common.protocal.Request;
import Server.FileServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by Hunter on 2017/02/09.
 */
public class FileClient {
    /**
     * 日志组件
     */
    private static final Logger LOGGER=Logger.getLogger(FileServer.class);

    private Channel channel=null;
    private EventLoopGroup group=null;

    private String host=null;
    private int port=-1;

    private String remoteFile=null;
    private String localFile=null;


    public FileClient(String host,int port,String remoteFile,String localFile){
        this.host=host;
        this.port=port;
        this.remoteFile=remoteFile;
        this.localFile=localFile;
        initSocket();
    }



    public void initSocket(){
        Bootstrap b=new Bootstrap();

        group=new NioEventLoopGroup();
        b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY,true).handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingResolver(null)));
                ch.pipeline().addLast(new ObjectEncoder());
                ch.pipeline().addLast(new FileClientHandler(remoteFile,localFile));
            }
        });
        ChannelFuture channelFuture = null;
        try {
            channelFuture = b.connect(host, port).sync();
            LOGGER.info("Server starts，remote server address:"+host+":"+port);
            channel=channelFuture.channel();

        } catch (InterruptedException e) {
            LOGGER.error("FileClient connection error:"+e.getMessage());
        }
    }

    public void DownLoadFile(){
        Request req=new Request();
        req.setType(Request.DOWNLOAD);
        req.setFile(remoteFile);
        channel.writeAndFlush(req);
        LOGGER.info("Send DownLoad request,type:"+req.getType()+",file:"+req.getFile());
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("Channel.closeFuture() has benn interrupted:"+e.getMessage());
        }finally {
            group.shutdownGracefully();
        }

    }

    public void UploadFile(){
        long fileSize=new File(localFile).length();
        Request req=new Request();
        req.setType(Request.UPLOAD);
        req.setFile(remoteFile);
        req.setFileSize(fileSize);
        channel.writeAndFlush(req);
        LOGGER.info("Send Upload request,type:"+req.getType()+",file:"+req.getFile()+",file size:"+fileSize);
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("Channel.closeFuture() has benn interrupted:"+e.getMessage());
        }finally {
            group.shutdownGracefully();
        }
    }



    public static void main(String[] args) {
        FileClient fileClient = new FileClient("127.0.0.1", 10010,"E://nettyBookSourceV2.zip","E://nettyBookSourceV2111.zip");
        fileClient.DownLoadFile();
        //fileClient.UploadFile();
    }


    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public EventLoopGroup getGroup() {
        return group;
    }

    public void setGroup(EventLoopGroup group) {
        this.group = group;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRemoteFile() {
        return remoteFile;
    }

    public void setRemoteFile(String remoteFile) {
        this.remoteFile = remoteFile;
    }

    public String getLocalFile() {
        return localFile;
    }

    public void setLocalFile(String localFile) {
        this.localFile = localFile;
    }
}
