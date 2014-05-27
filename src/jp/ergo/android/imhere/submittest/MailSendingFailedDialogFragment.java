package jp.ergo.android.imhere.submittest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class MailSendingFailedDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
		.setTitle("メールの送信に失敗しました")
		.setMessage("メール送信時に何らかのエラーが発生しました。\nアカウント情報、ネットワーク状況、位置情報設定を確認し、再度お試し下さい。")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
			}
		})
		.create();
	}
}
