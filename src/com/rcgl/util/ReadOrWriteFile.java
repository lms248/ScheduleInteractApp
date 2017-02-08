package com.rcgl.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.Context;

/**
 * 写数据到资源文件或读取资源文件数据
 * @author lims
 * @date 2015-04-01
 */
public class ReadOrWriteFile extends Activity {
	/** 写数据到资源文件 */  
	public void writeFile(String fileName,String data) throws IOException{   
	  try{   
	  
	        FileOutputStream fout = openFileOutput(fileName, Context.MODE_PRIVATE);   
	  
	        byte [] bytes = data.getBytes();   
	  
	        fout.write(bytes);   
	  
	        fout.close();   
	      }   
	  
	        catch(Exception e){   
	        e.printStackTrace();   
	       }   
	}
	
	/** 读取资源文件数据 */ 
	public String readFile(String fileName) throws IOException{   
	  String res="";   
	  try{   
	         FileInputStream fin = openFileInput(fileName);   
	         int length = fin.available();   
	         byte [] buffer = new byte[length];   
	         fin.read(buffer);       
	         res = EncodingUtils.getString(buffer, "UTF-8");   
	         fin.close();       
	     }   
	     catch(Exception e){   
	         e.printStackTrace();   
	     }   
	     return res;   
	  
	}
	
}
