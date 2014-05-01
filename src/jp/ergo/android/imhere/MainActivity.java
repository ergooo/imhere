package jp.ergo.android.imhere;

import java.util.List;

import jp.ergo.android.imhere.gmailaccount.GmailAccountActivity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		final View button = findViewById(R.id.accountSettingItem);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, GmailAccountActivity.class);
				startActivity(intent);
			}
		});

		final ToggleButton launchTogle = (ToggleButton)findViewById(R.id.serviceLaunchToggle);
		launchTogle.setChecked(isServiceRunning());
		launchTogle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				final AlarmManager alarmManager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
				final Intent serviceIntent = new Intent(MainActivity.this, ImhereService.class);
				final PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				if(isChecked){
					alarmManager.cancel(pendingIntent);
					alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 5000, pendingIntent);
				}else{
					alarmManager.cancel(pendingIntent);
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onDestroy(){
		stopService(new Intent(getBaseContext(),ImhereService.class));
		super.onDestroy();
	}

	private boolean isServiceRunning() {
	    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

	    for (final RunningServiceInfo info : services) {
	        if (ImhereService.class.getCanonicalName().equals(info.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
