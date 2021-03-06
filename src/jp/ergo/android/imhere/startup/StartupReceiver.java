package jp.ergo.android.imhere.startup;

import jp.ergo.android.imhere.ImhereService;
import jp.ergo.android.imhere.R;
import jp.ergo.android.imhere.utils.Logger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StartupReceiver extends BroadcastReceiver{
//	public static final String ACTION_PUSH_IMHERE = "imhere.action.PUSH_IMHERE";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		Logger.d(intent);
		final String action = intent.getAction();
		if(action == null) return;
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			Logger.d("action is ACTION_BOOT_COMPLETED");
			final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			final boolean isLaunchOnBoot = sharedPreferences.getBoolean(context.getResources().getString(R.string.pref_key_launch_on_boot), false);
			if(!isLaunchOnBoot) return;

			final Intent serviceIntent = new Intent(context, ImhereService.class);
			context.startService(serviceIntent);
			// 端末起動時にAlarmManagerに登録する
			final String mPrefKeyInterval = context.getResources().getString(R.string.pref_key_interval);
			final int intervalMinutes = Integer.parseInt(sharedPreferences.getString(mPrefKeyInterval, "1440"));
			final long intervalMillis = (long)intervalMinutes * 60L * 1000;
			ImhereService.registerWithAlarmManager(context, intervalMillis);
		}
	}
}
