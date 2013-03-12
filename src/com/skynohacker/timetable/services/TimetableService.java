package com.skynohacker.timetable.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.skynohacker.timetable.R;
import com.skynohacker.timetable.activity.DisplayActivity;
import com.skynohacker.timetable.provider.TimeAppWidgetProvider;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class TimetableService extends Service{
	
	private SQLiteDatabase _database;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, getIntent());
		//am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), getIntent());
	}
	
	private PendingIntent getIntent() {
		Intent intent = new Intent(this, TimeAppWidgetProvider.class);		
		intent.setAction("com.skynohacker.timetable.update_appwidget");
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
		return pi;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}


}
