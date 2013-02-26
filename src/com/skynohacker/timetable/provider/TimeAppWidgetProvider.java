package com.skynohacker.timetable.provider;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.skynohacker.timetable.R;
import com.skynohacker.timetable.activity.DisplayActivity;
import com.skynohacker.timetable.services.TimetableService;

public class TimeAppWidgetProvider extends AppWidgetProvider {

	SQLiteDatabase _database;
	static int _curIndex = 1;
	static String _curWeek = "第一周";
	final static String ACTION_PREVIOUS_CLASS = "com.skynohacker.timeable.action_previous_class";
	final static String ACTION_NEXT_CLASS = "com.skynohacker.timetable.action_next_class";
	final static String ACTION_UPDATE = "com.skynohacker.timetable.update_appwidget";
	final static String[] weeks = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {		
		// 启动TimetableService
		Intent serviceIntent = new Intent(context, TimetableService.class);
		context.startService(serviceIntent);
		
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK)-1;
		_database = context.openOrCreateDatabase(DisplayActivity.DATABASE_NAME,
				Activity.MODE_PRIVATE, null);
		String content = "";

		String sql = "SELECT classname,time,location,teacher FROM timetable where week=? AND classtime=?";
		Cursor cursor = _database.rawQuery(sql, new String[] { "" + day,
				"" + _curIndex });
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			int classname = cursor.getColumnIndex("classname");
			int location = cursor.getColumnIndex("location");
			int time = cursor.getColumnIndex("time");
			while (!cursor.isAfterLast()) {
				content += cursor.getString(classname) + " ";
				content += cursor.getString(location) + " ";
				content += cursor.getString(time) + "\n";
				cursor.moveToNext();
				Log.v("TimeAppWidgetProvider", "content=" + content);
			}
		}
		cursor.close();
		_database.close();
		Log.v("TimeAppWidgetProvider", "content=" + content);

		for (int i = 0; i < appWidgetIds.length; i++) {

			final int appWidgetId = appWidgetIds[i];
			Date date = new Date(System.currentTimeMillis());
			int today = date.getDay();
			SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
			String time = dateFormat.format(date);
			Intent intent = new Intent(context, DisplayActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0);

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.appwidget_layout);
			views.setOnClickPendingIntent(R.id.appwidget_content, pendingIntent);
			views.setTextViewText(R.id.appwidget_content, content);
			views.setTextViewText(R.id.appwidget_title, "" + _curIndex );
			views.setTextViewText(R.id.appwidget_time, weeks[day] + "\n" + _curWeek);
			Intent intent2 = new Intent(context, TimeAppWidgetProvider.class);
			intent2.setAction(ACTION_PREVIOUS_CLASS);
			PendingIntent pi2 = PendingIntent.getBroadcast(context, 0, intent2,
					0);
			views.setOnClickPendingIntent(R.id.appwidget_up_btn, pi2);
			intent2 = new Intent(context, TimeAppWidgetProvider.class);
			intent2.setAction(ACTION_NEXT_CLASS);
			pi2 = PendingIntent.getBroadcast(context, 0, intent2, 0);
			views.setOnClickPendingIntent(R.id.appwidget_down_btn, pi2);
			Log.v("AppWidget", "time=" + time);
			appWidgetManager.updateAppWidget(appWidgetId, views);

		}

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(this.toString(), "onReceive");
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		ComponentName componentName = new ComponentName(context,
				TimeAppWidgetProvider.class);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
		
		String action_str = intent.getAction();
		if (action_str != null && action_str.equals(ACTION_NEXT_CLASS)) {
			Log.v("TimeAppWidget action", action_str);
			_curIndex = _curIndex+1>5 ? 1 : _curIndex+1;
			//onUpdate(context, appWidgetManager, appWidgetIds);
		}
		else if (action_str != null && action_str.equals(ACTION_PREVIOUS_CLASS)) {
			Log.v("TimeAppWidget action", action_str);
			_curIndex = _curIndex-1<1 ? 5 : _curIndex-1;
			//onUpdate(context, appWidgetManager, appWidgetIds);
		}
		else if (action_str != null && action_str.equals(ACTION_UPDATE)) {
			Log.v("TimeAppWidget action", action_str);
			_curWeek = intent.getStringExtra("nowWeek");
			Log.v("TimeAppWidget", "_curWeek=" + _curWeek);
		}
		onUpdate(context, appWidgetManager, appWidgetIds);
	}

}
