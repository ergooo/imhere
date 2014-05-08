package jp.ergo.android.imhere;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ImhereActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		setContentView(R.layout.activity_main);

		// SharedPreferencesを初期化（第3引数をfalseとすることで初回起動時のみ初期化される）
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new ImhereSettingsFragment())
        .commit();

//		// タッチイベント登録
//		// アカウント設定
//		final View accountSettingButton = findViewById(R.id.accountSettingItem);
//		accountSettingButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(ImhereActivity.this, GmailAccountActivity.class);
//				startActivity(intent);
//			}
//		});
//
//		// サービス起動
//		final ToggleButton launchTogle = (ToggleButton)findViewById(R.id.serviceLaunchToggle);
//		launchTogle.setChecked(isServiceRunning());
//		launchTogle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//				final AlarmManager alarmManager = (AlarmManager) ImhereActivity.this.getSystemService(Context.ALARM_SERVICE);
//				final Intent serviceIntent = new Intent(ImhereActivity.this, ImhereService.class);
//				final PendingIntent pendingIntent = PendingIntent.getService(ImhereActivity.this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//				if(isChecked){
//					alarmManager.cancel(pendingIntent);
//					final int selectedInterval = getSelectedIntervalFromPreferences(ImhereActivity.this);
//					final Interval interval = Interval.gen(selectedInterval);
//					alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), interval.getDuration(), pendingIntent);
//				}else{
//					alarmManager.cancel(pendingIntent);
//				}
//			}
//		});
//
//		// ブート時にサービスを起動
//		final View launchOnBootItem = findViewById(R.id.serviceLaunchOnBootItem);
//		final CheckBox launchOnBootCheckBox = (CheckBox)findViewById(R.id.serviceLaunchOnBootCheckbox);
//		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ImhereActivity.this);
//		final boolean isLaunchOnBoot = sharedPreferences.getBoolean("launch_on_boot", false);
//		launchOnBootCheckBox.setChecked(isLaunchOnBoot);
//		launchOnBootCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				final Editor editor = sharedPreferences.edit();
//				editor.putBoolean("launch_on_boot", isChecked);
//				editor.commit();
//			}
//		});
//		launchOnBootItem.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				launchOnBootCheckBox.setChecked(!launchOnBootCheckBox.isChecked());
//			}
//		});
//
//
//		// インターバル選択
//		final View intervalItem = findViewById(R.id.notifyIntervalItem);
//		intervalItem.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//
//				final DialogFragment dialogFragment = new DialogFragment(){
//					@Override
//			        public Dialog onCreateDialog(Bundle savedInstanceState) {
//						return createIntervalSelectDialog(ImhereActivity.this);
//					}
//				};
//				dialogFragment.show(getSupportFragmentManager(), "interval_select");
//
//			}
//		});
//
//		// 選択中のインターバル表示
//		final String[] intervals = Interval.getDisplayNames();
//		final String selectedItem = intervals[getSelectedIntervalFromPreferences(this)];
//		((TextView)findViewById(R.id.notifyIntervalSelectedTextView)).setText(selectedItem);
//
//		// 位置情報サービス設定
//		final View locationServiceSettings = findViewById(R.id.locationSettingItem);
//		locationServiceSettings.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent intent=new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
//				startActivity(intent);
//			}
//		});
	}

	private Dialog createIntervalSelectDialog(final Context context){
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("通知間隔");

		// 表示アイテムを指定する
		final int previousSelected = getSelectedIntervalFromPreferences(this);
		builder.setSingleChoiceItems(Interval.getDisplayNames(), previousSelected, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				((TextView)findViewById(R.id.notifyIntervalSelectedTextView)).setText(Interval.gen(which).getDisplayName());
				setSelectedIntervalToPreferences(ImhereActivity.this, which);
				dialog.dismiss();
			}
		});

		return  builder.create();

	}

	public static int getSelectedIntervalFromPreferences(final Context context){
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getInt("interval", Interval.DAY.ordinal());
	}

	private static void setSelectedIntervalToPreferences(final Context context, final int interval){
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		final Editor editor = sharedPreferences.edit();
		editor.putInt("interval", interval);
		editor.commit();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}
}
