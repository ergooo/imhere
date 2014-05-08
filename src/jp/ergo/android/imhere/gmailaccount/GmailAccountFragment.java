package jp.ergo.android.imhere.gmailaccount;

import jp.ergo.android.imhere.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class GmailAccountFragment extends Fragment{

	private String mPrefKeyMail = "";
	private String mPrefKeyPass = "";

    // 状態保存用のキー
    private static final String KEY_DUMMY_PASSWORD = "KEY_DUMMY_PASSWORD";

    // Activity内のView
    private CheckBox mPasswordDisplayCheck;

    // パスワードがダミー表示かを表すフラグ
    private boolean mIsDummyPassword;

    @Override
    public View onCreateView(
    		LayoutInflater inflater,
    		ViewGroup container,
    		Bundle savedInstanceState) {
    	if(savedInstanceState != null){
    		mIsDummyPassword = savedInstanceState.getBoolean(KEY_DUMMY_PASSWORD);
    	}
    	return inflater.inflate(R.layout.gmail_account_layout, container, false);
    }

    @Override
    public void onStart(){
    	super.onStart();
    	mPrefKeyMail = getResources().getString(R.string.pref_key_mail);
    	mPrefKeyPass = getResources().getString(R.string.pref_key_pass);

    	// Viewの取得
    	final EditText passwordEdit = (EditText) getView().findViewById(R.id.password_edit);
        mPasswordDisplayCheck = (CheckBox) getView().findViewById(R.id.password_display_check);

        // 前回入力パスワードがあるか
        if (getPreviousPassword() != null) {
            // ★ポイント4★ Activity初期表示時に前回入力したパスワードがある場合、
        	// 前回入力パスワードの桁数を推測されないよう固定桁数の●文字でダミー表示する

            // 表示はダミーパスワードにする
            passwordEdit.setText("**********");
            // パスワード入力時にダミーパスワードをクリアするため、テキスト変更リスナーを設定
            passwordEdit.addTextChangedListener(new PasswordEditTextWatcher(passwordEdit));
            // ダミーパスワードフラグを設定する
            mIsDummyPassword = true;
        }

        // パスワードを表示するオプションのチェック変更リスナーを設定
        mPasswordDisplayCheck
                .setOnCheckedChangeListener(new OnPasswordDisplayCheckedChangeListener(passwordEdit));

        final EditText mailEdit = (EditText)getView().findViewById(R.id.mail_edit);
        mailEdit.setText(getPreviousEmail());
        final Button submitButton = (Button)getView().findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String mail = mailEdit.getText().toString();
				final String pass = mIsDummyPassword ? getPreviousPassword() : passwordEdit.getText().toString();
				final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
				final Editor editor = sharedPreferences.edit();
				editor.putString(mPrefKeyMail, mail);
				editor.putString(mPrefKeyPass, pass);
				editor.commit();
				Toast.makeText(getActivity(), "mail is \"" + mail + "\"" + "\npassword is \"" + pass + "\"", Toast.LENGTH_SHORT).show();
			}
		});
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // 画面の縦横変更でActivityが再生成されないよう指定した場合には不要
        // Activityの状態保存
        outState.putBoolean(KEY_DUMMY_PASSWORD, mIsDummyPassword);
    }

    /**
     * パスワードを入力した場合の処理
     */
    private class PasswordEditTextWatcher implements TextWatcher {
    	private EditText mPasswordEdit;

    	public PasswordEditTextWatcher(final EditText editText){
    		mPasswordEdit = editText;
    	}

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // 未使用
        }

        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            // ★ポイント6★ 前回入力パスワードをダミー表示しているとき、ユーザーがパスワードを入力しようと
        	// した場合、前回入力パスワードをクリアし、ユーザーの入力を新たなパスワードとして扱う
            if (mIsDummyPassword) {
                // ダミーパスワードフラグを設定する
                mIsDummyPassword = false;
                // パスワードを入力した文字だけにする
                CharSequence work = s.subSequence(start, start + count);
                mPasswordEdit.setText(work);
                // カーソル位置が最初に戻るので最後にする
                mPasswordEdit.setSelection(work.length());
            }
        }

        public void afterTextChanged(Editable s) {
            // 未使用
        }

    }

    /**
     * パスワードの表示オプションチェックを変更した場合の処理
     */
    private class OnPasswordDisplayCheckedChangeListener implements
            OnCheckedChangeListener {
    	private EditText mPasswordEdit;

    	public OnPasswordDisplayCheckedChangeListener(final EditText editText){
    		mPasswordEdit = editText;
    	}

        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            // ★ポイント5★ 前回入力パスワードをダミー表示しているとき、「パスワードを表示」した場合、
        	// 前回入力パスワードをクリアして、新規にパスワードを入力できる状態とする
            if (mIsDummyPassword && isChecked) {
                // ダミーパスワードフラグを設定する
                mIsDummyPassword = false;
                // パスワードを空表示にする
                mPasswordEdit.setText("");
            }

            // カーソル位置が最初に戻るので今のカーソル位置を記憶する
            int pos = mPasswordEdit.getSelectionStart();

            // ★ポイント2★ パスワードを平文表示するオプションを用意する
            // InputTypeの作成
            int type = InputType.TYPE_CLASS_TEXT;
            if (isChecked) {
                // チェックON時は平文表示
                type |= InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
            } else {
                // チェックOFF時はマスク表示
                type |= InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }

            // パスワードEditTextにInputTypeを設定
            mPasswordEdit.setInputType(type);

            // カーソル位置を設定する
            mPasswordEdit.setSelection(pos);
        }

    }

    // 以下のメソッドはアプリに合わせて実装すること

    /**
     * 前回入力パスワードを取得する
     *
     * @return 前回入力パスワード
     */
    private String getPreviousPassword() {
        // 保存パスワードを復帰させたい場合にパスワード文字列を返す
    	// パスワードを保存しない用途ではnullを返す
    	final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    	return sharedPreferences.getString(mPrefKeyPass, "");
    }

    private String getPreviousEmail(){
    	final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		return sharedPreferences.getString(mPrefKeyMail, "");
    }
}
