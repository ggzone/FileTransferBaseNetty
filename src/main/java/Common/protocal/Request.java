package Common.protocal;


import java.io.Serializable;

/**
 * Created by Hunter on 2017/02/08.
 */
public class Request implements Serializable{

    public static final int DOWNLOAD=1;
    public static final int UPLOAD=2;
    public static final int UPLOADCONTENT=3;
    public static final int REQFILESEGMENT=4;


    /**
     * 请求类型，
     * 1：msg为上传请求消息
     * 2：msg为下载文件请求消息
     * 3：msg为上传文件内容
     * 4: 请求下载文件的片段
     */
    private int type;
    private String file;
    private long fileSize;
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


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
