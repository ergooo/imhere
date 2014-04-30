package jp.ergo.android.imhere;

import jp.ergo.android.imhere.gmailaccount.GmailAccountActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
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
		launchTogle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				final AlarmManager alarmManager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
				final Intent serviceIntent = new Intent(MainActivity.this, StartupReceiver.class);
				serviceIntent.setAction(StartupReceiver.ACTION_PUSH_IMHERE);

				final PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

//				new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//						new GmailSender("imhereservice.info@gmail.com", "imhereservice").sendEmail("hoge", "hogehoge", "imhereservice.info@gmail.com");
//					}
//				}).start();
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

	private LinearLayout createLayout(){
		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

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
		return layout;
	}
}
