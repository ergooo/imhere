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
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ImhereService extends Service implements LocationListener {
	private ImhereServiceListener mListener;
	private final ImhereBindService.Stub mMyBindService = new ImhereBindService.Stub() {

		//Observer登録method内でRemoteCallbackList#register() methodで引数に渡されたCallback interfaceを登録する
		public void setImhereServiceListener(ImhereServiceListener observer)
				throws RemoteException {
			Logger.d("setObserver called by " + Thread.currentThread().getName());
			mListener = observer;
		}

	};


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
		return mMyBindService;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG, "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.d(TAG, "onStartCommand");
		final String prefKeyMail = getResources().getString(R.string.pref_key_mail);
		final String prefKeyPass = getResources().getString(R.string.pref_key_pass);
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		mUser = sp.getString(prefKeyMail, ""); // コールバック関数内で使いたいので変数に入れとく。
		mPassword = sp.getString(prefKeyPass, ""); // コールバック関数内で使いたいので変数に入れとく。
		Logger.d("mail is \"" + mUser + "\"" + "\npassword is \"" + mPassword + "\"");

		if(mUser.equals("") || mPassword.equals("")) {
			if(mListener != null){
				try {
					mListener.onAccountNotValid();
				} catch (RemoteException e) {
					if(BuildConfig.DEBUG) e.printStackTrace();
				}
			}
			return START_STICKY;
		}

		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);	// onDestroy()で後始末をしたいので変数に入れる。

		final Criteria criteria = new Criteria();
		// PowerRequirement は設定しないのがベストプラクティス
		// Accuracy は設定しないのがベストプラクティス
		criteria.setAccuracy(Criteria.ACCURACY_FINE);//	← Accuracy で最もやってはいけないパターン
		// 以下は必要により
		criteria.setBearingRequired(false);	// 方位不要
		criteria.setSpeedRequired(false);	// 速度不要
		criteria.setAltitudeRequired(false);	// 高度不要

		final LocationProvider provider = getAvailableProviderOrNull(mLocationManager);

//		final String provider = mLocationManager.getBestProvider(criteria, true);
		Logger.d("provider=" + provider);
		if (provider == null) {
			// 位置情報が有効になっていない場合は、Google Maps アプリライクな [現在地機能を改善] ダイアログを起動します。
			// TODO 位置情報の取得が出来ない場合の処理
			if(mListener != null){
				try {
					mListener.onLocationProviderNotFound();
				} catch (RemoteException e) {
					if(BuildConfig.DEBUG) e.printStackTrace();
				}
			}
			return START_STICKY;
		}
		Logger.d("provider=" + provider.getName());
		// 最後に取得できた位置情報が5分以内のものであれば有効とします。
//		final Location lastKnownLocation = mLocationManager.getLastKnownLocation(provider);
//		Logger.d("lastLocation: " + lastKnownLocation);
//		if (lastKnownLocation != null && (new Date().getTime() - lastKnownLocation.getTime()) <= (5 * 60 * 1000L)) {
//			onLocationChanged(lastKnownLocation);
//			return START_STICKY;
//		}
		mLocationManager.requestLocationUpdates(provider.getName(), 0, 0, ImhereService.this);

		return START_STICKY;
	}
	@Override
	public void onDestroy() {
		if(mLocationManager != null) mLocationManager.removeUpdates(this);
		super.onDestroy();

		Logger.d(TAG, "onDestroy");
	}

	@Override
	public void onLocationChanged(final Location location) {
		Logger.d("onLocationChanged " + location);
		mLocationManager.removeUpdates(ImhereService.this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String title = "テスト送信";
				// 住所を取得
				final String prefKeyUseGeocoder = getResources().getString(R.string.pref_key_use_geocoder);
				final boolean useGeocoder = PreferenceManager.getDefaultSharedPreferences(ImhereService.this).getBoolean(prefKeyUseGeocoder, false);
				// 設定が有効な場合のみGeocoderから取得する
				String addressString = "";
				try{
					addressString = useGeocoder ? getAddress(location.getLatitude(), location.getLongitude()) : "";
				}catch(final Exception e){
					// Goecoderでのエラー
					if(mListener != null){
						// コールバックがあれば呼び出して終了
						try {
							mListener.onGeocoderNotWorking();
						} catch (RemoteException e1) {
							// TODO 自動生成された catch ブロック
							if(BuildConfig.DEBUG) e1.printStackTrace();
						}
						return;
					}
				}
				final String address = String.format(Locale.JAPANESE, "%s%s(%f, %f)",
						addressString
						,System.getProperty("line.separator")
						,location.getLatitude()
						,location.getLongitude()
						);
				final String message =  new MessageBuilder(address).toString();

				Logger.d("message: " + message);
				try {
					new GmailSender(mUser, mPassword).sendEmail(title, message, mUser);
					if(mListener != null) mListener.onComplete();
				} catch (Throwable e) {
					if(BuildConfig.DEBUG) e.printStackTrace();
					if(mListener != null){
						try {
							mListener.onMailSendingFailed(e.getClass().getName());
						} catch (RemoteException e1) {
							if(BuildConfig.DEBUG) e1.printStackTrace();
						}
					}
				}

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
	public static void registerWithAlarmManagerOneShot(final Context context){
		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final PendingIntent pendingIntent = createPendingIntent(context);
		alarmManager.cancel(pendingIntent);

		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
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

	private String getAddress(final double latitude, final double longitude) throws IOException{
		final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
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
		return "" + latitude + ", " + longitude;
	}

	private LocationProvider getAvailableProviderOrNull(final LocationManager locationManager){
		return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?  mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER)
				: locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ?  mLocationManager.getProvider(LocationManager.GPS_PROVIDER)
						: null;
	}


}
