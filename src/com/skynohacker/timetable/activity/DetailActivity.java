package com.skynohacker.timetable.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.skynohacker.timetable.R;

public class DetailActivity extends SherlockActivity {
	private ListView _listView;
	private int _week;
	private int _time;
	private SQLiteDatabase _database;
	private static String[] _detailName = new String[] { "课程:", "老师:", "时间:", "地址:" };
	private static String[] _indexName = new String[]{"classname", "teacher", "time", "location"};
	
	private static final int EDIT_MENU = 0;
	private static final int NEW_MENU = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		_listView = (ListView) findViewById(R.id.class_detail_listview);
		Intent intent = getIntent();
		_week = intent.getIntExtra("week", 0);
		_time = intent.getIntExtra("time", 0);

		_listView.setAdapter(new SimpleAdapter(this, getData(),
				R.layout.class_detail_list_item,
				new String[] { "name", "value" }, new int[] { R.id.detail_name,
						R.id.detail_value }));
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;
		_database = openOrCreateDatabase(DisplayActivity.DATABASE_NAME,
				Activity.MODE_PRIVATE, null);
		String sql = "SELECT classname,time,location,teacher FROM timetable where week=? AND classtime=?";
		Cursor cursor = _database.rawQuery(sql, new String[] { "" + _week,
				"" + _time });

		// cursor.moveToFirst();
		Log.w("query", "" + cursor.getCount());
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			int[] keys = new int[4];
			for (int i = 0; i < _indexName.length; i++)
				keys[i] = cursor.getColumnIndex(_indexName[i]);

			while (!cursor.isAfterLast()) {
				for (int i = 0; i < keys.length; i++) {
					map = new HashMap<String, Object>();
					map.put("value", cursor.getString(keys[i]));
					map.put("name", _detailName[i]);
					list.add(map);
				}
				cursor.moveToNext();
			}
		}
		_database.close();
		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, EDIT_MENU, 0, "编辑").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, NEW_MENU, 1, "添加").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Toast.makeText(this, "抱歉，该功能还没有完成~~", Toast.LENGTH_LONG).show();
		return super.onOptionsItemSelected(item);
	}

}
