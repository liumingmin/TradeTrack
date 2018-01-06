package com.ming.trade.track.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.ming.trade.track.holder.HttpClientHolder;
import com.ming.trade.track.holder.TradeGetMethodHolder;
import com.ming.trade.track.model.GroupTrade;
import com.ming.trade.track.model.Stock;
import com.ming.trade.track.model.Trade;
import com.ming.trade.track.util.FileUtil;
import com.ming.trade.track.util.StringHelper;


public class TradeFetcher {
	private static final Log LOG = LogFactory.getLog(TradeFetcher.class);
	
	private static final String defaultUrl="http://market.finance.sina.com.cn/downxls.php?date=$date$&symbol=$market$$code$";
	
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	
	//private static final HttpClient httpclient=new HttpClient();
	
	private static final Properties prop= new Properties();
	
	{
		//load Properties
	}
	
	private static void checkEvn(){
		if(HttpClientHolder.getThreadLocal()==null){
			HttpClientHolder.setThreadLocal(new HttpClient());
		}
	}
	
	public static GroupTrade getTrades(String date,Stock stock){
		checkEvn();
		String exportFile = getTradesAsFile(date,stock);
		GroupTrade grouptrade=new GroupTrade();
		List<Trade> trades=new ArrayList<Trade>();
		Trade trade=null;
		
		List<String> lines=new ArrayList<String>();
		String linestr=null;
		InputStream is = null;
		try{
			//POIFSFileSystem fsread = new POIFSFileSystem(new FileInputStream(exportFile));
			//HSSFWorkbook workbookread=new HSSFWorkbook(fsread);
			//HSSFSheet sheet = workbookread.getSheetAt(0);
			is = new FileInputStream(exportFile);
			BufferedReader br=new BufferedReader(new InputStreamReader(is));
			while((linestr=br.readLine())!=null) { 
				lines.add(linestr); 
			}
			java.util.Collections.reverse(lines);
			
			for (int i=0;i<lines.size()-1;i++){
				trade = readRow(lines.get(i),date);
				if(trade!=null){
					trades.add(trade);
				}
			}
			grouptrade.setStock(stock);
			grouptrade.setAllTrades(trades);
			LOG.info(stock.getCode()+":"+trades.size());
			
			is.close();
			//É¾³ýÎÄ¼þ
			//new File(exportFile).delete();
		}catch(Exception e){
			LOG.error(e);
			return null;
		}finally{
			try {
				if(is!=null) {
					is.close();
					is=null;
				}
			} catch (IOException e) {
				LOG.error(e);
			}	
		}
		return grouptrade;
	}
	public static Trade readRow(String line,String date){
		Trade trade=new Trade();
		String [] cols=line.split("\t");
		try{
			trade.setTime(df.parse(date+" "+cols[0]));
			trade.setPrice(Double.parseDouble(cols[1]));
			if (!cols[2].equals("--")) {
				trade.setGap(Double.parseDouble(cols[2]));
			}else{
				trade.setGap(0);
			}
			trade.setQty(Integer.parseInt(cols[3]));
			trade.setAmount(Integer.parseInt(cols[4]));
			if(cols[5].equals("ÂòÅÌ")){
				trade.setType(Trade.TYPE_BUY);
			}else if(cols[5].equals("ÂôÅÌ")){
				trade.setType(Trade.TYPE_SELL);
			}else if(cols[5].equals("ÖÐÐÔÅÌ")){
				trade.setType(Trade.TYPE_NONE);
			}
		}catch(Exception e){
			LOG.error(e);
			//e.printStackTrace();
			return null;
		}
		return trade;
	}
	/*public static Trade readRow(HSSFSheet sheet,int rowIndex,String date){
		Trade trade=new Trade();
		HSSFRow row = sheet.getRow(rowIndex);
		try{
			trade.setTime(df.parse(date+" "+row.getCell(0).getStringCellValue()));
			trade.setPrice(Double.parseDouble(row.getCell(1).getStringCellValue()));
			trade.setGap(Double.parseDouble(row.getCell(2).getStringCellValue()));
			trade.setQty(Integer.parseInt(row.getCell(3).getStringCellValue()));
			trade.setAmount(Integer.parseInt(row.getCell(4).getStringCellValue()));
			if(row.getCell(5).getStringCellValue().equals("ÂòÅÌ")){
				trade.setType(Trade.TYPE_BUY);
			}else if(row.getCell(5).getStringCellValue().equals("ÂôÅÌ")){
				trade.setType(Trade.TYPE_SELL);
			}else if(row.getCell(5).getStringCellValue().equals("ÖÐÐÔÅÌ")){
				trade.setType(Trade.TYPE_NONE);
			}
		}catch(Exception e){
			LOG.error(e);
			return null;
		}
		return trade;
	}*/
	public static String getTradesAsFile(String date,Stock stock){
		checkEvn();
		String tmpDir = FileUtil.tmpDir();
		String exportFile=tmpDir+"/"+stock.getMarket()+stock.getCode()+date+".xls";
		if(!new File(exportFile).exists()) {
			InputStream is = getTradesAsStream(date,stock);
			if (is==null) return null;
			byte [] bytes=new byte[8192];
			int length;
			OutputStream os=null;
			try{
				os = new FileOutputStream(exportFile);
				while((length=is.read(bytes))!=-1){
					os.write(bytes, 0, length);
				}
				os.flush();
				is.close();
				os.close();
				LOG.info(exportFile);
			}catch(Exception e){
				LOG.error(e);
				return null;
			}finally{
				TradeGetMethodHolder.getThreadLocal().releaseConnection();
			}
		}
		
		return exportFile;
		//HSSFWorkbook workbookwrite=new HSSFWorkbook(istream);
	}
	public static InputStream getTradesAsStream(String date,Stock stock){
		checkEvn();
		String eastmoneyUrl=prop.getProperty("fetcherTradeUrl",defaultUrl);
		eastmoneyUrl=StringHelper.replace(eastmoneyUrl, "$date$", date);
		eastmoneyUrl=StringHelper.replace(eastmoneyUrl, "$code$", stock.getCode());
		eastmoneyUrl=StringHelper.replace(eastmoneyUrl, "$market$", stock.getMarket());
		LOG.info(eastmoneyUrl);
		GetMethod getmethod = new  GetMethod(eastmoneyUrl);
		TradeGetMethodHolder.setThreadLocal(getmethod);
		int statusCode;
		InputStream istream=null;
		//Reader sr=null;
		try{
			statusCode = HttpClientHolder.getThreadLocal().executeMethod(getmethod);
			istream = getmethod.getResponseBodyAsStream();
			//sr=new InputStreamReader(istream);
		}catch(Exception e){
			LOG.error(e);
		}finally{
			//getmethod.releaseConnection();
		}
		return istream;
	}
	public static void main(String [] arg){
		Stock stock =new Stock();
		stock.setCode("000651");
		stock.setMarket("sz");
		getTrades("2011-02-24",stock);
		//System.out.println(defaultTmpDir);
	}
}
