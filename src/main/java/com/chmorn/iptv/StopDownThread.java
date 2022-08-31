package com.chmorn.iptv;

import com.chmorn.model.QueueModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author chmorn
 * @description 根据id停止下载
 * @date 2022/8/31
 **/
public class StopDownThread implements Runnable {

    private QueueModel removeModel;
    /**
     * 下载id
     **/
    private int downloadId;
    /**
     * 下载目录
     * 例如：M:/iptv/hntv_high/
     **/
    private String distPath;
    /**
     * 临时文件保存目录
     * 取值：distPath+"temp"+downloadId
     **/
    private String piecePath;
    /**
     * 合并后的结果文件
     * 存放在目录：distPath，文件名：downloadId+".ts"
     **/
    private String mergeFileName;


    private StopDownThread() {

    }

    public StopDownThread(QueueModel removeModel) {
        this.removeModel = removeModel;
        this.downloadId = removeModel.getDownloadId();
        this.distPath = removeModel.getDistPath();
        if (!this.distPath.endsWith(File.separator)) {
            this.distPath = this.distPath + File.separator;
        }
        this.piecePath = this.distPath + "temp" + downloadId + File.separator;
        this.mergeFileName = this.distPath + downloadId + ".ts";
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();//用来计算时间
        //1、停线程(先停写线程，再停读线程，防止读线程停止了，写线程还在往队列写,导致队列数据量大，内存溢出)
        //停写线程
//		removeModel.getWriteQueue().setExit(true);
//		while(true){
//			if(!removeModel.getWriteThread().isAlive()){
//				removeModel.setWriteQueue(null);
//				//停读线程
//				removeModel.getReadQueue().setExit(true);
//				if(!removeModel.getReadThread().isAlive()){
//					removeModel.setReadQueue(null);
//					break;
//				}
//			}
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
        //2、从下载列表中修改该条数据状态为停止下载
        boolean removeFlag = QueueThreadPool.stopThread(removeModel);
        if (!removeFlag) {
            System.out.println("发生异常，请检查该下载任务是否还在下载，如还在下载，请关闭客户端");
        }
        try {
            startMix();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();//用来计算时间
        long userSeconds = (end - start) / 1000;
        System.out.println("停止成功，耗时(秒):" + userSeconds + "---->" + distPath + mergeFileName);
    }

    // ts视频合并
    private void startMix() throws IOException {
        System.out.println("开始合并............." + distPath + mergeFileName);
        File dir = new File(piecePath);
        File[] files = dir.listFiles();
        //files读取可能顺序乱了，重新排序
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            list.add(files[i].getName());
        }
        Collections.sort(list);
        //排序结束
        FileInputStream fis = null;
        FileOutputStream fos = new FileOutputStream(mergeFileName);
        byte[] buffer = new byte[1024];// 一次读取1K
        int len;
        System.out.println(files.length);
        for (int i = 0; i < list.size(); i++) {
            fis = new FileInputStream(new File(piecePath + list.get(i)));
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

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public String getDistPath() {
        return distPath;
    }

    public void setDistPath(String distPath) {
        this.distPath = distPath;
    }

    public String getPiecePath() {
        return piecePath;
    }

    public void setPiecePath(String piecePath) {
        this.piecePath = piecePath;
    }

    public String getMergeFileName() {
        return mergeFileName;
    }

    public void setMergeFileName(String mergeFileName) {
        this.mergeFileName = mergeFileName;
    }
}
