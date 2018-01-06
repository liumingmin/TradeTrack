package com.ming.trade.track.model;


import java.util.ArrayList;
import java.util.List;


public class GroupTrade implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3592360839733273053L;
	Stock stock;
    List<Trade> allTrades;
    List<List<Trade>> bulkTrades=new ArrayList<List<Trade>>();//大单交易局部 集合
    long allTradesQty=0;//总量
    
    public void calcuQty(List<Trade> allTrades){
    	allTradesQty=0;
    	for(int i=0;i<allTrades.size();i++){
    		allTradesQty+=allTrades.get(i).getQty();
    	}
    }
    
    public GroupTrade(){
    	
    }
    public GroupTrade(Stock stock, List<Trade> allTrades) {
        this.stock = stock;
        this.allTrades = allTrades;
        calcuQty(allTrades);
    }
	public Stock getStock() {
		return stock;
	}
	public void setStock(Stock stock) {
		this.stock = stock;
	}
	public List<Trade> getAllTrades() {
		return allTrades;
	}
	public void setAllTrades(List<Trade> allTrades) {
		this.allTrades = allTrades;
		calcuQty(allTrades);
	}
	public List<List<Trade>> getBulkTrades() {
		return bulkTrades;
	}
	public void setBulkTrades(List<List<Trade>> bulkTrades) {
		this.bulkTrades = bulkTrades;
	}
	
	public String bulkTradesToString(){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<bulkTrades.size();i++){
			sb.append("-----------------------------------\n");
			for(int j=0;j<bulkTrades.get(i).size();j++){
				sb.append(bulkTrades.get(i).get(j).toString());
				sb.append("\n");
			}
			sb.append("-----------------------------------\n");
		}
		return sb.toString();
	}

	public long getAllTradesQty() {
		return allTradesQty;
	}
    
}
