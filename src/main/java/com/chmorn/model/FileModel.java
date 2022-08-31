package com.chmorn.model;

import java.io.InputStream;
import java.net.URL;

/**
 * @author chmorn
 * @description 文件类
 * @date 2022/8/31
 **/
public class FileModel {

    private String fileName;
    private URL url;

    public FileModel() {
    }

    public FileModel(String fileName, URL url) {
        this.fileName = fileName;
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
