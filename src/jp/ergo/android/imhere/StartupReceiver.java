package jp.ergo.android.imhere;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class StartupReceiver extends BroadcastReceiver{
	public static final String ACTION_PUSH_IMHERE = "imhere.action.PUSH_IMHERE";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		System.out.println(intent);
		final Bundle bundle = intent.getExtras();
		for(String key: bundle.keySet()){
			System.out.println(key + ":" + bundle.get(key));
		}
		final String action = intent.getAction();
		if(action == null) return;
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			// 端末起動時にAlarmManagerに登録する
			final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			final Intent serviceIntent = new Intent(context, StartupReceiver.class);
			serviceIntent.setAction(ACTION_PUSH_IMHERE);
			final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, pendingIntent);
		}
		if(action.equals(ACTION_PUSH_IMHERE)){
			// AlarmManagerからのBroadcastを受け取ってServiceを起動する
			final Intent serviceIntent = new Intent(context, ImhereService.class);
			context.startService(serviceIntent);
		}
	}
}
