package com.skynohacker.timetable.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.skynohacker.timetable.R;
import com.skynohacker.timetable.thread.LoginHandler;
import com.skynohacker.timetable.thread.LoginThread;

public class DisplayActivity extends Activity {

	private ListView _lv1;
	private ListView _lv2;
	private ListView _lv3;
	private ListView _lv4;
	private ListView _lv5;
	private String[] _weeks;
	private int _curWeek;
	private String[] _dates;
	private int _nowDate;
	
	private TextView _weekTextView;
	private TextView _nowWeekTextView;
	
	private boolean flag = false;

	private ViewPager _viewPager;
	private ArrayList<View> _viewPagers;
	private ViewGroup _main;
	private LayoutInflater _inflater;

	private SharedPreferences _preferences;

	private static final int PROGRESS_DIALOG_ID = 0;
	private static final int WRONG_DIALOG_ID = 1;
	private static final int TIMEOUT_DIALOG_ID = 2;

	private SQLiteDatabase _database;
	public static final String DATABASE_NAME = "Timetable.db";
	public static final String TABLE_NAME = "timetable";
	
	public static final int SETTINGS_CODE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_display);
		setContentView(R.layout.main);
		Calendar cal = Calendar.getInstance();
		_curWeek = cal.get(Calendar.DAY_OF_WEEK)-1;	// SUNDAY=1, MONDAY=2,...
		_weeks = getResources().getStringArray(R.array.week);
		_dates = getResources().getStringArray(R.array.now_weeks);
		_weeks[_curWeek] += "(今天)";
		createDatabase();
		initPreferences();
		initLayout();
	}
	
	public void initLayout() {
		_inflater = getLayoutInflater();
		_viewPagers = new ArrayList<View>();

		for (int i = 0; i < 7; i++)
			_viewPagers.add(getView(i));

		//_main = (ViewGroup) _inflater.inflate(R.layout.main, null);
		_viewPager = (ViewPager) findViewById(R.id.viewPager);
		_weekTextView = (TextView) findViewById(R.id.week);
		_weekTextView.setText(_weeks[_curWeek]);
		_nowWeekTextView = (TextView) findViewById(R.id.now_week);
		_nowWeekTextView.setText(_dates[_nowDate]);
		//setContentView(_main);
		_viewPager.setAdapter(new MyPagerAdapter());
		_viewPager.setOnPageChangeListener(new MyPagerChangeListener());
		_viewPager.setCurrentItem(_curWeek);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == SETTINGS_CODE && resultCode == RESULT_OK) {
			refresh();
		}
		else {
			initPreferences();
			initLayout();
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	/**
	 * 初始化用户偏好
	 */
	public void initPreferences() {
		_preferences = PreferenceManager.getDefaultSharedPreferences(this);
		// 判断是否有数据
		boolean hasData = _preferences.getBoolean("hasData", false);
		if (!hasData) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, SETTINGS_CODE);
		}
		
		// 每到星期日，周数加1
		boolean hasChangedDate = _preferences.getBoolean("hasChangedDate", true);
		int i = Integer.parseInt(_preferences.getString(getString(R.string.pre_now_week), "1"));
		System.out.println(_curWeek + " " + hasChangedDate);
		
		_nowDate = i;
		if (!hasChangedDate && _curWeek == 0) {
			System.out.println("周数加1");
			Editor editor = _preferences.edit();
			editor.putString(getString(R.string.pre_now_week), String.valueOf((i+1)%20));
			editor.putBoolean("hasChangedDate", true);
			editor.commit();
			_nowDate = (i+1)%20;
		}
		else if (hasChangedDate && _curWeek != 0) {
			System.out.println("周数没变");
			Editor editor = _preferences.edit();
			editor.putBoolean("hasChangedDate", false);
			editor.commit();
		}
		System.out.println(_nowDate);
	}

	class MyPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return _viewPagers.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup arg0, int position) {
			// TODO Auto-generated method stub
			((ViewPager) arg0).addView(_viewPagers.get(position));
			return _viewPagers.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			((ViewPager) container).removeView(_viewPagers.get(position));
		}

	}

	class MyPagerChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int arg0) {
			_weekTextView.setText(_weeks[arg0]);
		}

	}

	/**
	 * 获取ListView Adapter的数据
	 * 
	 * @param week
	 *            星期几
	 * @param classtime
	 *            第几节课
	 * @return
	 */
	private List<Map<String, Object>> getData(int week, int classtime) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		_database = openOrCreateDatabase(DisplayActivity.DATABASE_NAME,
				Activity.MODE_PRIVATE, null);
		String sql = "SELECT classname,time,location,teacher FROM timetable where week=? AND classtime=?";
		Cursor cursor = _database.rawQuery(sql, new String[]{""+week, ""+classtime});

		// cursor.moveToFirst();
		Log.w("query", "" + cursor.getCount());
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			int classname = cursor.getColumnIndex("classname");
			int time = cursor.getColumnIndex("time");
			int location = cursor.getColumnIndex("location");
			int teacher = cursor.getColumnIndex("teacher");
			while (!cursor.isAfterLast()) {
				map = new HashMap<String, Object>();
				map.put("className", cursor.getString(classname));
				map.put("teacherName", cursor.getString(teacher));
				map.put("time", cursor.getString(time));
				map.put("location", cursor.getString(location));
				list.add(map);
				cursor.moveToNext();
			}
		} else {
			map.put("className", "空闲时间");
			list.add(map);
		}
		_database.close();
		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_display, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refresh();
			return true;
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, SETTINGS_CODE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/**
	 * 刷新课表
	 */
	public void refresh() {
		String userid = _preferences.getString(getString(R.string.userId), "");
		String userpw = _preferences.getString(getString(R.string.userPw), "");
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle("请稍后");
		LoginHandler handler = new LoginHandler(this, dialog);
		LoginThread thread = new LoginThread(this, handler, userid, userpw);
		thread.start();
	}

	/***
	 * 动态设置listview的高度
	 * 
	 * @param listView
	 */
	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		// params.height += 5;// if without this statement,the listview will be
		// a
		// little short
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
	}

	public View getView(int i) {

		View page = _inflater.inflate(R.layout.activity_display, null);

		_lv1 = (ListView) page.findViewById(R.id.lv1);
		_lv2 = (ListView) page.findViewById(R.id.lv2);
		_lv3 = (ListView) page.findViewById(R.id.lv3);
		_lv4 = (ListView) page.findViewById(R.id.lv4);
		_lv5 = (ListView) page.findViewById(R.id.lv5);
		_lv1.setAdapter(new SimpleAdapter(
				this,
				getData(i, 1),
				R.layout.item1,
				new String[] { "className", "teacherName", "time", "location" },
				new int[] { R.id.className1, R.id.teacherName1, R.id.time1,
						R.id.location1 }));
		setListViewHeightBasedOnChildren(_lv1);

		_lv2.setAdapter(new SimpleAdapter(
				this,
				getData(i, 2),
				R.layout.item2,
				new String[] { "className", "teacherName", "time", "location" },
				new int[] { R.id.className2, R.id.teacherName2, R.id.time2,
						R.id.location2 }));
		setListViewHeightBasedOnChildren(_lv2);
		_lv3.setAdapter(new SimpleAdapter(
				this,
				getData(i, 3),
				R.layout.item3,
				new String[] { "className", "teacherName", "time", "location" },
				new int[] { R.id.className3, R.id.teacherName3, R.id.time3,
						R.id.location3 }));
		setListViewHeightBasedOnChildren(_lv3);

		_lv4.setAdapter(new SimpleAdapter(
				this,
				getData(i, 4),
				R.layout.item4,
				new String[] { "className", "teacherName", "time", "location" },
				new int[] { R.id.className4, R.id.teacherName4, R.id.time4,
						R.id.location4 }));
		setListViewHeightBasedOnChildren(_lv4);

		_lv5.setAdapter(new SimpleAdapter(
				this,
				getData(i, 5),
				R.layout.item5,
				new String[] { "className", "teacherName", "time", "location" },
				new int[] { R.id.className5, R.id.teacherName5, R.id.time5,
						R.id.location5 }));
		setListViewHeightBasedOnChildren(_lv5);

		return page;
	}


	private void createDatabase() {
		_database = openOrCreateDatabase(DisplayActivity.DATABASE_NAME,
				Context.MODE_PRIVATE, null);
		// 查询课程表是否存在
		Cursor c = _database.rawQuery(
				"SELECT name From sqlite_master WHERE type='table' AND name=?",
				new String[] { DisplayActivity.TABLE_NAME });
		if (c.getCount() > 0) {
			return;
		}
		String create_table = String
				.format("CREATE TABLE %s (classname TEXT, time TEXT, location TEXT, teacher TEXT, week INTEGER, classtime INTEGER)",
						DisplayActivity.TABLE_NAME);
		_database.execSQL(create_table);
		_database.close();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	

}
