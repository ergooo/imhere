package jp.ergo.android.imhere;

import java.util.ArrayList;
import java.util.List;

public enum Interval {
	FIFTEEN_MINUTES("15分", toMinute(15))
	,HALF_HOUR("30分", toMinute(30))
	,HOUR("1時間", toHour(1))
	,SIX_HOUR("6時間", toHour(6))
	,HALF_DAY("12時間", toHour(12))
	,DAY("24時間", toHour(24));
	;
	private final String mName;
	private final long mDuration;
	private Interval(final String name, final long duration){
		mName = name;
		mDuration = duration;
	}

	public String getDisplayName(){
		return mName;
	}
	public long getDuration(){
		return mDuration;
	}
	public static String[] getDisplayNames(){
		final List<String> displayNames = new ArrayList<String>();
		for(final Interval interval: Interval.values()){
			displayNames.add(interval.getDisplayName());
		}
		return displayNames.toArray(new String[displayNames.size()]);
	}
	public static Interval gen(final int ordinal){
		for(final Interval interval: Interval.values()){
			if(interval.ordinal() == ordinal) return interval;
		}
		return DAY;
	}


	private static long toMinute(final int minute){
		return minute * 60 * 1000;
	}
	private static long toHour(final int hour){
		return toMinute(60 * hour);
	}

}
