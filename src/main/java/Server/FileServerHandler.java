package Server;



import Common.processor.ServerDownLoadProcessor;
import Common.processor.ServerUploadProcessor;
import Common.protocal.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;


/**
 * Created by Hunter on 2017/02/08.
 */
public class FileServerHandler extends SimpleChannelInboundHandler<Request> {
    private static final Logger LOGGER= Logger.getLogger(FileServerHandler.class);
    private ServerDownLoadProcessor dwProcessor=null;
    private ServerUploadProcessor upProcessor=null;

    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        switch (request.getType()){
            case Request.DOWNLOAD:
                //true为请求下载某文件
                LOGGER.info("Receive DownLoad Request:"+request.getFile()+" from "+ctx.channel().remoteAddress());
                dwProcessor=new ServerDownLoadProcessor();
                dwProcessor.processDownload(ctx,request);
                break;
            case Request.UPLOAD:
                //false为上传文件
                LOGGER.info("Receive Upload Request:"+request.getFile()+" from "+ctx.channel().remoteAddress());
                upProcessor=new ServerUploadProcessor();
                upProcessor.processUpload(ctx,request);
                break;
            case Request.UPLOADCONTENT:
                LOGGER.info("Received segemnt:"+request.getCurSeg()+",byte size:"+request.getBuffer().length);
                upProcessor.processUpLoadContent(ctx,request);
                break;
            case Request.REQFILESEGMENT:
                //处理文件的段请求
                LOGGER.info("Receive Segment Request:"+request.getCurSeg()+" from "+ctx.channel().remoteAddress());
                dwProcessor.processSegment(ctx,request);
                break;
            default:
                LOGGER.info("Receive Other Msg:"+request.getType()+",curSeg:"+request.getCurSeg());

        }

    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Connection close!Exception:"+cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.warn("Client disconnection:"+ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }
}
