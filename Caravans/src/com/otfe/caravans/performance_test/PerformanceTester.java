package com.otfe.caravans.performance_test;

import android.util.Log;
/**
 * Class that monitors runtime of encryption
 * @author Ivan Dominic Baguio
 */
public class PerformanceTester {
	private static final String TAG = "PerformanceTester";
	private long start_time;
	private long end_time;
	private String name;
	private long total_time;
	
	public PerformanceTester(String name){
		this.name = name;
		Log.w(TAG, "Starting Test: "+name);
		start_time = System.currentTimeMillis();
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
	}
	
	public void endTest(){
		end_time = System.currentTimeMillis();
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		Log.w(TAG,"Test ended: "+name);
	}
	
	public long getResult(){
		total_time = end_time - start_time;
		String res = "Total time: "+total_time;
		Log.w(TAG, res);
		return total_time;
	}
	
	public long getTotalTime(){
		return total_time;
	}
}
