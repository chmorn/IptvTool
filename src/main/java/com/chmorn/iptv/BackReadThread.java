package com.chmorn.iptv;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author chmorn
 * @description 回录下载线程
 * @date 2022/8/31
 **/
public class BackReadThread implements Runnable {

    //视频url
    private URL url;
    //视频名
    private String fileName;
    //保存目录
    private String distPath;
    //写入线程
    private BackWriteThread writeThread;

    private BackReadThread() {
    }

    public BackReadThread(URL url, String fileName, String distPath, BackWriteThread writeThread) {
        this.distPath = distPath;
        this.url = url;
        this.fileName = fileName;
        this.writeThread = writeThread;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4 * 1000);
            conn.setUseCaches(true);
            FileUtils.copyURLToFile(url, new File(distPath + fileName));
        } catch (Exception e) {
            //处理个别异常下载，再多次尝试下载
            try {
                Thread.sleep(10);
                int lo;
                for (lo = 0; lo < 5; lo++) {
                    try {
                        FileUtils.copyURLToFile(url, new File(distPath + fileName));
                        break;
                    } catch (IOException ioException) {
                        if (lo >= 4) {
//                            System.out.println("异常：" + fileName);
//                            ioException.printStackTrace();
                            break;
                        } else {
                            Thread.sleep(10);
                        }
                    }
                }
            } catch (InterruptedException interruptedException) {
                //interruptedException.printStackTrace();
            }

        } finally {
            writeThread.taskCount.decrementAndGet();
        }
    }

}
