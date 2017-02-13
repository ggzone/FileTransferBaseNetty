package Common.processor;


import Common.protocal.Request;
import Common.protocal.Response;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketAddress;

/**
 * Created by Hunter on 2017/02/08.
 */
public class ServerDownLoadProcessor {
    private static final Logger LOGGER=Logger.getLogger(ServerDownLoadProcessor.class);
    private int bufferSize=204800;
    RandomAccessFile raf=null;
    String tarFile=null;


    public void processDownload(ChannelHandlerContext ctx, final Request request){
        tarFile=request.getFile();
        SocketAddress clientAddress = ctx.channel().remoteAddress();

        Response response=new Response();
        response.setType(Response.RESDOWNLOAD);
        try {
            raf=new RandomAccessFile(request.getFile(),"r");
            LOGGER.info(clientAddress+" request file:"+tarFile);
            response.setRetCode(Response.SUCESS);
            response.setBufferSize(bufferSize);
            response.setSumSeg(getFileSegs(raf.length()));

        } catch (FileNotFoundException e) {
            LOGGER.error(clientAddress+" request file not exist:"+tarFile);
            response.setRetCode(Response.FILENOTFOUND);
        } catch (IOException e) {
            LOGGER.error(clientAddress+" request file :"+tarFile+".When get length,IOException:"+e.getMessage());
            response.setRetCode(Response.IOException);
        }

        ctx.writeAndFlush(response);
    }

    public void processSegment(ChannelHandlerContext ctx,Request request){
        try {
            raf.seek(bufferSize*request.getCurSeg());
            //剩余文件长度
            long remainderFileSize=raf.length()-raf.getFilePointer();
            LOGGER.info("Not send file length:"+remainderFileSize);

            int byteSize=bufferSize;
            if(remainderFileSize<bufferSize){
                LOGGER.info("Not send file length lower buffersize");
                byteSize=(int)remainderFileSize;
            }
            byte[] buffer=new byte[byteSize];
            if((raf.read(buffer)!=-1) && (remainderFileSize >0)){
                Response res=new Response();
                res.setType(Response.DOWNLOADCONTENT);
                res.setCurSeg(request.getCurSeg());
                res.setBuffer(buffer);
                ctx.writeAndFlush(res);
                LOGGER.info("Send segment:"+request.getCurSeg());
            }else{
                raf.close();
                ctx.close();
                LOGGER.info("File download completed!"+tarFile);
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
