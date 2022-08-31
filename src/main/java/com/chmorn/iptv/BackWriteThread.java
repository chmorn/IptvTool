package com.chmorn.iptv;

import com.chmorn.model.FileModel;
import com.chmorn.model.QueueModel;
import com.chmorn.utils.DownloadUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenxu
 * @version 1.0
 * @className BackDownloadThread
 * @description 回录下载线程
 * @date 2021/8/26
 **/
public class BackWriteThread implements Runnable {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d H:m");

    public AtomicInteger taskCount = new AtomicInteger(0);

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
     * 例如：1629881042-1-162988103.hls.ts
     * 前缀：1629881042，后缀：162988103
     **/
    Long tsPrefix = 0L;//前缀
    int tsPrefixLength = 0;//前缀的长度
    Long tsSuffix = 100000000L;//后缀

    private BackWriteThread() {
    }

    public BackWriteThread(QueueModel model, String distPath, int downloadId, String m3u8url, String timeStart, String timeEnd) {
        this.model = model;
        if (distPath.endsWith(File.separator)) {
            this.distPath = distPath + downloadId + File.separator;
        } else {
            this.distPath = distPath + File.separator + downloadId + File.separator;
        }
        new File(this.distPath).mkdir();
        this.downloadId = downloadId;
        this.m3u8url = m3u8url;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
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
        //计算后缀
//        LocalDateTime zero = LocalDateTime.parse("1970-1-1 0:0", dateTimeFormatter);
//        Duration between = Duration.between(zero, start);
//        tsSuffix = between.getSeconds();

        //获取m3u8地址
        String rooturl = null;
        try {
            URL m3 = new URL(m3u8url);
            rooturl = m3.getProtocol() + "://" + m3.getHost() + ":" + m3.getPort() + m3.getPath();
            rooturl = rooturl.substring(0, rooturl.lastIndexOf("/") + 1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            res = Jsoup.connect(m3u8url).headers(headers).ignoreContentType(true).get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
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
            return;
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
        LocalDateTime begin = LocalDateTime.now();
        while (true) {
            String tmp = milliSecondFormat.substring(String.valueOf(tsPrefix).length());
            LocalDateTime downloadTime = DownloadUtils.getMilliSecond(tsPrefix + tmp);//下载到的时间点
            //结束时间已过,退出
            if (downloadTime.isAfter(stop)) {
                while (taskCount.get() > 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                model.setState(1);
                System.out.println("id:" + downloadId + "完成");
                try {
                    tsMix();
                    LocalDateTime end = LocalDateTime.now();
                    System.out.println("耗时（秒）：" + Duration.between(begin, end).getSeconds());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            //控制队列下载数量
            while (taskCount.get() > 12) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            tsPrefix++;
            tsSuffix++;
            try {
                fileName = tsPrefix + "-1-" + tsSuffix + ".hls.ts";
                url = new URL(rooturl + fileName);
                new Thread(new BackReadThread(url, tsPrefix + ".ts", distPath, this)).start();
                taskCount.incrementAndGet();
            } catch (Exception e) {
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //url = null;
            fileName = null;
        }

    }

    // ts视频合并+（new文件夹）
    private void tsMix() throws IOException {
        System.out.println("开始合并.............");
        File dir = new File(distPath);
        File[] files = dir.listFiles();
        //files读取可能顺序乱了，重新排序
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            list.add(files[i].getName());
        }
        Collections.sort(list);
        //排序结束
        FileInputStream fis = null;
        FileOutputStream fos = new FileOutputStream(distPath + downloadId + "-merge.ts");
        byte[] buffer = new byte[1024];// 一次读取1K
        int len;
        System.out.println(files.length);
        // 长度减1（有个new文件夹）
        for (int i = 0; i < list.size(); i++) {
            fis = new FileInputStream(new File(distPath + list.get(i)));
            //fis = new FileInputStream(new File(filepath+i+".ts"));
            len = 0;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);// buffer从指定字节数组写入。buffer:数据中的起始偏移量,len:写入的字数。
            }
            fis.close();
        }
        fos.flush();
        fos.close();
        System.out.println("合并完成.............");

    }

}
