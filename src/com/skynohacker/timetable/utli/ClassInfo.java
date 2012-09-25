package com.skynohacker.timetable.utli;

public class ClassInfo {

	public final String classname;			// 课程名
	public final String time;				// 上课时间
	public final String location;			// 地点
	public final String teacher;			// 老师
	public final int week;					// 哪一天的课
	public final int classtime;			// 第几节的课
	
	public ClassInfo(String classname, String time, String location, String teacher,
			int week, int classtime) {
		this.classname = classname;
		this.time = time;
		this.location = location;
		this.teacher = teacher;
		this.week = week;
		this.classtime = classtime;
	}
	public ClassInfo(int week, int classtime) {
		this.classname = "空闲时间";
		this.time = "";
		this.location = "";
		this.teacher = "";
		this.week = week;
		this.classtime = classtime;
	}
}
