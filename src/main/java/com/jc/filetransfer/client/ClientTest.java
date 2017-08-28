package com.jc.filetransfer.client;

import com.jc.filetransfer.protocal.StreamCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.Random;

/**
 * Created by Hunter on 2017/04/14.
 */
public class ClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("E:\\bdproxy-parent\\bdproxy-filetransfer\\src\\main\\resources\\SSLClient.properties"));
        String storeFile = props.getProperty("ssl.keystore");
        String storePass = props.getProperty("ssl.storepass");
        String keyPass = props.getProperty("ssl.keypass");

        SSLEngine sslConfig = SSLFactory.createSSLEngine(storeFile, storePass, keyPass, true);

        TransportClient client = new TransportClient("127.0.0.1", 10010);
        client.start(sslConfig);


        //download(client);
        upload(client);

    }

    public static void upload(TransportClient client) {
        String fileId = String.valueOf(new Random().nextInt());
        client.uploadFile(fileId, new File("E:\\boost_1_64_0.zip"));
    }

    public static void download(TransportClient client) {
        RandomAccessFile raf = null;

        String fileId = String.valueOf((new Random().nextInt(100) + 1000));
        LOGGER.info("Download file path:" + "D:\\base\\" + fileId + "\\out.zip");

        File baseDir = new File("D:\\base\\" + fileId);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        try {
            raf = new RandomAccessFile("D:\\base\\" + fileId + "\\out.zip", "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        final RandomAccessFile finalRaf = raf;
        StreamCallback callback = new StreamCallback() {

            @Override
            public void onData(String streamId, ByteBuffer buf) throws IOException {
                int remaining = buf.remaining();
                if (remaining > 0) {
                    byte[] bytes = new byte[remaining];
                    buf.get(bytes);
                    finalRaf.write(bytes);
                }
            }

            @Override
            public void onComplete(String streamId) throws IOException {
                finalRaf.close();
                System.out.println("File send finished!");
            }

            @Override
            public void onFailure(String streamId, Throwable cause) throws IOException {
                finalRaf.close();
                System.out.println("File send failed!");
            }
        };
        client.downloadFile(fileId, callback);
    }

}
