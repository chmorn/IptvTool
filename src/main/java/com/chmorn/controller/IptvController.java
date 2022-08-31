package com.chmorn.controller;

import com.chmorn.base.ResultApi;
import com.chmorn.iptv.*;
import com.chmorn.model.QueueModel;
import com.chmorn.utils.DownloadUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author chmorn
 * @description 下载iptv
 * @date 2022/8/31
 **/
@RestController
@RequestMapping(value = "/iptv")
public class IptvController {

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-d H:m");

    //查询下载列表
    @GetMapping(value = "/list")
    @ApiOperation(value = "查询下载列表")
    public List<QueueModel> listDownload() {
        List<QueueModel> list = QueueThreadPool.getQueueThreads();
        return list;
    }

    @GetMapping(value = "/record")
    @ApiOperation(value = "录制")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "m3u8url", value = "m3u8地址'", dataType = "String", required = true, defaultValue = "http://39.135.138.59:18890/PLTV/88888910/224/3221225659/index.m3u8"),
            @ApiImplicitParam(name = "distPath", value = "保存目录", dataType = "String", required = true, defaultValue = "C:/dvb"),
            @ApiImplicitParam(name = "startTime", value = "开始时间，格式yyyy-M-d H:m", dataType = "String", required = true),
            @ApiImplicitParam(name = "endTime", value = "结束时间，格式yyyy-M-d H:m", dataType = "String", required = true)
    })
    public ResultApi record(String m3u8url, String distPath, String startTime, String endTime) {
        //校验参数是否为空
        if (StringUtils.isEmpty(m3u8url) || StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            return ResultApi.failure("参数为空");
        }
        //校验参数合法性
        ResultApi check = checkParams(m3u8url, distPath, startTime, endTime);
        if (!check.getSuccess()) {
            return check;
        }
        //RequestModel model = new RequestModel(m3u8, distPath, startTime, endTime);
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(16);
        //获取id
        int newId = DownloadUtils.getDownloadId();
        QueueModel model = new QueueModel(newId, m3u8url, distPath, startTime, endTime, 0);
        //启动线程
        WriteQueue writeQueue = new WriteQueue(queue, m3u8url, newId, startTime, endTime);
        Thread writeThread = new Thread(writeQueue);
        writeThread.start();
        ReadQueue readQueue = new ReadQueue(queue, model, distPath, newId, startTime, endTime);
        Thread readThread = new Thread(readQueue);
        readThread.start();

        //将新线程添加到下载线程池
        QueueThreadPool.addQueueThreads(model);
        return ResultApi.success();
    }

    //停止下载
    @GetMapping(value = "/stop")
    @ApiOperation(value = "停止任务")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "downloadId", value = "下载id'", dataType = "String", required = true)
    })
    public ResultApi stopDownload(String downloadId) {
        if (DownloadUtils.isNull(downloadId)) {
            return ResultApi.failure("error!id为空");
        }
        int id = -1;
        try {
            id = Integer.parseInt(downloadId);
        } catch (NumberFormatException e) {
            return ResultApi.failure("id不存在");
        }
        List<QueueModel> queueThreads = QueueThreadPool.getQueueThreads();
        for (int i = 0; i < queueThreads.size(); i++) {
            if (queueThreads.get(i).getDownloadId() == id) {
                QueueModel removeModel = queueThreads.get(i);
                //启动停止线程后台慢慢停止，然后跳出循环返回前端结果
                new Thread(new StopDownThread(removeModel)).start();
                break;
            }
        }
        return ResultApi.success("后台正在停止下载，请稍后查看下载列表");
    }

    /**
     * 校验录制参数是否合法
     **/
    private ResultApi checkParams(String m3u8url, String distPath, String startTime, String endTime) {
        //1、校验时间
        try {
            LocalDateTime start = LocalDateTime.parse(startTime, dateTimeFormatter);
            LocalDateTime end = LocalDateTime.parse(endTime, dateTimeFormatter);
            LocalDateTime now = LocalDateTime.now();
            if (start.isAfter(end)) {
                return ResultApi.failure("开始时间需在结束时间之前");
            }
            if (end.isBefore(now)) {
                return ResultApi.failure("结束时间已过去，请重新选择");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultApi.failure("时间格式错误，请检查，正确格式为：yyyy-M-d H:m");
        }
        //2、校验保存路径
        try {
            if (!new File(distPath).exists()) {
                return ResultApi.failure("保存目录校验失败，非正确的路径");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultApi.failure("保存目录校验失败，非正确的路径");
        }
        //3、校验m3u8地址
        try {
            URL url = new URL(m3u8url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setConnectTimeout(1000);
            int status = conn.getResponseCode();
            if (status != 200) {
                return ResultApi.failure("m3u8地址有误，连接异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultApi.failure("m3u8地址连接超时");
        }
        return ResultApi.success();
    }

    //回录
    @GetMapping(value = "/back")
    @ApiOperation(value = "回录")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "m3u8url", value = "m3u8地址'", dataType = "String", required = true, defaultValue = "http://39.135.138.59:18890/PLTV/88888910/224/3221225659/index.m3u8"),
            @ApiImplicitParam(name = "distPath", value = "保存目录", dataType = "String", required = true, defaultValue = "C:/dvb"),
            @ApiImplicitParam(name = "startTime", value = "开始时间，格式yyyy-M-d H:m", dataType = "String", required = true),
            @ApiImplicitParam(name = "endTime", value = "结束时间，格式yyyy-M-d H:m", dataType = "String", required = true)
    })
    public ResultApi backRecord(String m3u8url, String distPath, String startTime, String endTime) {
        //校验参数是否为空
        if (StringUtils.isEmpty(m3u8url) || StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
            return ResultApi.failure("参数为空");
        }
        //校验参数合法性
        ResultApi check = checkBackParams(m3u8url, distPath, startTime, endTime);
        if (!check.getSuccess()) {
            return check;
        }
        //获取id
        int newId = DownloadUtils.getDownloadId();
        QueueModel model = new QueueModel(newId, m3u8url, distPath, startTime, endTime, 0);
        //启动线程
        BackDownloadThread backDownloadThread = new BackDownloadThread(model, distPath, newId, m3u8url, startTime, endTime);
        Thread backThread = new Thread(backDownloadThread);
        backThread.start();

        //将新线程添加到下载线程池
        QueueThreadPool.addQueueThreads(model);
        return ResultApi.success();
    }

    /**
     * 校验回录参数是否合法
     **/
    private ResultApi checkBackParams(String m3u8url, String distPath, String startTime, String endTime) {
        //1、校验时间
        try {
            LocalDateTime start = LocalDateTime.parse(startTime, dateTimeFormatter);
            LocalDateTime end = LocalDateTime.parse(endTime, dateTimeFormatter);
            LocalDateTime now = LocalDateTime.now();
            if (start.isAfter(end)) {
                return ResultApi.failure("开始时间需在结束时间之前");
            }
            if (end.isAfter(now)) {
                return ResultApi.failure("结束时间需是过去时间，请重新选择");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultApi.failure("时间格式错误，请检查，正确格式为：yyyy-M-d H:m");
        }
        //2、校验保存路径
        try {
            if (!new File(distPath).exists()) {
                return ResultApi.failure("保存目录校验失败，非正确的路径");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultApi.failure("保存目录校验失败，非正确的路径");
        }
        //3、校验m3u8地址
        try {
            URL url = new URL(m3u8url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setConnectTimeout(1000);
            int status = conn.getResponseCode();
            if (status != 200) {
                return ResultApi.failure("m3u8地址有误，连接异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultApi.failure("m3u8地址连接超时");
        }
        return ResultApi.success();
    }

}
