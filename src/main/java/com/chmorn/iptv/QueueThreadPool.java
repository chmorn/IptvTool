package com.chmorn.iptv;

import com.chmorn.model.QueueModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chmorn
 * @description 存放开启的下载线程，每个下载add一个Map，每个map含write和read两个线程
 * @date 2022/8/31
 **/
public class QueueThreadPool {
    //下载线程池
    private static List<QueueModel> queueThreads;

    private QueueThreadPool() {
    }

    public static synchronized List<QueueModel> getQueueThreads() {
        if (queueThreads == null) {
            return new ArrayList<QueueModel>(4);
        }
        return queueThreads;
    }

    public static synchronized boolean removeThread(QueueModel removeModel) {
        try {
            queueThreads.remove(removeModel);
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    public static synchronized boolean stopThread(QueueModel removeModel) {
        try {
            removeModel.setState(1);
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    public static synchronized void addQueueThreads(QueueModel model) {
        if (queueThreads == null) {
            queueThreads = new ArrayList<QueueModel>();
        }
        queueThreads.add(model);
    }

}
