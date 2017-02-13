package Common.processor;


import Common.protocal.Request;
import Common.protocal.Response;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Hunter on 2017/02/09.
 */
public class ClientDownLoadProcessor {
    private static final Logger LOGGER=Logger.getLogger(ClientDownLoadProcessor.class);
    private String remoteFile=null;
    private String localFile=null;
    private RandomAccessFile raf=null;
    private int sumSeg=0;
    private int curSeg=0;
    private int bufferSize=0;


    public ClientDownLoadProcessor(String remoteFile,String localFile){
        this.remoteFile=remoteFile;
        this.localFile=localFile;
    }

    public void processDownLoad(ChannelHandlerContext ctx, Response res){
        sumSeg=res.getSumSeg();
        bufferSize=res.getBufferSize();
        LOGGER.info("Segemnt Sum:"+sumSeg+",Buffer Size:"+bufferSize);

        Request req=new Request();
        req.setType(Request.REQFILESEGMENT);
        req.setCurSeg(curSeg);
        ctx.writeAndFlush(req);
        LOGGER.info("Send Segment Request,SegID:"+curSeg+",type:"+ Request.REQFILESEGMENT);
        try {
            raf=new RandomAccessFile(localFile,"rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void processDownLoadContent(ChannelHandlerContext ctx, Response res){
        try {
            raf.seek(bufferSize*curSeg);
            raf.write(res.getBuffer());
            curSeg+=1;
            if(curSeg==sumSeg){
                LOGGER.info("File download complete:"+remoteFile+",local location:"+localFile);
                ctx.close();
                raf.close();
            }else{
                Request req=new Request();
                req.setType(Request.REQFILESEGMENT);
                req.setCurSeg(curSeg);
                ctx.writeAndFlush(req);
                LOGGER.info("Send Segment Request:"+curSeg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
