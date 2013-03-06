package jp.ergo.android.imhere;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class ImhereService extends Service implements LocationListener {

	private final static String TAG = "ImhereService";
	private static final long DELAY = 1000;
	private static final long PERIOD = 60000;
	private final Timer mTimer = new Timer();
	private String mUser = "";
	private String mPassword = "";
	private LocationManager mLocationManager;

	private native String stringA();

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
		final Handler handler = new Handler();
		mUser = intent.getStringExtra("user");	// コールバック関数内で使いたいので変数に入れとく。
		mPassword = intent.getStringExtra("password"); // コールバック関数内で使いたいので変数に入れとく。
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);	// onDestroy()で後始末をしたいので変数に入れる。
		
		// タスクを実行。
		final TimerTask task = new TimerTask() {
			@Override
			public void run() {
				final LocationProvider provider = mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER);
				// requestLocationUpdates()はメインスレッドでないと実行できない。
				handler.post(new Runnable() {
					@Override
					public void run() {
						mLocationManager.requestLocationUpdates(provider.getName(), 60 * 60, 0, ImhereService.this);
					}
				});
			}
		};
//		mTimer.schedule(task, DELAY, PERIOD);
		
		System.out.println(stringA());

		return START_STICKY;
	}
	@Override
	public void onDestroy() {
		mTimer.cancel();
		mLocationManager.removeUpdates(this);
		super.onDestroy();
		
		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onLocationChanged(final Location location) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String title = "テスト送信";
				final String address = getAddress(location.getLatitude(), location.getLongitude());
				final String message =  new MessageBuilder(address).toString();
				
				new GmailSender(mUser, mPassword).sendEmail(title, message, mUser);
				mLocationManager.removeUpdates(ImhereService.this);
			}
		}).start();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	private String getAddress(final double latitude, final double longitude){
		final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
		try {
			// Call the synchronous getFromLocation() method by passing in the lat/long values.
			final List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
			if (addresses != null && addresses.size() > 0) {
				final Address address = addresses.get(0);
				final StringBuilder builder = new StringBuilder();
				builder.append(address.getAdminArea());
				builder.append(address.getLocality());
				builder.append(System.getProperty("line.separator"));
				builder.append(address.getSubAdminArea() != null ? address.getSubAdminArea(): "");
				builder.append(address.getFeatureName());
				builder.append(address.getThoroughfare());
				return builder.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "" + latitude + ", " + longitude;
	}
	
}