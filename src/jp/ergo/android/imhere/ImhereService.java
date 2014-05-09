package jp.ergo.android.imhere;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.ergo.android.imhere.utils.Logger;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
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
		if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			final LocationProvider provider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);
			Logger.d("provider:" + provider.getName());
			mLocationManager.removeUpdates(ImhereService.this);
			mLocationManager.requestLocationUpdates(provider.getName(), 0, 0, ImhereService.this);
		}else if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			final LocationProvider provider = mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER);
			Logger.d("provider:" + provider.getName());
			mLocationManager.requestLocationUpdates(provider.getName(), 0, 0, ImhereService.this);
		}else{
			// TODO ネットワークもGPSもなかった場合
			Logger.d("nothing is enabled");
		}

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
		Logger.d("onLocationChanged");
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

}
