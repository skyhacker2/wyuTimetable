package com.skynohacker.timetable.thread;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.skynohacker.timetable.activity.DisplayActivity;
import com.skynohacker.timetable.utli.ClassInfo;
import com.skynohacker.timetable.utli.WYUApi;

public class LoginThread extends Thread {
	private Handler _handler;
	private String _userid;
	private String _userpw;
	private Context _context;

	public LoginThread(Context context, Handler handler, String userid,
			String userpw) {
		_context = context;
		_handler = handler;
		_userid = userid;
		_userpw = userpw;
	}

	@Override
	public void run() {
		WYUApi api = new WYUApi(_userid, _userpw);
		if (!api.isNetworkAvailable(_context)) {
			_handler.sendEmptyMessage(LoginHandler.NETWORK_UNAVAILABLE);
			return;
		}
		_handler.sendEmptyMessage(LoginHandler.LOGIN);
		try {
			if (!api.login(_context)) {
				_handler.sendEmptyMessage(LoginHandler.ERROR);
				return;
			}
			_handler.sendEmptyMessage(LoginHandler.DOWNLOAD);
			List<ClassInfo> result = api.getTimetable();
			if (result == null)
				Log.w("timetable data", "null");
			for (ClassInfo info : result) {
				System.out.printf("%s %s %s %s %d %d\n", info.classname,
						info.time, info.location, info.teacher, info.week,
						info.classtime);
			}
			writeToDatabase(result);
			_handler.sendEmptyMessage(LoginHandler.FINISH);
		} catch(SocketTimeoutException e) {
			_handler.sendEmptyMessage(LoginHandler.TIMEOUT);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 将课程表写进数据库了
	 * @param list List<ClassInfo>包含课程表的链表
	 */
	private void writeToDatabase(List<ClassInfo> list) {
		SQLiteDatabase database = _context.openOrCreateDatabase(
				DisplayActivity.DATABASE_NAME, DisplayActivity.MODE_PRIVATE, null);
		// 查询课程表是否存在
		Cursor c = database
				.rawQuery(
						"SELECT name From sqlite_master WHERE type='table' AND name=?",
						new String[] { DisplayActivity.TABLE_NAME });
		if (c.getCount() > 0) {
			database.execSQL("DROP TABLE " + DisplayActivity.TABLE_NAME);
		}
		String create_table = String
				.format("CREATE TABLE %s (classname TEXT, time TEXT, location TEXT, teacher TEXT, week INTEGER, classtime INTEGER)",
						DisplayActivity.TABLE_NAME);
		database.execSQL(create_table);
		
		for (ClassInfo info : list) {
			ContentValues cv = new ContentValues();
			cv.put("classname", info.classname);
			cv.put("time", info.time);
			cv.put("location", info.location);
			cv.put("teacher", info.teacher);
			cv.put("week", info.week);
			cv.put("classtime", info.classtime);
			database.insert(DisplayActivity.TABLE_NAME, null, cv);
		}
		database.close();
	}
}
