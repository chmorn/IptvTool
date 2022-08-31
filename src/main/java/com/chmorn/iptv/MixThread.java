package com.chmorn.iptv;

import javafx.scene.control.TableView;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * @author chmorn
 * @description 视频合并线程
 * @date 2022/8/31
 **/
public class MixThread implements Runnable {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");
    private static DecimalFormat dF = new DecimalFormat("0.00");

    private static boolean exit = false;

    private TableView mDownloadTable;

    private MixThread() {

    }

    public MixThread(TableView mDownloadTable) {
        this.mDownloadTable = mDownloadTable;
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
