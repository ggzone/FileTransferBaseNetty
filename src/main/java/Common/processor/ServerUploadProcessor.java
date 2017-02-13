package Common.processor;


import Common.protocal.Request;
import Common.protocal.Response;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Hunter on 2017/02/09.
 */
public class ServerUploadProcessor {
    private static final Logger LOGGER=Logger.getLogger(ServerDownLoadProcessor.class);
    private int bufferSize=204800;
    RandomAccessFile raf=null;
    String tarFile=null;
    private int sumSeg=0;
    private int curSeg=0;




    public void processUpload(ChannelHandlerContext ctx, Request request){
        tarFile=request.getFile();
        Response res=new Response();
        res.setType(Response.RESUPLOAD);
        if(new File(tarFile).exists()){
            res.setRetCode(Response.FILEEXIST);
            LOGGER.error("Upload Request File Exist:"+tarFile+",Connection Close!");
            ctx.writeAndFlush(res);
            ctx.close();
        }else{
            try {
                raf=new RandomAccessFile(tarFile,"rw");
            } catch (FileNotFoundException e) {
                LOGGER.error("Upload Request File Exist:"+tarFile+",Connection Close!");
            }
            long fileSize=request.getFileSize();
            LOGGER.info("Upload Request File Sucess:"+tarFile+",file size:"+fileSize);
            sumSeg=getFileSegs(fileSize);
            res.setRetCode(Response.SUCESS);
            res.setBufferSize(bufferSize);
            res.setSumSeg(sumSeg);
            ctx.writeAndFlush(res);

            Response firstRes=new Response();
            firstRes.setType(Response.REQUPLOADSEG);
            firstRes.setCurSeg(curSeg);
            ctx.writeAndFlush(firstRes);
        }
    }

    public void processUpLoadContent(ChannelHandlerContext ctx,Request request){
        try {
            raf.seek(bufferSize*curSeg);
            raf.write(request.getBuffer());
            curSeg+=1;
            if(curSeg==sumSeg){
                LOGGER.info("File upload complete:"+tarFile);
                ctx.close();
                raf.close();
            }else{
                Response res=new Response();
                res.setType(Response.REQUPLOADSEG);
                res.setCurSeg(curSeg);
                ctx.writeAndFlush(res);
                LOGGER.info("Send Segment Request:"+curSeg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getFileSegs(long fileSize){
        int sumSeg=-1;
        //对文件进行切分发送
        if(fileSize%bufferSize==0){
            //当文件正好切分为整数个段
            sumSeg=(int)fileSize/bufferSize;
        }else{
            sumSeg=(int)fileSize/bufferSize+1;
        }
        return sumSeg;
    }
}
