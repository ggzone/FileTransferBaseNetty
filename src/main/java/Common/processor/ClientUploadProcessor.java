package Common.processor;


import Common.protocal.Request;
import Common.protocal.Response;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Hunter on 2017/02/10.
 */
public class ClientUploadProcessor {
    private static final Logger LOGGER=Logger.getLogger(ClientUploadProcessor.class);
    private String remoteFile=null;
    private String localFile=null;
    private RandomAccessFile raf=null;
    private int sumSeg=0;
    private int curSeg=0;
    private int bufferSize=0;

    public ClientUploadProcessor(String remoteFile,String localFile){
        this.remoteFile=remoteFile;
        this.localFile=localFile;
    }

    public void processUpload(ChannelHandlerContext ctx, Response res){
        sumSeg=res.getSumSeg();
        bufferSize=res.getBufferSize();
        LOGGER.info("Segemnt Sum:"+sumSeg+",Buffer Size:"+bufferSize);
        try {
            raf=new RandomAccessFile(localFile,"r");
        } catch (FileNotFoundException e) {
            LOGGER.error("Upload file not exit:"+localFile);
        }
    }

    public void processSegment(ChannelHandlerContext ctx,Response response) {
        try {
            curSeg=response.getCurSeg();
            raf.seek(bufferSize*response.getCurSeg());
            //剩余文件长度
            long remainderFileSize=raf.length()-raf.getFilePointer();
            LOGGER.info("Not send file length:"+remainderFileSize);

            int byteSize=bufferSize;
            if(remainderFileSize<bufferSize){
                LOGGER.info("Not send file length lower buffersize,Send last segment!");
                byteSize=(int)remainderFileSize;
            }
            byte[] buffer=new byte[byteSize];
            if((raf.read(buffer)!=-1) && (remainderFileSize >0)){
                Request req=new Request();
                req.setType(Request.UPLOADCONTENT);
                req.setCurSeg(curSeg);
                req.setBuffer(buffer);
                ctx.writeAndFlush(req);
                LOGGER.info("Send segment:"+curSeg);
            }else{
                raf.close();
                ctx.close();
                LOGGER.info("File upload completed!"+remoteFile);
            }
        } catch (IOException e) {
            LOGGER.error("IOException:"+e.getMessage());
            ctx.close();
            try {
                raf.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
