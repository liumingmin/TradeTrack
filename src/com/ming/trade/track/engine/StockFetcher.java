package com.ming.trade.track.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ming.trade.track.holder.StockGetMethodHolder;
import com.ming.trade.track.model.Stock;
import com.ming.trade.track.util.FileUtil;
import com.ming.trade.track.util.StringHelper;

public class StockFetcher {
	
	private static final Log LOG = LogFactory.getLog(StockFetcher.class);
	
	private static final String defaultUrl="http://data.share.jrj.com.cn/stocks/download/AguHq.xls";
	
	private static final String defalutUrl2= "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx/JS.aspx?type=ct&st=(BalFlowMain)&sr=-1&p=0&ps=4000&js=[(x)]&token=894050c76af8597a853f5b408b759f5d&cmd=C._AB&sty=DCFFITA&rt=50473629";
	private static final HttpClient httpClient = new DefaultHttpClient();  
	
	private static final Properties prop= new Properties();
	
	{
		//load Properties
	}
	
	public static List<Stock> getStocks(){
		String exportFile = getStocksAsFile();
		List<Stock> stocks=new ArrayList<Stock>();
		Stock stock=null;
		try{
			POIFSFileSystem fsread = new POIFSFileSystem(new FileInputStream(exportFile));
			HSSFWorkbook workbookread=new HSSFWorkbook(fsread);
			HSSFSheet sheet = workbookread.getSheetAt(0);
			int end=sheet.getLastRowNum();
			
			for (int i=0;i<end;i++){
				stock=readRow(sheet,end-i);
				if(stock!=null){
					stocks.add(stock);
				}
			}
			LOG.info(stocks.size());
			
			//删除文件
			//new File(exportFile).delete();
		}catch(Exception e){
			LOG.error(e);
			return null;
		}finally{
			
		}
		return stocks;
	}
	
	public static List<Stock> getStocks2(){
		String exportFile = getStocksAsFile();
		List<Stock> stocks=new ArrayList<Stock>();
		Stock stock=null;
		try{
			byte[] bs = Files.readAllBytes(Paths.get(exportFile));
			String str = new String(bs,"utf-8");
			//String jsonstr = str.substring(str.indexOf("=")+1);
			
			//JSONObject alldata = JSON.parseObject(str);
			JSONArray dataarray = JSON.parseArray(str);

			for (int i=0;i<dataarray.size();i++){
				
				String stockitemstr= dataarray.getString(i);
				String[] stockitemarray = stockitemstr.split(",");
				
				stock=readRow2(stockitemarray);
				if(stock!=null){
					stocks.add(stock);
				}
			}
			LOG.info(stocks.size());
			
			//删除文件
			//new File(exportFile).delete();
		}catch(Exception e){
			LOG.error(e);
			return null;
		}finally{
			
		}
		return stocks;
	}
	
	public static Stock readRow2(String[] stockitemarray){
		Stock stock=new Stock();

		try{
			stock.setCode(stockitemarray[1]);
			stock.setName(stockitemarray[2]);
			stock.setPrice(Double.parseDouble(stockitemarray[3]));
			stock.setPe(00);
		}catch(Exception e){
			LOG.error(e);
			return null;
		}
		return stock;
	}
	
	public static Stock readRow(HSSFSheet sheet,int rowIndex){
		Stock stock=new Stock();
		HSSFRow row = sheet.getRow(rowIndex);
		try{
			stock.setCode(row.getCell(1).getStringCellValue());
			stock.setName(row.getCell(0).getStringCellValue());
			stock.setPrice(Double.parseDouble(row.getCell(3).getStringCellValue()));
			stock.setPe(Double.parseDouble(row.getCell(14).getStringCellValue()));
		}catch(Exception e){
			LOG.error(e);
			return null;
		}
		return stock;
	}
	public static String getStocksAsFile(){
		String tmpDir = FileUtil.tmpDir();
		String exportFile=tmpDir+"/stock.dat";
		if(!new File(exportFile).exists()) {
			InputStream is = getStocksAsStream();
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
				StockGetMethodHolder.getThreadLocal().releaseConnection();
			}
		}
		
		return exportFile;
	}
	public static InputStream getStocksAsStream(){
		String eastmoneyUrl=prop.getProperty("fetcherStockUrl",defalutUrl2);
		LOG.info(eastmoneyUrl);
		//GetMethod getmethod = new  GetMethod(eastmoneyUrl);
		HttpGet httpget = new HttpGet(eastmoneyUrl);  
		StockGetMethodHolder.setThreadLocal(httpget);
		int statusCode;
		InputStream istream=null;
		//Reader sr=null;
		try{
			
			
			HttpResponse httpresponse = httpClient.execute(httpget);
			
			istream = httpresponse.getEntity().getContent();
			//sr=new InputStreamReader(istream);
		}catch(Exception e){
			LOG.error(e);
		}finally{
			//getmethod.releaseConnection();
		}
		return istream;
	}
	public static void main(String [] arg){
		getStocks2();
	}
}
