package com.chmorn.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行命令：目前支持：ts文件提取视频、提取音频、合并视频音频
 * 命令格式：   D:/tools/ffmpeg/bin/ffmpeg.exe -i D:/iqyvideo/iqyvideo/迷城/new/mix.265ts -vcodec copy -an D:/iqyvideo/iqyvideo/迷城/new/mix.hevc
			D:/tools/ffmpeg/bin/ffmpeg.exe -i D:/iqyvideo/iqyvideo/迷城/new/mix.265ts -acodec copy -vn D:/iqyvideo/iqyvideo/迷城/new/mix.aac
			D:/tools/ffmpeg/bin/ffmpeg.exe -i D:/iqyvideo/iqyvideo/迷城/new/mix.hevc -i D:/iqyvideo/iqyvideo/迷城/new/mix.aac -c copy D:/iqyvideo/iqyvideo/迷城/new/mix.mp4
 * @author chenxu
 * @date 20210624
 */
public class ProcessCommandUtil {
	private String command;//所执行命令
	private boolean processSuccess;//执行命令是否成功
	private String processMsg;//异常信息
	private String successMsg;//正常信息
	private Map<String,String> vtype=new HashMap<>();
	/**
	 * @param command 命令字符串
	 */
	public ProcessCommandUtil(String command) {
		this.command = command;
		this.processSuccess = true;
		this.processMsg = "";
		this.successMsg = "";
	}
	public void start() {
		System.out.println("执行命令开始："+this.command);
		Runtime rt = Runtime.getRuntime();
		long start = System.currentTimeMillis();
		try {
			Process process = rt.exec(command);
			InputStream is1 = process.getInputStream();
			InputStream is2 = process.getErrorStream();
			ReadProcess read1 = new ReadProcess(is1);
			new Thread(read1).start();
			ReadProcess read2 = new ReadProcess(is2);
			new Thread(read2).start();
			process.waitFor();
			if(read1.isReadSuccess() && read2.isReadSuccess()) {
				processSuccess = true;
				successMsg = read1.getSuccMsg();
			} else {
				processSuccess = false;
				processMsg = read1.getReadMsg()+" | "+read1.getReadMsg();
			}
			long end = System.currentTimeMillis();
			System.out.println("执行命令结束：耗时秒：" +(end - start)/1000);
		} catch (Exception e) {
			System.out.println("执行命令异常："+e.getMessage());
			processSuccess = false;
			if(!"".equals(processMsg)) {
				processMsg = processMsg + " | " + e.getMessage();
			}else {
				processMsg = e.getMessage();
			}
		}
	}
	
	/**
	 * @return 是否成功
	 */
	public boolean isProcessSuccess() {
		return processSuccess;
	}
	/**
	 * @return 异常信息
	 */
	public String getProcessMsg() {
		return processMsg;
	}

	public Map<String, String> getVtype() {
		return vtype;
	}

	public String getSuccessMsg() {
		return successMsg;
	}

	/**
	  * 读取命令流
	 * @author chenxu
	 */
	class ReadProcess implements Runnable{

		private InputStream inputStream;
		private boolean readSuccess;//是否读取成功
		private String readMsg;//异常信息
		private String succMsg;//正常信息
		public ReadProcess(InputStream inputStream) {
			this.inputStream = inputStream;
			this.readSuccess = true;
			this.readMsg = "";
			this.succMsg = "";
		}
		@Override
		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				String tmp = null;
				while ((tmp=br.readLine()) != null) {
					succMsg += tmp;//不要换行，否则生成ckey出错
					System.out.println(tmp);
					int s = -1;
					if(( s= tmp.indexOf("Video"))!=-1) {
						vtype.put("Video", tmp.substring(s).split(" ")[1]);
					}
					if(( s= tmp.indexOf("Audio"))!=-1) {
						vtype.put("Audio", tmp.substring(s).split(" ")[1]);
					}
				}
			} catch (Exception e) {
				System.out.println("执行命令异常："+e.getMessage());
				readSuccess = false;
				readMsg = e.getMessage();
			}
		}
		
		public boolean isReadSuccess() {
			return readSuccess;
		}
		public String getReadMsg() {
			return readMsg;
		}

		public String getSuccMsg() {
			return succMsg;
		}
	}

}
