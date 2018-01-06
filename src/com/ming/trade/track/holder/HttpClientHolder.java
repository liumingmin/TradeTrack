package com.ming.trade.track.holder;

import org.apache.commons.httpclient.HttpClient;


public class HttpClientHolder {
	
	private static  ThreadLocal<HttpClient> threadlocal=new ThreadLocal<HttpClient>();
	
	public  static void setThreadLocal(HttpClient gm){
		threadlocal.set(gm);
	}
	public  static HttpClient getThreadLocal(){
		return threadlocal.get();
	}
}
