package jp.ergo.android.imhere.submittest;

import jp.ergo.android.imhere.gmailaccount.GmailAccountActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * メールアドレスかパスワードがない場合に出るダイアログ
 *
 */
public class AccountNotValidDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
		.setTitle("アカウント情報を確認して下さい")
		.setMessage("メールアドレスまたはパスワードが正しくないかもしれません。\nアカウント設定画面よりアカウント情報を確認して下さい。")
		.setPositiveButton("設定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				getActivity().startActivity(new Intent(getActivity(), GmailAccountActivity.class));
			}
		})
		.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			@Override public void onClick(final DialogInterface dialog, final int which) {}	// 何も行わない
		})
		.create();
	}
}
