package jp.ergo.android.imhere.submittest;

import jp.ergo.android.imhere.ImhereService;
import jp.ergo.android.imhere.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

public class SubmitTestDialogFragment extends DialogFragment{

	private final SharedPreferences mSharedPreferences;
	public SubmitTestDialogFragment(final SharedPreferences sharedPreferences){
		mSharedPreferences = sharedPreferences;

	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.submit_test_dialog_title);
		builder.setMessage(createDialogMessage())
		.setPositiveButton(R.string.submit_test_dialog_positive, new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				ImhereService.unregisterWithAlermManager(getActivity());
        		ImhereService.registerWithAlarmManagerOneShot(getActivity());

			}
		})
		.setNegativeButton(R.string.submit_test_dialog_negative, new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
			}
		});
		// Create the AlertDialog object and return it
		return builder.create();
	}

	private String createDialogMessage(){
		final String messageFormat = getResources().getString(R.string.submit_test_dialog_message);
		return String.format(messageFormat, mSharedPreferences.getString(getResources().getString(R.string.pref_key_mail), ""));
	}
}

