package com.skynohacker.timetable.provider;

import java.util.Calendar;

import com.skynohacker.timetable.activity.DisplayActivity;
import com.skynohacker.timetable.services.TimetableService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TimetableReceiver extends BroadcastReceiver{
	private SQLiteDatabase _database;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("TimetableReceiver", "onReceive");
		Intent intent1 = new Intent(context, TimeAppWidgetProvider.class);
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		_database = context.openOrCreateDatabase(DisplayActivity.DATABASE_NAME,
				Activity.MODE_PRIVATE, null);
		String content = "";
		for (int i = 1; i <= 5; i++) {
			String sql = "SELECT classname,time,location,teacher FROM timetable where week=? AND classtime=?";
			Cursor cursor = _database.rawQuery(sql, new String[] { "" + 2, "" + i });
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				int classname = cursor.getColumnIndex("classname");
				int location = cursor.getColumnIndex("location");
				int time = cursor.getColumnIndex("time");
				while(!cursor.isAfterLast()) {
					content += cursor.getString(classname) + "\n";
					cursor.moveToNext();
				}
			}
			if (content.equals("")) {
				content = "哈哈，没有课哦~";
			}
			Log.v("TimetableService", "content=" + content);
			
		}
		intent1.putExtra("classname", content);
		Log.v("TimeAppWidgetPrivider", intent1.getStringExtra("classname"));
		context.sendBroadcast(intent1);
	}

}
