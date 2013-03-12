package com.skynohacker.timetable.activity;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.skynohacker.timetable.R;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	private SharedPreferences _preferences;
	private EditTextPreference _userId;
	private EditTextPreference _userPw;
	private Preference _aboutPre;
	private ListPreference _nowWeeksPre;
	private static final int DONE_MENU_ID = 0;

	String[] nowWeeks;

	public final static String DEFAULT_USERID = "学生子服务系统学号";
	public final static String DEFAULT_USERPW = "学生子服务系统密码";
	public final static String DEFAULT_WEEK = "请选择";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		getSupportActionBar().setHomeButtonEnabled(true);

		nowWeeks = getResources().getStringArray(R.array.now_weeks);
		_preferences = PreferenceManager.getDefaultSharedPreferences(this);
		_preferences.edit();
		_userId = (EditTextPreference) findPreference(getString(R.string.userId));
		_userPw = (EditTextPreference) findPreference(getString(R.string.userPw));
		_aboutPre = (Preference) findPreference(getString(R.string.about));
		_aboutPre.setOnPreferenceClickListener(this);
		_userId.setSummary(_preferences.getString("user_id", DEFAULT_USERID));
		String user_pw = _preferences.getString("user_pw", DEFAULT_USERPW);
		if (!user_pw.equals(DEFAULT_USERPW))
			user_pw = "******";
		_userPw.setSummary(user_pw);

		_nowWeeksPre = (ListPreference) findPreference(getString(R.string.pre_now_week));
		String pos = _preferences.getString(getString(R.string.pre_now_week),
				DEFAULT_WEEK);
		if (!pos.equals(DEFAULT_WEEK)) {
			int nowWeek = Integer.parseInt(pos);
			System.out.println("nowWeek=" + nowWeek);
			_nowWeeksPre.setTitle(nowWeeks[nowWeek]);
		} else {
			_nowWeeksPre.setTitle(DEFAULT_WEEK);
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(getString(R.string.userId))) {
			_userId.setSummary(_preferences.getString(
					getString(R.string.userId), DEFAULT_USERID));
			setResult(RESULT_OK);
		} else if (key.equals(getString(R.string.userPw))) {
			_userPw.setSummary("******");
			setResult(RESULT_OK);
		} else if (key.equals(getString(R.string.pre_now_week))) {
			// 现在是第几周
			int nowWeek = Integer.parseInt(_preferences.getString(
					getString(R.string.pre_now_week), "0"));
			_nowWeeksPre.setTitle(nowWeeks[nowWeek]);
			Calendar calendar = Calendar.getInstance();
			// 现在离1970年是多少天
			long now_day = calendar.getTimeInMillis() / 1000 / 60 / 60 / 24;
			// 今天是星期几 SUNDAY=1
			int day_of_week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			// 第一周开始时间
			long start_day = now_day - nowWeek * 7 - day_of_week;
			SharedPreferences.Editor editor = _preferences.edit();
			editor.putLong("start_day", start_day);
			editor.commit();
		}

	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		if (preference.getKey().equals(getString(R.string.about))) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("关于").setMessage(
					getString(R.string.pre_about_content));
			builder.setPositiveButton("确认", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, DONE_MENU_ID, 0, "完成").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DONE_MENU_ID:
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}
