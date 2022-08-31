package com.chmorn.iptv;

import java.time.format.DateTimeFormatter;

/**
 * @author chmorn
 * @description 视频合并线程
 * @date 2022/8/31
 **/
public class MixThread implements Runnable {

    private static boolean exit = false;

    private MixThread() {
    }

    @Override
    public void run() {
        while (!exit) {

            //5秒刷新一次进度
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
