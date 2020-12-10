package ru.henridellal.emerald;

import android.os.Handler;
import android.os.HandlerThread;

public class GetAppsThread extends HandlerThread {
	private Handler mHandler;

	public GetAppsThread(String name) {
		super(name);
	}

	public void postTask(Runnable task) {
		mHandler.post(task);
	}

	public void prepareHandler() {
		mHandler = new Handler(getLooper());
	}
}
