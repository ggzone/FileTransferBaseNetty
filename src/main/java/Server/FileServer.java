package Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.log4j.Logger;


/**
 * Created by Hunter on 2017/02/08.
 */
public class FileServer {
    /**
     * 日志组件
     */
    private static final Logger LOGGER=Logger.getLogger(FileServer.class);
    /**
     * 监听端口
     */
    private int listenPort=10001;

    public FileServer(int listenPort){
        this.listenPort=listenPort;
    }

    /**
     * 启动文件服务器
     *
     */
    public void start(){
        ServerBootstrap bootstrap=new ServerBootstrap();
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();

        try{
            bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ObjectEncoder());
                            socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingResolver(null)));
                            socketChannel.pipeline().addLast("handler",new FileServerHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true).childOption(ChannelOption.TCP_NODELAY,true);

            ChannelFuture f = bootstrap.bind(listenPort).sync();
            LOGGER.info("服务已启动，监听端口为:" + listenPort);
            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            LOGGER.error("Netty启动异常：", e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }


    public static void main(String[] args) {
        new FileServer(10010).start();
    }


}
