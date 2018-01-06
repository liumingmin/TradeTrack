package com.ming.trade.track.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Trade implements java.io.Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7589165655225839015L;
	private Date time;
	private double price;
	private double gap;
	private int qty;
	private int amount;
	private int type;
	public static final int TYPE_NONE=0;
	public static final int TYPE_BUY=1;
	public static final int TYPE_SELL=2;
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getGap() {
		return gap;
	}
	public void setGap(double gap) {
		this.gap = gap;
	}
	public int getQty() {
		return qty;
	}
	public void setQty(int qty) {
		this.qty = qty;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public String getTypeName() {
		switch(type){
			case  TYPE_NONE : return "÷––‘≈Ã";
			case  TYPE_BUY : return "¬Ú≈Ã";
			case  TYPE_SELL : return "¬Ù≈Ã";
			default : return "";
		}
	}
	@Override
	public String toString() {
		String retstr=df.format(time)+"\t"+price+"\t"+gap+"\t"+qty+"\t"+amount+"\t"+getTypeName();
		return retstr;
	}
	
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
}
