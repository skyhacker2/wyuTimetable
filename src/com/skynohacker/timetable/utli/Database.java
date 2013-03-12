package com.skynohacker.timetable.utli;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

	public static final String DB_NAME = "Timetable.db";
	public static final int DB_VERSION = 1;
	public static final String NAME_ACCOUTS = "accouts";
	public static final String NAME_DATA = "data";
	public static final String NAME_WEEK = "week";

	/**
	 * 这个类用来保存了学生的个人信息在数据库里的字段
	 * 
	 * @author skyhacker
	 * 
	 */
	public static final class Account {
		public static final String _ID = "_id";
		public static final String NUM = "num"; // 学号
		public static final String NAME = "name"; // 姓名
		public static final String SEX = "sex"; // 性别
		public static final String CLASS = "class"; // 班级
		public static final String DEPARTMENT = "department"; // 院系
		public static final String MAJOR = "major"; // 专业
	}

	String id = Account._ID;

	/**
	 * 这个类保存了data表中的字段
	 * 
	 * @author skyhacker
	 * 
	 */
	public static final class Data {
		public static final String _ID = "_id";
		public static final String TYPE = "type"; // 数据类型
		public static final String DATA1 = "data1"; // 字段1
		public static final String DATA2 = "data2";
		public static final String DATA3 = "data3";
		public static final String DATA4 = "data4";
		public static final String DATA5 = "data5";
		public static final String DATA6 = "data6";
		public static final String DATA7 = "data7";
		public static final String DATA8 = "data8";
		public static final String DATA9 = "data9";
		public static final String DATA10 = "data10";
	}

	/**
	 * 这个是数据类型的类，子类表示各种类型
	 * 
	 * @author skyhacker
	 * 
	 */
	public static final class DataKinds {
		/**
		 * 课程类型，表示课程的信息
		 * 
		 * @author skyhacker
		 * 
		 */
		public static final class ClassInfo {
			public static final String NAME = "data1"; // 课程名称
			public static final String CLASS_ID = "data2"; // 开课号
			public static final String CREDIT = "data3"; // 学分
			public static final int CONTENT_ITEM_TYPE = 1; // 对应Data的TYPE
		}

		/**
		 * 教师类型，表示老师的信息
		 * 
		 * @author skyhacker
		 * 
		 */
		public static final class Teacher {
			public static final int CONTENT_ITEM_TYPE = 2;
			public static final String NAME = "data1"; // 老师名称
		}

		/**
		 * 上课地点和时间
		 * 
		 * @author skyhacker
		 * 
		 */
		public static final class LocationAndTime {
			public static final int CONTENT_ITEM_TYPE = 3;
			public static final String ADDRESS = "data1"; // 教师地址
			public static final String WEEK = "data2"; // 星期几的课
			public static final String NUM = "data3"; // 第几节课
			public static final String TIME = "data4"; // 第几周的课
		}

		/**
		 * 电话号码
		 * 
		 * @author skyhacker
		 * 
		 */
		public static final class Phone {
			public static final int CONTENT_ITEM_TYPE = 4;
			public static final String NUMBER = "data1"; // 电话号码
			public static final String TYPE = "data2"; // 电话类型
			public static final int TYPE_HOME = 0;
			public static final int TYPE_WORK = 1;
			public static final int TYPE_MOBILE = 2;
			public static final int TYPE_OTHER = 3;
		}

		/**
		 * 地址邮件
		 * 
		 * @author skyhacker
		 * 
		 */
		public static final class Email {
			public static final int CONTENT_ITEM_TYPE = 5;
			public static final String ADDRESS = "data1"; // 邮箱地址
		}

		/**
		 * 备注
		 * 
		 * @author skyhacker
		 * 
		 */
		public static final class Remark { // 备注
			public static final int CONTENT_ITEM_TYPE = 6;
			public static final String REMARK = "data1";
		}

		/**
		 * 笔记
		 * 
		 * @author skyhacker
		 * 
		 */
		public static final class Note {
			public static final int CONTENT_ITEM_TYPE = 7;
			public static final String NOTE = "data1";
		}

		/**
		 * 考试时间和地点
		 * 
		 * @author skyhacker
		 * 
		 */
		public static final class ExaminationTimeAndLocation {
			public static final int CONTENT_ITEM_TYPE = 8;
			public static final String TIME = "data1";
			public static final String ADDRESS = "data2";
		}

		/**
		 * 网站
		 * 
		 * @author skyhacker
		 * 
		 */
		public static final class WebSite {
			public static final int CONTENT_ITEM_TYPE = 9;
			public static final String URL = "data1";
		}
	}

	private Context _context;

	public Database(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		_context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String accouts_sql = String.format(
				"CREATE TABLE %s (%s INTEGET PRIMARY, %s TEXT, %s TEXT, %s TEXT,"
						+ "%s TEXT, %s TEXT, %s TEXT)", NAME_ACCOUTS, Account._ID,
				Account.NUM, Account.NAME, Account.SEX, Account.CLASS,
				Account.DEPARTMENT, Account.MAJOR);
		String data_sql = String.format("CREATE TABLE %s (%s INGETER, %s INGETER " +
				"%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT," +
				"%s TEXT, %s TEXT)", NAME_DATA, Data._ID, Data.TYPE, Data.DATA1, Data.DATA2,
				Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6, Data.DATA7, Data.DATA8, 
				Data.DATA9, Data.DATA10);
		db.execSQL(accouts_sql);
		db.execSQL(data_sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
