package com.ming.trade.track.holder;

import org.apache.http.client.methods.HttpGet;

public class StockGetMethodHolder {
	private static  ThreadLocal<HttpGet> threadlocal=new ThreadLocal<HttpGet>();
	
	public  static void setThreadLocal(HttpGet gm){
		threadlocal.set(gm);
	}
	public  static HttpGet getThreadLocal(){
		return threadlocal.get();
	}
}
