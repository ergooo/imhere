package jp.ergo.android.imhere;

import java.util.Map;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.google.common.collect.ImmutableMap;

public class ImhereSettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	private Map<String, String> mIntervalKeyValue;

	private String mPrefKeyInterval = "";
	private String mPrefKeyMail = "";
	private String mPrefKeyPass = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // preferenceのkeyを取得
        mPrefKeyInterval = getResources().getString(R.string.pref_key_interval);
        mPrefKeyMail = getResources().getString(R.string.pref_key_mail);
        mPrefKeyPass = getResources().getString(R.string.pref_key_pass);


        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mIntervalKeyValue = createIntervalMap(this);

        // Interval Summary
        final Preference intervalPreference = findPreference(mPrefKeyInterval);
        final SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        final String interval = sharedPreferences.getString(mPrefKeyInterval, "");
        intervalPreference.setSummary(mIntervalKeyValue.get(interval));

        findPreference(mPrefKeyMail).setSummary(sharedPreferences.getString(mPrefKeyMail, ""));
    }

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

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
}