package com.ming.trade.track.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ming.trade.track.model.GroupTrade;
import com.ming.trade.track.util.FileUtil;

public class AnalysisHis {
	private static final Log LOG = LogFactory.getLog(AnalysisHis.class);
	
	public static String save(List<GroupTrade> gt){
		String filename = FileUtil.saveFileName();
		save(gt,filename);
		return filename;
	}
	public static void save(List<GroupTrade> gt,String filename){
		save(gt,new File(filename));
	}
	public static void save(List<GroupTrade> gt,File file){
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(  
			        new FileOutputStream(file));
			oos.writeObject(gt);
			oos.close();  
		} catch (Exception e) {
			LOG.error(e);
		}finally{
			
		}  
	}
	public static List<GroupTrade> open(String filename){
		return open(new File(filename));
	}
	public static List<GroupTrade> openReanaly(File file){
		List<GroupTrade> gts=open(file);
		for(int i=0;i<gts.size();i++){
			Analysis.analysisByPrice(gts.get(i));
		}
		return gts;
	}
	public static List<GroupTrade> open(File file){
		ObjectInputStream ois = null;
		List<GroupTrade> gt=null;
		try {  
            ois = new ObjectInputStream(new FileInputStream(file));  
            gt = (List<GroupTrade>) ois.readObject();   
            ois.close();  
        } catch (Exception e) {  
            LOG.error(e);  
        }finally{
			
		}   
		return gt;
	}
}
