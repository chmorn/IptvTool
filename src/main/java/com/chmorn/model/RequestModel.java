package com.chmorn.model;

/**
 * @author chmorn
 * @description 请求信息
 * @date 2022/8/31
 **/
public class RequestModel {

    private String m3u8url;
    private String distPath;
    private String timeStart;
    private String timeEnd;

    public RequestModel() {
    }

    public RequestModel(String m3u8url, String distPath, String timeStart, String timeEnd) {
        this.m3u8url = m3u8url;
        this.distPath = distPath;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
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
}
