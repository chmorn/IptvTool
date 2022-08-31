package com.chmorn.model;

/**
 * @author chmorn
 * @description 返回信息
 * @date 2022/8/31
 **/
public class ResponseModel {

    /**
     * 下载id
     **/
    private int downloadId;
    /**
     * m3u8地址
     **/
    private String m3u8url;
    /**
     * 保存目录
     **/
    private String distPath;
    /**
     * 定时开始时间
     **/
    private String timeStart;
    /**
     * 定时结束时间
     **/
    private String timeEnd;
    /**
     * 状态:0-下载中，1-完成
     **/
    private int state;
    private String stateValue;

    public ResponseModel() {
    }

    public ResponseModel(int downloadId, String m3u8url, String distPath, String timeStart, String timeEnd, int state) {
        this.downloadId = downloadId;
        this.m3u8url = m3u8url;
        this.distPath = distPath;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.state = state;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public String getM3u8url() {
        return m3u8url;
    }

    public void setM3u8url(String m3u8url) {
        this.m3u8url = m3u8url;
    }

    public String getDistPath() {
        return distPath;
    }

    public void setDistPath(String distPath) {
        this.distPath = distPath;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateValue() {
        return stateValue;
    }

    public void setStateValue(String stateValue) {
        this.stateValue = stateValue;
    }
}
