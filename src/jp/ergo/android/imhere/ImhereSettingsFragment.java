package jp.ergo.android.imhere;

import java.util.List;
import java.util.Map;

import jp.ergo.android.imhere.utils.Logger;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.google.common.collect.ImmutableMap;

public class ImhereSettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	private Map<String, String> mIntervalKeyValue;

	private String mPrefKeyInterval = "";
	private String mPrefKeyLaunch = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // preferenceのkeyを取得
        mPrefKeyInterval = getResources().getString(R.string.pref_key_interval);
        mPrefKeyLaunch = getResources().getString(R.string.pref_key_launch);


        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mIntervalKeyValue = createIntervalMap(this);

        // Interval Summary
        final Preference intervalPreference = findPreference(mPrefKeyInterval);
        final SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        final String interval = sharedPreferences.getString(mPrefKeyInterval, "");
        intervalPreference.setSummary(mIntervalKeyValue.get(interval));

        // サービス起動スイッチをサービス起動状態と合わせる
        final Editor editor = sharedPreferences.edit();
        editor.putBoolean(mPrefKeyLaunch, isServiceRunning());
        editor.commit();

//        findPreference(mPrefKeyMail).setSummary(sharedPreferences.getString(mPrefKeyMail, ""));
    }

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		Logger.d(key);
		if(key == null) return;
		if(key.equals(mPrefKeyLaunch)){
			if(sharedPreferences.getBoolean(key, false)){
				final int intervalMinutes = Integer.parseInt(sharedPreferences.getString(mPrefKeyInterval, "1440"));
				final long intervalMillis = (long)intervalMinutes * 60L * 1000;
				ImhereService.registerWithAlarmManager(getActivity(), intervalMillis);
			}else{
				ImhereService.unregisterWithAlermManager(getActivity());
			}
		}
		if(key.equals(mPrefKeyInterval)){
			final Preference intervalPreference = findPreference(mPrefKeyInterval);
	        final String interval = sharedPreferences.getString(mPrefKeyInterval, "");
	        intervalPreference.setSummary(mIntervalKeyValue.get(interval));
		}
	}
	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	private ImmutableMap<String, String> createIntervalMap(final Fragment fragment){
		final String[] intervalKey       = fragment.getResources().getStringArray(R.array.interval_key);
		final String[] intervalValue = fragment.getResources().getStringArray(R.array.interval_value);
		final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
		for(int i=0; i < intervalKey.length; i++){
			mapBuilder.put(intervalValue[i], intervalKey[i]);
		}
		return mapBuilder.build();
	}
	private boolean isServiceRunning() {
	    final ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
	    final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

	    for (final RunningServiceInfo info : services) {
	        if (ImhereService.class.getCanonicalName().equals(info.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}