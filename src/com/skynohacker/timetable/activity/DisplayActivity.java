package com.skynohacker.timetable.activity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.skynohacker.timetable.R;
import com.skynohacker.timetable.services.TimetableService;
import com.skynohacker.timetable.thread.LoginHandler;
import com.skynohacker.timetable.thread.LoginThread;

public class DisplayActivity extends SherlockActivity implements
		ActionBar.OnNavigationListener {

	private ListView _lv1;
	private ListView _lv2;
	private ListView _lv3;
	private ListView _lv4;
	private ListView _lv5;
	private String[] _weeks;
	private int _curWeek;
	private String[] _dates;
	private int _nowDate;

	private ViewPager _viewPager;
	private ArrayList<View> _viewPagers;
	private LayoutInflater _inflater;

	private SharedPreferences _preferences;

	private SQLiteDatabase _database;
	public static final String DATABASE_NAME = "Timetable.db";
	public static final String TABLE_NAME = "timetable";

	public static final int SETTINGS_CODE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startService(new Intent(this, TimetableService.class));
		Calendar cal = Calendar.getInstance();
		_curWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // SUNDAY=1, MONDAY=2,...
		_weeks = getResources().getStringArray(R.array.week);
		_dates = getResources().getStringArray(R.array.now_weeks);
		_weeks[_curWeek] += "*";
		createDatabase();
		initPreferences();
		initLayout();
	}

	public void initLayout() {
		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = new ArrayAdapter<CharSequence>(
				context, R.layout.sherlock_spinner_item, _weeks);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
		getSupportActionBar().setTitle("课表");
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setSelectedNavigationItem(_curWeek);

		_inflater = getLayoutInflater();
		_viewPager = (ViewPager) findViewById(R.id.viewPager);

		_viewPagers = new ArrayList<View>();
		for (int i = 0; i < 7; i++)
			_viewPagers.add(getView(i));
		_viewPagers.add(getAllClassView());
		_viewPager.setDrawingCacheEnabled(true);
		_viewPager.setAdapter(new MyPagerAdapter());
		_viewPager.setOnPageChangeListener(new MyPagerChangeListener());
		_viewPager.setCurrentItem(_curWeek);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == SETTINGS_CODE && resultCode == RESULT_OK) {
			refresh();
		} else {
			if (_preferences.getBoolean("hasData", false) == true)	
				initPreferences();
				initLayout();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 初始化用户偏好
	 */
	public void initPreferences() {
		_preferences = PreferenceManager.getDefaultSharedPreferences(this);
		// 判断是否有数据
		boolean hasData = _preferences.getBoolean("hasData", false);
		if (!hasData) {
			long now_day = System.currentTimeMillis() / 1000 / 60 / 60 / 24;
			Editor editor = _preferences.edit();
			editor.putLong("start_day", now_day);
			editor.commit();
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, SETTINGS_CODE);
		} else {
			Calendar c = Calendar.getInstance();
			long now_day = c.getTimeInMillis() / 1000 / 60 / 60 / 24;
			long start_day = _preferences.getLong("start_day", 0);
			_nowDate = (int) ((now_day - start_day) / 7);
			_nowDate = _nowDate % 20;
			System.out.printf("现在是第%d周\n", _nowDate);
			Editor editor = _preferences.edit();
			editor.putString(getString(R.string.pre_now_week),
					String.valueOf(_nowDate));
			editor.commit();
		}
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
		public void onPageSelected(int index) {
			// _weekTextView.setText(_weeks[arg0]);
			getSupportActionBar().setSelectedNavigationItem(index);

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
		Cursor cursor = _database.rawQuery(sql, new String[] { "" + week,
				"" + classtime });

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
			map.put("className", "");
			list.add(map);
		}
		_database.close();
		return list;
	}

	/**
	 * 
	 * @param classtime
	 *            第几节课
	 * @return
	 */
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		_database = openOrCreateDatabase(DisplayActivity.DATABASE_NAME,
				Activity.MODE_PRIVATE, null);
		String sql = "SELECT classname,time,location,teacher FROM timetable where week=? AND classtime=?";
		for (int i = 1; i <= 5; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			int index = 0;
			for (int week = 0; week < 7; week++) {
				Cursor cursor = _database.rawQuery(sql, new String[] {
						"" + week, "" + i });
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					int classname = cursor.getColumnIndex("classname");
					map.put("time", "" + i);
					String content = "";
					while (!cursor.isAfterLast()) {
						content = content + '\n' + cursor.getString(classname);
						cursor.moveToNext();
					}
					map.put("" + index, content);
				}
				index++;
			}
			list.add(map);
		}
		_database.close();
		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(_dates[_nowDate]).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		// getMenuInflater().inflate(R.menu.activity_display, menu);
		SubMenu subMenu = menu.addSubMenu("更多");
		MenuInflater menuInflater = new MenuInflater(this);
		menuInflater.inflate(R.menu.activity_display, subMenu);
		MenuItem menuItem = subMenu.getItem();
		menuItem.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_light)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("image/*");
		ShareActionProvider actionProvider = new ShareActionProvider(this);
		actionProvider.setShareIntent(shareIntent);
		Uri uri = Uri.fromFile(getFileStreamPath("share.png"));
		shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

		MenuItem shareMenu = menu
				.findItem(R.id.menu_item_share_action_provider_action_bar);
		shareMenu.setActionProvider(actionProvider);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refresh();
			return true;
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, SETTINGS_CODE);
			return true;
		case android.R.id.home:
			Toast.makeText(this, "益达课表2.2", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.menu_item_share_action_provider_action_bar:
			View view = getWindow().getDecorView();
			view.setDrawingCacheEnabled(true);
			Bitmap bm = view.getDrawingCache();
			Rect frame = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			int statusHeight = frame.top; // 获得状态栏高度
			bm = bm.createBitmap(bm, 0, statusHeight, bm.getWidth(),
					bm.getHeight() - statusHeight);
			FileOutputStream outputStream = null;
			try {
				outputStream = openFileOutput("share.png",
						Context.MODE_WORLD_READABLE);
				if (bm == null)
					System.out.println("bm==null");
				bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
				outputStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//Toast.makeText(this, "onPrepareOptionMenu", Toast.LENGTH_SHORT).show();
		
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * 刷新课表
	 */
	public void refresh() {
		String userid = _preferences.getString(getString(R.string.userId), "");
		String userpw = _preferences.getString(getString(R.string.userPw), "");
		ProgressDialog dialog = new ProgressDialog(this);
		
		LoginHandler handler = new LoginHandler(this, dialog);
		LoginThread thread = new LoginThread(this, handler, userid, userpw);
		thread.start();
		SherlockDialogFragment sherlockDialog = new SherlockDialogFragment();
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

		View page = _inflater.inflate(R.layout.activity_display2, null);

		_lv1 = (ListView) page.findViewById(R.id.lv1);
		_lv2 = (ListView) page.findViewById(R.id.lv2);
		_lv3 = (ListView) page.findViewById(R.id.lv3);
		_lv4 = (ListView) page.findViewById(R.id.lv4);
		_lv5 = (ListView) page.findViewById(R.id.lv5);
		_lv1.setOnItemClickListener(new OnItemClickListener(1, i));
		_lv2.setOnItemClickListener(new OnItemClickListener(2, i));
		_lv3.setOnItemClickListener(new OnItemClickListener(3, i));
		_lv4.setOnItemClickListener(new OnItemClickListener(4, i));
		_lv5.setOnItemClickListener(new OnItemClickListener(5, i));

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
				R.layout.item1,
				new String[] { "className", "teacherName", "time", "location" },
				new int[] { R.id.className1, R.id.teacherName1, R.id.time1,
						R.id.location1 }));
		setListViewHeightBasedOnChildren(_lv2);
		_lv3.setAdapter(new SimpleAdapter(
				this,
				getData(i, 3),
				R.layout.item1,
				new String[] { "className", "teacherName", "time", "location" },
				new int[] { R.id.className1, R.id.teacherName1, R.id.time1,
						R.id.location1 }));
		setListViewHeightBasedOnChildren(_lv3);

		_lv4.setAdapter(new SimpleAdapter(
				this,
				getData(i, 4),
				R.layout.item1,
				new String[] { "className", "teacherName", "time", "location" },
				new int[] { R.id.className1, R.id.teacherName1, R.id.time1,
						R.id.location1 }));
		setListViewHeightBasedOnChildren(_lv4);

		_lv5.setAdapter(new SimpleAdapter(
				this,
				getData(i, 5),
				R.layout.item1,
				new String[] { "className", "teacherName", "time", "location" },
				new int[] { R.id.className1, R.id.teacherName1, R.id.time1,
						R.id.location1 }));
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
		c.close();
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

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		_viewPager.setCurrentItem(itemPosition);
		return false;
	}

	public View getAllClassView() {
		String[] from = new String[] { "time", "0", "1", "2", "3", "4", "5",
				"6" };
		int[] to = new int[] { R.id.time_label, R.id.classname0_label,
				R.id.classname1_label, R.id.classname2_label,
				R.id.classname3_label, R.id.classname4_label,
				R.id.classname5_label, R.id.classname6_label };

		List<Map<String, Object>> list = getData();
		/*
		 * if (list == null) System.out.println("list is null"); for
		 * (Map<String, Object> map : list) { for (int i = 0; i < from.length;
		 * i++) System.out.println(map.get(from[i])); }
		 */

		View page = _inflater.inflate(R.layout.all_class, null);
		ListView listView = (ListView) page.findViewById(R.id.all_list_view);
		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.all_list_content, from, to);
		listView.setAdapter(adapter);

		return page;
	}

	/**
	 * listview OnItemSelectedListener
	 * 
	 * @author skyhacker
	 * 
	 */
	class OnItemClickListener implements
			android.widget.AdapterView.OnItemClickListener {
		private int _time;
		private int _week;

		public OnItemClickListener(int time, int week) {
			_time = time;
			_week = week;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(DisplayActivity.this,
					DetailActivity.class);
			intent.putExtra("week", _week);
			intent.putExtra("time", _time);
			DisplayActivity.this.startActivity(intent);
			Log.v("OnItemClickListener", "_time=" + _time + " _week=" + _week);

		}

	}

}
