package com.ming.trade.track.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ming.trade.track.model.GroupTrade;
import com.ming.trade.track.model.Stock;
import com.ming.trade.track.model.Trade;

public class Analysis {
	
	private static final Log LOG = LogFactory.getLog(Analysis.class);
	
	private static final double bulkrate=0.2;//大单占总数比
	
	private static final double nearrate=0.9;//大单接近比例
	
	public static void analysisByGap(GroupTrade grouptrade){
		
	}
	
	
	public static void analysisByPrice(GroupTrade grouptrade){
		grouptrade.getBulkTrades().clear();
		List<Trade> trades = grouptrade.getAllTrades();
		Stock stock = grouptrade.getStock();
		double bulkgap=stock.getBulkGap();
		double bulkqty=stock.getBulkQty();
		List<Trade> parttrade=null;
		double gap = 0.0;
		boolean isbulkgap=false;
		double rate=0.0;
		double rate2=0.0;//大单相似
		
		trades.remove(trades.size()-1);//去掉尾单
		if((trades.get(0).getQty()/10000)<nearrate) trades.remove(0);//去掉头单
		
		for(int i=0;i<trades.size();i++){
			isbulkgap=false;
			rate = (trades.get(i).getQty()+0.0)/grouptrade.getAllTradesQty();
			rate2 = (trades.get(i).getQty()+0.0)/bulkqty;
			if(rate>bulkrate) {//占比
				parttrade = new ArrayList<Trade>();
				for(int j=stock.getBulkPart();j>=-stock.getBulkPart();j--){//大单附近交易
					if(i-j>=0 && i-j<=trades.size()-1){
						parttrade.add(trades.get(i-j));
					}
				}
				grouptrade.getBulkTrades().add(parttrade);
				stock.setBulkGap(true);
			}else if(rate2>=nearrate){
				parttrade = new ArrayList<Trade>();
				//整数关口
				/*if(trades.get(i).getPrice()==Math.floor(trades.get(i).getPrice())){
					stock.setIntQty(true);
					isbulkgap=true;
					for(int j=stock.getBulkPart();j>=-stock.getBulkPart();j--){//大单附近交易
						if(i-j>=0 && i-j<=trades.size()-1){
							//gap = trades.get(i-j).getPrice()-trades.get(i).getPrice();
							parttrade.add(trades.get(i-j));
						}
					}
				}else{*/
					for(int j=stock.getBulkPart();j>=-stock.getBulkPart();j--){//大单附近交易
						if(i-j>=0 && i-j<=trades.size()-1){
							gap = trades.get(i-j).getPrice()-trades.get(i).getPrice();
							if(-gap>=bulkgap){//差值大于或等于大差价
								isbulkgap=true;
							}
							parttrade.add(trades.get(i-j));
						}
					}
				//}
				
				if(isbulkgap){//存在大差价
					grouptrade.getBulkTrades().add(parttrade);
					stock.setBulkGap(true);
					if(trades.get(i).getPrice()==Math.floor(trades.get(i).getPrice())){//整数关口优先标志
						stock.setIntQty(true);
					}
				}else{
					parttrade.clear();
					parttrade=null;
				}
			}
			
		}
		LOG.info(grouptrade.bulkTradesToString());
	}
	
	public static void main(String [] arg){
		Stock stock =new Stock();
		stock.setCode("002275");
		analysisByPrice(TradeFetcher.getTrades("2011-05-13",stock));
	}
}
