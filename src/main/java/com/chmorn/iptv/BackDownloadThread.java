package com.chmorn.iptv;

import com.chmorn.model.QueueModel;
import com.chmorn.utils.DownloadUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenxu
 * @version 1.0
 * @className BackDownloadThread
 * @description 回录下载线程
 * @date 2021/8/26
 **/
public class BackDownloadThread implements Runnable {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");

    //终止线程标志（volatile修饰符用来保证其它线程读取的总是该变量的最新的值）
    private volatile boolean exit = false;
    private QueueModel model;
    /**
     * 下载目录
     * 例如：M:/iptv/hntv_high/
     **/
    private String distPath;
    private int downloadId;
    /**
     * m3u8地址
     * 例如：http://183.207.248.237/ott.js.chinamobile.com/PLTV/3/224/3221227482/index.m3u8
     **/
    private String m3u8url;
    /**
     * 定时开始时间
     **/
    private String timeStart;
    /**
     * 定时结束时间
     **/
    private String timeEnd;
    /**
     * 合并后的结果文件
     * 存放在目录：distPath，文件名：downloadId+"ts"
     **/
    private String mergeFileName;

    private FileOutputStream fileOutputStream;
    /**
     * 例如：1629881042-1-162988103.hls.ts
     * 前缀：1629881042，后缀：162988103
     **/
    Long tsPrefix = 0L;//前缀
    int tsPrefixLength = 0;//前缀的长度
    Long tsSuffix = 100000000L;//后缀

    private BackDownloadThread() {

    }

    public BackDownloadThread(QueueModel model, String distPath, int downloadId, String m3u8url, String timeStart, String timeEnd) {
        this.model = model;
        if (!distPath.endsWith(File.separator)) {
            this.distPath = distPath + File.separator;
        } else {
            this.distPath = distPath;
        }
        this.downloadId = downloadId;
        this.m3u8url = m3u8url;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.mergeFileName = this.distPath + downloadId + ".ts";
        try {
            this.fileOutputStream = new FileOutputStream(new File(mergeFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("id:" + downloadId + "开始,m3u8url=" + m3u8url);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        Document res = null;
        String m3u8 = null;
        String line = null;
        BufferedReader br = null;

        LocalDateTime start = LocalDateTime.parse(timeStart, dateTimeFormatter);
        LocalDateTime stop = LocalDateTime.parse(timeEnd, dateTimeFormatter);

        String rooturl = null;
        try {
            URL m3 = new URL(m3u8url);
            rooturl = m3.getProtocol() + "://" + m3.getHost() + m3.getPath();
            rooturl = rooturl.substring(0, rooturl.lastIndexOf("/") + 1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            res = Jsoup.connect(m3u8url).headers(headers).ignoreContentType(true).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m3u8 = res.body().html().replaceAll(" ", "\n");
        br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(m3u8.getBytes())));
        try {
            while ((line = br.readLine()) != null) {
                if (!DownloadUtils.isNull(line) && !line.startsWith("#")) {
                    line = line.split("\\.")[0];//1629881042-1-162988103.hls.ts
                    String[] ls = line.split("-1-");
                    tsPrefixLength = ls[0].length();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            br = null;
        }
        res = null;
        m3u8 = null;
        line = null;

        //截取开始时间，长度和实际ts前缀一致
        tsPrefix = Long.valueOf(DownloadUtils.getMilliSecond(start).substring(0, tsPrefixLength));
        URL url = null;
        String fileName = null;
        InputStream inputStream = null;
        String milliSecondFormat = "0000000000000";//13位
        while (true) {
            String tmp = milliSecondFormat.substring(String.valueOf(tsPrefix).length());
            LocalDateTime downloadTime = DownloadUtils.getMilliSecond(tsPrefix + tmp);//下载到的时间点
            //结束时间已过,退出
            if (downloadTime.isAfter(stop)) {
                model.setState(1);
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                exit = true;
                System.out.println("id:" + downloadId + "完成");
                break;
            }
            tsPrefix++;
            tsSuffix++;

            while (true) {
                try {
                    fileName = tsPrefix + "-1-" + tsSuffix + ".hls.ts";
                    url = new URL(rooturl + fileName);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(8 * 1000);
                    conn.setUseCaches(true);
                    inputStream = url.openStream();
                    downByInputstream(inputStream);
                    break;
                } catch (IOException e) {
                    //如果文件流为空，则说明fileName不对，向下找
                    //如果文件流不为空，则说明下载异常，再去下载几次
                    if (inputStream == null) {
                        tsPrefix++;
                    } else {
                        //处理个别异常下载，再多次尝试下载
                        try {
                            Thread.sleep(10);
                            int lo;
                            for (lo = 0; lo < 5; lo++) {
                                try {
                                    downByInputstream(inputStream);
                                    break;
                                } catch (IOException ioException) {
                                    if (lo >= 4) {
                                        System.out.println("异常：" + rooturl + fileName);
                                        ioException.printStackTrace();
                                        break;
                                    } else {
                                        Thread.sleep(10);
                                    }
                                }
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }

                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            url = null;
            fileName = null;
            inputStream = null;
        }

    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    private void downByInputstream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];// 一次读取1K
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, len);// buffer从指定字节数组写入。buffer:数据中的起始偏移量,len:写入的字数。
        }
        inputStream.close();
        fileOutputStream.flush();
        //fileOutputStream.close();
    }

}
