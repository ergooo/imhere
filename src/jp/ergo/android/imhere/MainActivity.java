package jp.ergo.android.imhere;

import java.util.Map.Entry;
import java.util.Properties;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
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
		// FIXME
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);

		final EditText userEditText= new EditText(this);
		final EditText passwordEditText = new EditText(this);

		layout.addView(userEditText);
		layout.addView(passwordEditText);

		final Button submit = new Button(this);
		submit.setText("送信");
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String user = userEditText.getText().toString();
				final String password = passwordEditText.getText().toString();
				
				final Intent intent = new Intent(getBaseContext(), ImhereService.class);
				intent.putExtra("user", user);
				intent.putExtra("password", password);
				startService(intent);
			}
		});
		layout.addView(submit);
		
		final Button stopButton = new Button(this);
		stopButton.setText("停止");
		stopButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				stopService(new Intent(getBaseContext(),ImhereService.class));
			}
		});
		
		layout.addView(stopButton);
		
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
