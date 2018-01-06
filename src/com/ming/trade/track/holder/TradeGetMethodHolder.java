package com.ming.trade.track.holder;

import org.apache.commons.httpclient.methods.GetMethod;

public class TradeGetMethodHolder {
	private static  ThreadLocal<GetMethod> threadlocal=new ThreadLocal<GetMethod>();
	
	public  static void setThreadLocal(GetMethod gm){
		threadlocal.set(gm);
	}
	public  static GetMethod getThreadLocal(){
		return threadlocal.get();
	}
}
