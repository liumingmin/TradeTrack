package com.ming.trade.track.util;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtil {
	
	private static final Log LOG = LogFactory.getLog(FileUtil.class);
	
	private static  String defaultTmpDir=null;
	
	private static  String defaultSaveDir=null;
	
	private static final DateFormat datedf=new SimpleDateFormat("yyyy-MM-dd");
	
	static{
		init();
		clearFiles();
	}
	public static void init(){
		String cpath = null;
		try {
			cpath = FileUtil.class.getClassLoader().getResource("").getPath();
		} catch (RuntimeException e) {
			LOG.error(e);
		}
		if(cpath!=null){
			defaultTmpDir=cpath+"tmp";
			defaultSaveDir=cpath+"save";
		}else{
			defaultTmpDir="./tmp";
			defaultSaveDir="./save";
		}
	}
	public static String tmpDir(){
		String tmpDir = defaultTmpDir;
		File defaulttmpdir = new File(tmpDir);
		if(!defaulttmpdir.exists()){
			defaulttmpdir.mkdirs();
			defaulttmpdir.mkdir();
			LOG.info("创建文件临时目录");
		}
		return tmpDir;
	}
	public static String saveDir(){
		String saveDir = defaultSaveDir;
		File defaultsavedir = new File(saveDir);
		if(!defaultsavedir.exists()){
			defaultsavedir.mkdirs();
			defaultsavedir.mkdir();
			LOG.info("创建文件保存目录");
		}
		return saveDir;
	}
	public static String saveFileName(){
		String saveDir = saveDir();
		int i=0;
		String prefix="save";
		String filename=saveDir+"/"+prefix+i;
		while(new File(filename).exists()){
			i++;
			filename=saveDir+File.separator+prefix+i;
		}
		return filename;
	}
	public static void clearFiles(){
		String tmpDir = tmpDir();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -7);
		Date beforedate=c.getTime();
		File[] files = new File(tmpDir).listFiles();
		for(int i=0;i<files.length;i++){
			String str=files[i].getName();
			if(str.length()<=14) continue;
			String datestr=str.substring(str.length()-14,str.length()-4);
			Date date;
			try {
				date = datedf.parse(datestr);
			} catch (ParseException e) {
				continue;
			}
			if(date.before(beforedate)){ 
				files[i].delete();
			}
		}
	}
	public static void main(String [] args){
		String s="111111111112011-05-26.xls";
		System.out.println(s.substring(s.length()-14,s.length()-4));
	}
}
