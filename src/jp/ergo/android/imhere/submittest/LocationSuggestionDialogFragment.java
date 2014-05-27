package jp.ergo.android.imhere.submittest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * 位置情報サービスが無効の場合に出るダイアログ
 */
public class LocationSuggestionDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
		.setTitle("現在地機能を改善")
		.setMessage("現在、位置情報は一部有効ではないものがあります。次のように設定すると、もっともすばやく正確に現在地を検出できるようになります:\n\n● 位置情報の設定でGPSとワイヤレスネットワークをオンにする\n\n● Wi-Fiをオンにする")
		.setPositiveButton("設定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				// 端末の位置情報設定画面へ遷移
				try {
					getActivity().startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
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
