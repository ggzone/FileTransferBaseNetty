package Client;



import Common.processor.ClientDownLoadProcessor;
import Common.processor.ClientUploadProcessor;
import Common.protocal.Response;
import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

/**
 * Created by Hunter on 2017/02/09.
 */
public class FileClientHandler extends SimpleChannelInboundHandler<Response> {
    private static final Logger LOGGER=Logger.getLogger(FileClientHandler.class);

    private int sumSeg=0;
    private int curSeg=0;
    private int bufferSize=-1;

    private String remoteFile=null;
    private String localFile=null;
    private ClientDownLoadProcessor clientDownLoadProcessor=null;
    private ClientUploadProcessor clientUploadProcessor=null;


    public FileClientHandler(String remoteFile,String localFile){
        this.remoteFile=remoteFile;
        this.localFile=localFile;
    }


    protected void channelRead0(ChannelHandlerContext ctx, Response res) throws Exception {
       switch (res.getType()){
           case Response.RESDOWNLOAD:
               switch (res.getRetCode()){
                   case Response.FILENOTFOUND:
                       LOGGER.error("DownLoad RemoteFile Not Found:"+remoteFile+",Connection Close!");
                       ctx.close();
                       break;
                   case Response.SUCESS:
                       LOGGER.info("DownLoad RemoteFile exists:"+localFile);
                       clientDownLoadProcessor=new ClientDownLoadProcessor(remoteFile,localFile);
                       clientDownLoadProcessor.processDownLoad(ctx,res);
                       break;
               }
               break;
           case Response.RESUPLOAD:
               switch (res.getRetCode()){
                   case Response.FILEEXIST:
                       LOGGER.error("Upload RemoteFile exist:"+remoteFile+",Connection Close!");
                       ctx.close();
                       break;
                   case Response.SUCESS:
                       LOGGER.info("Upload Receive response:"+res.getRetCode());
                       clientUploadProcessor=new ClientUploadProcessor(remoteFile,localFile);
                       clientUploadProcessor.processUpload(ctx,res);
                       break;
               }
               break;
           case Response.DOWNLOADCONTENT:
               LOGGER.info("Received segemnt:"+res.getCurSeg()+",byte size:"+res.getBuffer().length);
               clientDownLoadProcessor.processDownLoadContent(ctx,res);
               break;
           case Response.REQUPLOADSEG:
               LOGGER.info("Receive Segment Request:"+res.getCurSeg());
               clientUploadProcessor.processSegment(ctx,res);
               break;
           default:
               LOGGER.info("Receive other response,type:"+res.getType()+",return code:"+res.getRetCode());
       }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Connection close!Exception:"+cause.getMessage());
        ctx.close();
    }

}
