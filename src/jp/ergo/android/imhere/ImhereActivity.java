package jp.ergo.android.imhere;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class ImhereActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// SharedPreferencesを初期化（第3引数をfalseとすることで初回起動時のみ初期化される）
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new ImhereSettingsFragment())
        .commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}
}
