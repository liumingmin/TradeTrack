package com.ming.trade.track.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ming.trade.track.model.Callback;
import com.ming.trade.track.model.GroupTrade;
import com.ming.trade.track.model.Stock;

public class TradeFetcherService {
	
	private static final Log LOG = LogFactory.getLog(TradeFetcher.class);
	
	private  ThreadPoolExecutor taskPool = null;
	
	private Callback<GroupTrade> callback=null;
	
	private Callback<Object> execend=null;
	
	//private Map<String,CountDownLatch> countdownlatchs=new HashMap<String,CountDownLatch>();
	
	CountDownLatch countdownlatch;
	
	public TradeFetcherService(Callback<GroupTrade> callback,Callback<Object> execend){
		this.callback=callback;
		this.execend=execend;
		taskPool = new ThreadPoolExecutor(5, 5, 5,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(3000), new ThreadPoolExecutor.DiscardOldestPolicy());
	}
	/*public boolean isTerminated(){
		return taskPool.isTerminated();
	}
	public boolean isTerminating(){
		return taskPool.isTerminating();
	}
	public boolean isShutdown(){
		return taskPool.isShutdown();
	}
	public void shutdown(){
		taskPool.shutdown();
	}
	public void shutdownNow(){
		taskPool.purge();
		taskPool.shutdownNow();
	}*/
	public void pause(){
		taskPool.getQueue().drainTo(new ArrayList<Runnable>());
	}
	public boolean isPause(){
		//return taskPool.isTerminated();
		return (taskPool.getQueue().size()>0?false:true);
	}
	private class TaskRun implements Runnable{
		private Stock stock=null;
		
		private String date=null;
		
		public TaskRun(String date,Stock stock){
			this.stock=stock;
			this.date=date;
		}
		
		public void run() {
			process(date,stock);
			
		}
		
	}
	public void addAll(String date,List<Stock> stocks){
		countdownlatch = new CountDownLatch(stocks.size());
		TaskRun tr=null;
		for(int i=0;i<stocks.size();i++){
			tr=new TaskRun(date,stocks.get(i));
			taskPool.execute(tr);
		}
		afterCall();
	}
	
	public void add(String date,Stock stock){
		countdownlatch = new CountDownLatch(1);
		TaskRun tr=new TaskRun(date,stock);
		taskPool.execute(tr);
		afterCall();
	}
	
	private void afterCall(){
		taskPool.execute(new Runnable(){
			public void run() {
				try {
					countdownlatch.await();
				} catch (InterruptedException e) {
					LOG.error(e);
				}
				execend.invoke(null);
			}
		});
	}
	
	private void process(String date,Stock stock){
		GroupTrade gt=null;
		try {
			gt = TradeFetcher.getTrades(date, stock);
			Analysis.analysisByPrice(gt);
		} catch (RuntimeException e) {
			LOG.error(e);
		}finally{
			countdownlatch.countDown();
			callback.invoke(gt);
		}
	}
}
