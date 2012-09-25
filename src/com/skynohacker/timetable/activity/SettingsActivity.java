package com.skynohacker.timetable.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.example.timetable.R;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener{

	private SharedPreferences _preferences;
	private EditTextPreference _userId;
	private EditTextPreference _userPw;
	private Preference _aboutPre;
	private Editor _editor;
	
	private final static String DEFAULT_USERID = "请输入学号";
	private final static String DEFAULT_USERPW = "请输入密码";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		_preferences = PreferenceManager.getDefaultSharedPreferences(this);
		_editor = _preferences.edit();
		_userId = (EditTextPreference) findPreference(getString(R.string.userId));
		_userPw = (EditTextPreference) findPreference(getString(R.string.userPw));
		_aboutPre = (Preference) findPreference(getString(R.string.about));
		_aboutPre.setOnPreferenceClickListener(this);
		_userId.setSummary(_preferences.getString("user_id", DEFAULT_USERID));
		_userPw.setSummary(_preferences.getString("user_pw", DEFAULT_USERPW));
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
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(getString(R.string.userId))) {
			_userId.setSummary(_preferences.getString(getString(R.string.userId), DEFAULT_USERID));
			setResult(RESULT_OK);
		}
		else if (key.equals(getString(R.string.userPw))) {
			_userPw.setSummary(_preferences.getString(getString(R.string.userPw), DEFAULT_USERPW));
			setResult(RESULT_OK);
		}

	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		if (preference.getKey().equals(getString(R.string.about))) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("关于").setMessage(getString(R.string.pre_about_content));
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

}
