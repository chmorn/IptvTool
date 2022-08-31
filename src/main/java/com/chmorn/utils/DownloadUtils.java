package com.chmorn.utils;

import sun.misc.BASE64Encoder;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author chenxu
 * @version 1.0
 * @className DownloadUtils
 * @description TODO
 * @date 2021/8/1
 **/
public class DownloadUtils {

    public static String rootPath = System.getProperty("user.dir");

    //根据时间戳返回下载id
    public static synchronized int getDownloadId(){
        return Integer.valueOf(String.valueOf(System.currentTimeMillis()).substring(6));
    }

    public static boolean isNull(String source){
        return source==null || source.length()==0;
    }

    public static synchronized String getBase64String(String source){
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String endstr = base64Encoder.encode(source.getBytes());
        return endstr;
    }

    //LocalDateTime转时间戳
    public static synchronized String getMilliSecond (LocalDateTime localDateTime){
        Long milliSecond = localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return milliSecond+"";
    }
    //时间戳转LocalDateTime
    public static synchronized LocalDateTime getMilliSecond (String milliSecond){
        Long timestamp = Long.valueOf(milliSecond);
        LocalDateTime localDateTime =LocalDateTime.ofEpochSecond(timestamp/1000,0,ZoneOffset.ofHours(8));
        return localDateTime;
    }

    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("http://39.134.39.39/PLTV/88888888/224/3221226164/1630033321-1-100000001.hls.ts");
        System.out.println(url.getPath());
    }

}
