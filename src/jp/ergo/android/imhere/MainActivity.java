package jp.ergo.android.imhere;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);

		final EditText userEditText= new EditText(this);
		userEditText.setText("");
		final EditText passwordEditText = new EditText(this);
		passwordEditText.setText("");
		layout.addView(userEditText);
		layout.addView(passwordEditText);

		final Button submit = new Button(this);
		submit.setText("AlarmManager登録");
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String user = userEditText.getText().toString();
				final String password = passwordEditText.getText().toString();
				// SharedPreferencesに保存
				final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		        final Editor editor = sharedPreferences.edit();
		        editor.putString("u", user);
		        editor.putString("p", password);
		        editor.commit();

				final AlarmManager alarmManager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
				final Intent serviceIntent = new Intent(MainActivity.this, StartupReceiver.class);
				serviceIntent.setAction(StartupReceiver.ACTION_PUSH_IMHERE);

				final PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
			}
		});
		layout.addView(submit);
		final Button stopAlarmButton = new Button(this);
		stopAlarmButton.setText("Alarm停止");
		stopAlarmButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final AlarmManager alarmManager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
				final Intent serviceIntent = new Intent(MainActivity.this, StartupReceiver.class);
				serviceIntent.setAction(StartupReceiver.ACTION_PUSH_IMHERE);

				final PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				alarmManager.cancel(pendingIntent);
			}
		});
		layout.addView(stopAlarmButton);
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
}
