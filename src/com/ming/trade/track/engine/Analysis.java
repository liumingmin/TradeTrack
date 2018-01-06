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
	
	private static final double bulkrate=0.2;//��ռ������
	
	private static final double nearrate=0.9;//�󵥽ӽ�����
	
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
		double rate2=0.0;//������
		
		trades.remove(trades.size()-1);//ȥ��β��
		if((trades.get(0).getQty()/10000)<nearrate) trades.remove(0);//ȥ��ͷ��
		
		for(int i=0;i<trades.size();i++){
			isbulkgap=false;
			rate = (trades.get(i).getQty()+0.0)/grouptrade.getAllTradesQty();
			rate2 = (trades.get(i).getQty()+0.0)/bulkqty;
			if(rate>bulkrate) {//ռ��
				parttrade = new ArrayList<Trade>();
				for(int j=stock.getBulkPart();j>=-stock.getBulkPart();j--){//�󵥸�������
					if(i-j>=0 && i-j<=trades.size()-1){
						parttrade.add(trades.get(i-j));
					}
				}
				grouptrade.getBulkTrades().add(parttrade);
				stock.setBulkGap(true);
			}else if(rate2>=nearrate){
				parttrade = new ArrayList<Trade>();
				//�����ؿ�
				/*if(trades.get(i).getPrice()==Math.floor(trades.get(i).getPrice())){
					stock.setIntQty(true);
					isbulkgap=true;
					for(int j=stock.getBulkPart();j>=-stock.getBulkPart();j--){//�󵥸�������
						if(i-j>=0 && i-j<=trades.size()-1){
							//gap = trades.get(i-j).getPrice()-trades.get(i).getPrice();
							parttrade.add(trades.get(i-j));
						}
					}
				}else{*/
					for(int j=stock.getBulkPart();j>=-stock.getBulkPart();j--){//�󵥸�������
						if(i-j>=0 && i-j<=trades.size()-1){
							gap = trades.get(i-j).getPrice()-trades.get(i).getPrice();
							if(-gap>=bulkgap){//��ֵ���ڻ���ڴ���
								isbulkgap=true;
							}
							parttrade.add(trades.get(i-j));
						}
					}
				//}
				
				if(isbulkgap){//���ڴ���
					grouptrade.getBulkTrades().add(parttrade);
					stock.setBulkGap(true);
					if(trades.get(i).getPrice()==Math.floor(trades.get(i).getPrice())){//�����ؿ����ȱ�־
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
