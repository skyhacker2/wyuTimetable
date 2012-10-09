package com.skynohacker.timetable.thread;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.skynohacker.timetable.activity.DisplayActivity;
import com.skynohacker.timetable.activity.SettingsActivity;

public class LoginHandler extends Handler{
	private ProgressDialog _dialog;
	private Context _context;
	private SQLiteDatabase _database;
	
	public static final int LOGIN = 0;		// 登陆
	public static final int DOWNLOAD = 1;		// 下载
	public static final int PARSE = 2;		// 解析
	public static final int FINISH = 3; 		// 完成
	public static final int TIMEOUT = 4;		// 超时
	public static final int ERROR = 5;		// 账号或密码错误
	public static final int NETWORK_UNAVAILABLE = 6;	// 网络不可用
	
	public LoginHandler(Context context, ProgressDialog dialog) {
		_dialog = dialog;
		_context = context;
		_dialog.show();
	}

	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
		case LOGIN:
			_dialog.setMessage("正在登陆...");
			break;
		case DOWNLOAD:
			_dialog.setMessage("下载数据...");
			break;
		case PARSE:
			_dialog.setMessage("解析数据...");
			break;
		case TIMEOUT:
			_dialog.dismiss();
			Toast.makeText(_context, "连接超时", Toast.LENGTH_LONG).show();
			break;
		case ERROR:
			_dialog.dismiss();
			Toast.makeText(_context, "学号或密码错误", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(_context, SettingsActivity.class);
			((DisplayActivity)_context).startActivityForResult(intent, DisplayActivity.SETTINGS_CODE);
			break;
		case NETWORK_UNAVAILABLE:
			_dialog.dismiss();
			Toast.makeText(_context, "网络不可用", Toast.LENGTH_LONG).show();
			break;
		case FINISH:
			SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(_context);
			SharedPreferences.Editor editor = pre.edit();
			editor.putBoolean("hasData", true);
			editor.commit();
			_dialog.dismiss();
			((DisplayActivity)_context).initPreferences();
			((DisplayActivity)_context).initLayout();
			Toast.makeText(_context, "课表已更新", Toast.LENGTH_SHORT).show();
			break;
		}
		super.handleMessage(msg);
	}
	
	
	
}
