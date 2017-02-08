package com.rcgl.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * 确认提醒工具类
 * @author lims
 * 2015-05-02
 */
public class ConfirmUtil {
	Context context;
	
	
	public ConfirmUtil(Context context) {
		super();
		this.context = context;
	}

	public boolean isDelete = false;
	public void onDeletePressed() { 
        new AlertDialog.Builder(context).setTitle("你真的确定要删除吗？") 
            .setIcon(android.R.drawable.ic_dialog_info) 
            .setPositiveButton("确定", new DialogInterface.OnClickListener() { 
         
                @Override 
                public void onClick(DialogInterface dialog, int which) { 
                	
                } 
            }) 
            .setNegativeButton("取消", new DialogInterface.OnClickListener() { 
         
                @Override 
                public void onClick(DialogInterface dialog, int which) { 
                	
                } 
            }).show(); 
     } 
}
