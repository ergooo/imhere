package jp.ergo.android.imhere;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StartupReceiver extends BroadcastReceiver{
//	public static final String ACTION_PUSH_IMHERE = "imhere.action.PUSH_IMHERE";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		System.out.println(intent);
		final String action = intent.getAction();
		if(action == null) return;
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			System.out.println("action is ACTION_BOOT_COMPLETED");
			final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			final boolean isLaunchOnBoot = sharedPreferences.getBoolean("launch_on_boot", false);
			if(!isLaunchOnBoot) return;

			final Intent serviceIntent = new Intent(context, ImhereService.class);
			context.startService(serviceIntent);
			// 端末起動時にAlarmManagerに登録する
			final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			final int selectedInterval = MainActivity.getSelectedIntervalFromPreferences(context);
			final Interval interval = Interval.gen(selectedInterval);
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis()+2000, interval.getDuration(), pendingIntent);
		}
	}
}
