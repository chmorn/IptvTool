package com.chmorn.base;

/**
 * @author chmorn
 * @description 获取下载状态值
 * @date 2022/8/31
 **/
public enum DownloadStateEnum {
    SUCC(0, "进行中"),
    FAIL(1, "完成"),
    DISTPATH_NULL(2, "停止中"),
    OTHER(9, "其他");

    private int state;
    private String msg;

    DownloadStateEnum(int state, String msg) {
        this.state = state;
        this.msg = msg;
    }

    public static String getMsg(int state) {
        for (DownloadStateEnum tc : DownloadStateEnum.values()) {
            if (tc.state == state) {
                return tc.msg;
            }
        }
        return null;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
