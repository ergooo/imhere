package jp.ergo.android.imhere.submittest;

import jp.ergo.android.imhere.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
/**
 * Geocoderが壊れてる時に出るダイアログ
 */
public class GeocoderNotWorkingDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
		.setTitle("住所を取得できません")
		.setMessage("住所を取得することが出来ませんでした。\n「"+getResources().getString(R.string.use_geocoder) + "」を無効にして再度お試し下さい。")
		.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			@Override public void onClick(final DialogInterface dialog, final int which) {}	// 何も行わない
		})
		.create();
	}
}
