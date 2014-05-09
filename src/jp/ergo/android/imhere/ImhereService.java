package jp.ergo.android.imhere;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.ergo.android.imhere.utils.Logger;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ImhereService extends Service implements LocationListener {

	private final static String TAG = "ImhereService";
	private String mUser = "";
	private String mPassword = "";
	private LocationManager mLocationManager;

	private native String stringA();
	private native String stringB();

    static {
        System.loadLibrary("imherejni");
    }
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		final String prefKeyMail = getResources().getString(R.string.pref_key_mail);
		final String prefKeyPass = getResources().getString(R.string.pref_key_pass);
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		mUser = sp.getString(prefKeyMail, ""); // コールバック関数内で使いたいので変数に入れとく。
		mPassword = sp.getString(prefKeyPass, ""); // コールバック関数内で使いたいので変数に入れとく。
		Logger.d("mail is \"" + mUser + "\"" + "\npassword is \"" + mPassword + "\"");

		if(mUser.equals("") || mPassword.equals("")) return START_STICKY;

		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);	// onDestroy()で後始末をしたいので変数に入れる。

		final Criteria criteria = new Criteria();
		// PowerRequirement は設定しないのがベストプラクティス
		// Accuracy は設定しないのがベストプラクティス
		//criteria.setAccuracy(Criteria.ACCURACY_FINE);	← Accuracy で最もやってはいけないパターン
		// 以下は必要により
		criteria.setBearingRequired(false);	// 方位不要
		criteria.setSpeedRequired(false);	// 速度不要
		criteria.setAltitudeRequired(false);	// 高度不要

		final String provider = mLocationManager.getBestProvider(criteria, true);
		System.out.println("provider=" + provider);
		if (provider == null) {
			// 位置情報が有効になっていない場合は、Google Maps アプリライクな [現在地機能を改善] ダイアログを起動します。
			// TODO 位置情報の取得が出来ない場合の処理
			return START_STICKY;
		}
		// 最後に取得できた位置情報が5分以内のものであれば有効とします。
		final Location lastKnownLocation = mLocationManager.getLastKnownLocation(provider);
		if (lastKnownLocation != null && (new Date().getTime() - lastKnownLocation.getTime()) <= (5 * 60 * 1000L)) {
			onLocationChanged(lastKnownLocation);
			return START_STICKY;
		}
		mLocationManager.requestLocationUpdates(provider, 0, 0, ImhereService.this);

		return START_STICKY;
	}
	@Override
	public void onDestroy() {
		if(mLocationManager != null) mLocationManager.removeUpdates(this);
		super.onDestroy();

		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onLocationChanged(final Location location) {
		Logger.d("onLocationChanged " + location);
		mLocationManager.removeUpdates(ImhereService.this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String title = "テスト送信";
				final String address = String.format(Locale.JAPANESE, "%s%s(%f, %f)",
						getAddress(location.getLatitude(), location.getLongitude())
						,System.getProperty("line.separator")
						,location.getLatitude()
						,location.getLongitude()
						);
				final String message =  new MessageBuilder(address).toString();

				Logger.d("message: " + message);
				new GmailSender(mUser, mPassword).sendEmail(title, message, mUser);

			}
		}).start();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Logger.d("onProviderDisabled() " + provider);
	}
	@Override
	public void onProviderEnabled(String provider) {
		Logger.d("onProviderEnabled() " + provider);
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Logger.d("onStatusChanged() " + provider);

	}

	public static void registerWithAlarmManager(final Context context, final long intervalMillis){
		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final PendingIntent pendingIntent = createPendingIntent(context);
		alarmManager.cancel(pendingIntent);

		Logger.d(intervalMillis);
		final long triggerAtMillis = System.currentTimeMillis() + (intervalMillis - (System.currentTimeMillis() % intervalMillis));
		alarmManager.setRepeating(AlarmManager.RTC, triggerAtMillis, intervalMillis, pendingIntent);
	}

	public static void unregisterWithAlermManager(final Context context){
		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final PendingIntent pendingIntent = createPendingIntent(context);
		alarmManager.cancel(pendingIntent);
	}

	private static PendingIntent createPendingIntent(final Context context){
		final Intent serviceIntent = new Intent(context, ImhereService.class);
		return PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private String getAddress(final double latitude, final double longitude){
		final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
		try {
			// Call the synchronous getFromLocation() method by passing in the lat/long values.
			final List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
			if (addresses != null && addresses.size() > 0) {

				// 一番長いのを使いたい
				final Map<Integer, String> sortedByLength = Maps.newTreeMap(Collections.reverseOrder());
				for(Address address: addresses){
					final String addressString = address.getAddressLine(1);
					if(addressString == null || addressString == null) continue;
					sortedByLength.put(addressString.length(), addressString);
				}

				return Lists.newArrayList(sortedByLength.values()).get(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "" + latitude + ", " + longitude;
	}

	private Dialog createLocationSuggestionDialog(){
		return new AlertDialog.Builder(this)
		.setTitle("現在地機能を改善")
		.setMessage("現在、位置情報は一部有効ではないものがあります。次のように設定すると、もっともすばやく正確に現在地を検出できるようになります:\n\n● 位置情報の設定でGPSとワイヤレスネットワークをオンにする\n\n● Wi-Fiをオンにする")
		.setPositiveButton("設定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				// 端末の位置情報設定画面へ遷移
				try {
					startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
				} catch (final ActivityNotFoundException e) {
					// 位置情報設定画面がない糞端末の場合は、仕方ないので何もしない
				}
			}
		})
		.setNegativeButton("スキップ", new DialogInterface.OnClickListener() {
			@Override public void onClick(final DialogInterface dialog, final int which) {}	// 何も行わない
		})
		.create();
	}

}
