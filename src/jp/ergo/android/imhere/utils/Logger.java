package jp.ergo.android.imhere.utils;

import jp.ergo.android.imhere.BuildConfig;
import android.util.Log;

public class Logger {
	private static final String M7_TAG = "ImHere";

	private static boolean sIsLoggable = BuildConfig.DEBUG;

	public static void e(final Object message){
		e(M7_TAG, message);
	}
	public static void d(final Object message){
		d(M7_TAG, message);
	}
	public static void i(final Object message){
		i(M7_TAG, message);
	}
	public static void w(final Object message){
		w(M7_TAG, message);
	}
	public static void v(final Object message){
		v(M7_TAG, message);
	}

	public static void e(final String tag, final Object message){
		if(sIsLoggable)Log.e(tag, message.toString());
	}
	public static void d(final String tag, final Object message){
		if(sIsLoggable)Log.d(tag, message.toString());
	}
	public static void i(final String tag, final Object message){
		if(sIsLoggable)Log.i(tag, message.toString());
	}
	public static void w(final String tag, final Object message){
		if(sIsLoggable)Log.w(tag, message.toString());
	}
	public static void v(final String tag, final Object message){
		if(sIsLoggable)Log.v(tag, message.toString());
	}
	public static void setLoggable(final boolean isLoggable){
		sIsLoggable = isLoggable;
	}
}
