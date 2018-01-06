package com.ming.trade.track.engine;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ming.trade.track.model.Callback;
import com.ming.trade.track.util.StringHelper;

public class ThreadDownloadService {
	
	private static final Log LOG = LogFactory.getLog(ThreadDownloadService.class);
	
	private  ThreadPoolExecutor taskPool = null;
	
	private byte[] buf = new byte[8192];
	//private CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
	private Map<String,CountDownLatch> countdownlatchs=new HashMap<String,CountDownLatch>();
	
	private ThreadDownloadService(){
		taskPool = new ThreadPoolExecutor(10, 10, 5,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(20), new ThreadPoolExecutor.DiscardOldestPolicy());
		
	}
	private static ThreadDownloadService service=null;
	
	public static ThreadDownloadService getInstance(){
		if(service==null){
			service=new ThreadDownloadService();
		}
		return service;
	}
	
	public void add(String address,RandomAccessFile file,int threadcount){
		add( address, file, threadcount, null);
	}
	public void add(String address,final RandomAccessFile file,int threadcount,final Callback<RandomAccessFile> callback){
		long size=getFileSize(address);
		long blocksize=size/threadcount+1;
		
		final String tid=StringHelper.uuid();
		countdownlatchs.put(tid, new CountDownLatch(threadcount));
		
		long pos=0;
		long endpos;
		Runnable r=null;
		for(int i=0;i<threadcount;i++){
			if(pos>=size) break;
			endpos = pos+blocksize-1;
			endpos = endpos>(size-1)?(size-1):endpos;
			r=new DownloadRun(address,file,pos,endpos,tid);
			taskPool.execute(r);
			pos=endpos+1;
		}
		
		
		if(callback==null){
			try {
				countdownlatchs.get(tid).await();
				file.close();
			} catch (Exception e) {
				LOG.error(e);
			}
		}else{
			taskPool.execute(new Runnable(){
				public void run() {
					try {
						countdownlatchs.get(tid).await();
						file.close();
						callback.invoke(file);
					} catch (Exception e) {
						LOG.error(e);
					}
				}
			});
		}
		
		
	}
	private class DownloadRun implements Runnable{
		private String address=null;
		private RandomAccessFile file=null;
		private long pos;
		private long endpos;
		private String tid=null;
		
		public DownloadRun(String address,RandomAccessFile file,long pos,long endpos,String tid){
			this.address=address;
			this.file=file;
			this.pos=pos;
			this.endpos=endpos;
			this.tid=tid;
		}
		public void run() {
			try {
				download(address,file,pos,endpos);
				countdownlatchs.get(tid).countDown();
				
			} catch (IOException e) {
				LOG.error(e);
			}
		}
		
	}
	private void download(String address,RandomAccessFile file,long pos,long endpos) throws IOException{
		URL url = new URL(address);
		HttpURLConnection cn = (HttpURLConnection) url.openConnection();
		cn.setAllowUserInteraction(true);
		//因为有些网站根据它判断是否是盗链接
        //cn.setRequestProperty("Referer", "www.sina.com");
		//设置请求的起始和结束位置。
		cn.setRequestProperty("Range", "bytes=" + pos + "-" + endpos);
		while(cn.getResponseCode() != 200 && cn.getResponseCode() != 206){
			LOG.error("request error,retry...");
			download( address, file, pos, endpos) ;
			return ;
		}
		LOG.info("download block "+pos+":"+endpos);
		InputStream bis = new BufferedInputStream(cn.getInputStream());
		int len;
		while ((len = bis.read(buf)) > 0 ) {
            synchronized (file) {
            	file.seek(pos);
                file.write(buf, 0, len);
                LOG.info("write range:"+pos+":"+(pos+len));
            }
            pos += len;
        }
	}
	public long getFileSize(String address){
		URL url=null;
		URLConnection cn=null;
		long size=0;
		try {
			url = new URL(address);
			cn = url.openConnection();
			size = cn.getContentLength();
			//if(size<1024) size = cn.getHeaderFieldInt("Content-Length", 0);
			LOG.info("file size"+size);
		} catch (Exception e) {
			LOG.error(e);
		}
         
		return  size;
	}
	
	public static void main(String [] arg) throws FileNotFoundException{
		ThreadDownloadService service =new ThreadDownloadService();
		service.add("http://data.share.jrj.com.cn/stocks/download/AguHq.xls", new RandomAccessFile("c:\\AguHq.xls","rw"), 2,new Callback<RandomAccessFile>(){
			public void invoke(RandomAccessFile file) {
				System.out.println("============================invoke end");
			}
		});
		/*new FileCallback(){
			public void invoke(RandomAccessFile file) {
				System.out.println("============================invoke end");
			}
		}*/
		System.out.println("========================main end");
	}
}
