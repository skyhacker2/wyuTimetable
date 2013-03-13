package com.skynohacker.timetable.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.skynohacker.timetable.R;

public class LoadingDialog extends ProgressDialog{

	public LoadingDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progress_dialog);
		Window window = getWindow();
		LayoutParams lp = window.getAttributes();
		lp.dimAmount = 0f;
	}

	
}
