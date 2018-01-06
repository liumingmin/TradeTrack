package com.ming.trade.track.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.List;


import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ming.trade.track.util.StringHelper;


public class Stock implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1826795055715422952L;
	private static final Log LOG = LogFactory.getLog(Stock.class);
	private String code;
	private String name;
	private String market;
	private double price;
	private String capital;//股本
	private String worth;//市值
	private double pe;
	private double bulkGap=0.1;//与价格的关系?
	private double bulkQty=500;
	private int bulkPart=3;
	//纠正方法
	//1.大量单后，无连续中量卖单，无同样价格虾米买单。
	private boolean isBulkGap=false;
	private boolean isIntQty=false;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
		if(code.startsWith("002") || code.startsWith("000") || code.startsWith("3")){
			setMarket("sz");
		}else if(code.startsWith("6")){
			setMarket("sh");
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public String getCapital() {
		return capital;
	}
	public void setCapital(String capital) {
		this.capital = capital;
	}
	public String getWorth() {
		return worth;
	}
	public void setWorth(String worth) {
		this.worth = worth;
	}
	
	public double getBulkGap() {
		return bulkGap;
	}
	public void setBulkGap(double bulkGap) {
		this.bulkGap = bulkGap;
	}
	public double getBulkQty() {
		return bulkQty;
	}
	public void setBulkQty(double bulkQty) {
		this.bulkQty = bulkQty;
	}
	public int getBulkPart() {
		return bulkPart;
	}
	public void setBulkPart(int bulkPart) {
		this.bulkPart = bulkPart;
	}
	public boolean isBulkGap() {
		return isBulkGap;
	}
	public void setBulkGap(boolean isBulkGap) {
		this.isBulkGap = isBulkGap;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
		double p=0.1;
		String key=null;
		for(int i=0;i<price_gap.size();i++){
			key = (String)price_gap.get(i);
			p = Double.parseDouble(key);
			if(price<p){
				bulkGap=Double.parseDouble((String)price_gap.get(key));
				break;
			}
		}
		//LOG.info("差价："+bulkGap);
		for(int i=0;i<price_qty.size();i++ ){
			key = (String)price_qty.get(i);
			p = Double.parseDouble(key);
			if(price<p){
				bulkQty=Double.parseDouble((String)price_qty.get(key));
				break;
			}
		}
		//LOG.info("大单："+bulkQty);
		
		/*if(price<5){
			bulkGap = 5;//忽略5元以下
		}else if(price<15){
			bulkGap = 0.2;
		}else if (price<20){
			bulkGap = 0.3;
		}else if (price<30){
			bulkGap = 0.5;
		}else {
			bulkGap = 1;
		}*/
		
		/*if(price<5){
			bulkQty = 10000;
		}else if(price <10){
			bulkQty = 5000;
		}else if(price<15){
			bulkQty = 1000;
		}else if(price<20){
			bulkQty = 1000;
		}else {
			bulkQty = 500;
		}*/
	}
	public double getPe() {
		return pe;
	}
	public void setPe(double pe) {
		this.pe = pe;
	}
	
	public static Stock getStockByCode(List<Stock> stocks, String code){
		Stock stock=null;
		for(int i=0;i<stocks.size();i++){
			if(stocks.get(i).getCode().equals(code)){
				stock = stocks.get(i);
				break;
			}
		}
		return stock;
	}
	
	private static ListOrderedMap price_qty=new ListOrderedMap();
	private static ListOrderedMap price_gap=new ListOrderedMap();
	
	static {
		loadQtyGapConf();
	}
	private static void loadQtyGapConf(){
		String cpath = null;
		BufferedReader  qtyreader=null;
		BufferedReader gapreader=null;
		String line;
		String[] pair;
		try {
			cpath = Stock.class.getClassLoader().getResource("").getPath();
		} catch (Exception e) {
			LOG.error(e);
		}
		try {
			if(cpath!=null){
				qtyreader=new BufferedReader(new FileReader(cpath+"/price-qty.properties"));
				gapreader=new BufferedReader(new FileReader(cpath+"/price-gap.properties"));
			}else{
				qtyreader=new BufferedReader(new FileReader("./price-qty.properties"));
				gapreader=new BufferedReader(new FileReader("./price-gap.properties"));
			}
			while((line = qtyreader.readLine()) != null){   
				
				pair = line.split("=");
				if(pair.length<2 || StringHelper.isEmpty(pair[0]) || StringHelper.isEmpty(pair[1])) continue;
				price_qty.put(pair[0].trim(), pair[1].trim());
			} 
			while((line=gapreader.readLine()) !=null){   
				pair = line.split("=");
				if(pair.length<2 || StringHelper.isEmpty(pair[0]) || StringHelper.isEmpty(pair[1])) continue;
				price_gap.put(pair[0].trim(), pair[1].trim());
			} 
			
		} catch (Exception e) {
			//e.printStackTrace();
			LOG.error(e);
		}
		
	}
	
	public static void main(String []arg){
		Stock s=new Stock();
		s.setPrice(17);
	}
	public boolean isIntQty() {
		return isIntQty;
	}
	public void setIntQty(boolean isIntQty) {
		this.isIntQty = isIntQty;
	}
}
