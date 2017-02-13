package Common.protocal;

import java.io.Serializable;

/**
 * Created by Hunter on 2017/02/08.
 */
public class Response implements Serializable {

    public static final int RESDOWNLOAD=1;
    public static final int RESUPLOAD=2;
    public static final int DOWNLOADCONTENT=3;
    public static final int REQUPLOADSEG=4;
    /**
     * return code
     */
    public static final int SUCESS=200;
    public static final int FILENOTFOUND=404;
    public static final int IOException=403;
    public static final int FILEEXIST=402;
    /**
     * response type，
     * 1：msg为上传请求回复
     * 2：msg为下载文件请求回复
     * 3：msg为下载文件内容
     */
    private int type;
    /**
     * 200：sucess
     * 404：FileNotFound
     */
    private int retCode=-1;
    /**
     * socket buffer size
     */
    private int bufferSize=-1;
    /**
     * file segment size
     */
    private int sumSeg=-1;
    /**
     * current segemnt index,which is sending
     */
    private int curSeg=-1;
    /**
     * current segment bytes
     */
    private byte[] buffer=null;

//    private ResponseBody msg;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getSumSeg() {
        return sumSeg;
    }

    public void setSumSeg(int sumSeg) {
        this.sumSeg = sumSeg;
    }

    public int getCurSeg() {
        return curSeg;
    }

    public void setCurSeg(int curSeg) {
        this.curSeg = curSeg;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
