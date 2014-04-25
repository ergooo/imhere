/*
 * Copyright (C) 2012-2013 Japan Smartphone Security Association
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.ergo.android.imhere.fragment;

import jp.ergo.android.imhere.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordActivity extends Activity {

    // 状態保存用のキー
    private static final String KEY_DUMMY_PASSWORD = "KEY_DUMMY_PASSWORD";

    // Activity内のView
    private EditText mPasswordEdit;
    private CheckBox mPasswordDisplayCheck;

    // パスワードがダミー表示かを表すフラグ
    private boolean mIsDummyPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gmail_account_layout);

        // Viewの取得
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        mPasswordDisplayCheck = (CheckBox) findViewById(R.id.password_display_check);

        // 前回入力パスワードがあるか
        if (getPreviousPassword() != null) {
            // ★ポイント4★ Activity初期表示時に前回入力したパスワードがある場合、
        	// 前回入力パスワードの桁数を推測されないよう固定桁数の●文字でダミー表示する

            // 表示はダミーパスワードにする
            mPasswordEdit.setText("**********");
            // パスワード入力時にダミーパスワードをクリアするため、テキスト変更リスナーを設定
            mPasswordEdit.addTextChangedListener(new PasswordEditTextWatcher());
            // ダミーパスワードフラグを設定する
            mIsDummyPassword = true;
        }

        // パスワードを表示するオプションのチェック変更リスナーを設定
        mPasswordDisplayCheck
                .setOnCheckedChangeListener(new OnPasswordDisplayCheckedChangeListener());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // 画面の縦横変更でActivityが再生成されないよう指定した場合には不要
        // Activityの状態保存
        outState.putBoolean(KEY_DUMMY_PASSWORD, mIsDummyPassword);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // 画面の縦横変更でActivityが再生成されないよう指定した場合には不要
        // Activityの状態の復元
        mIsDummyPassword = savedInstanceState.getBoolean(KEY_DUMMY_PASSWORD);
    }

    /**
     * パスワードを入力した場合の処理
     */
    private class PasswordEditTextWatcher implements TextWatcher {

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // 未使用
        }

        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        	System.out.println(mIsDummyPassword);
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

        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            // ★ポイント5★ 前回入力パスワードをダミー表示しているとき、「パスワードを表示」した場合、
        	// 前回入力パスワードをクリアして、新規にパスワードを入力できる状態とする
            if (mIsDummyPassword && isChecked) {
                // ダミーパスワードフラグを設定する
                mIsDummyPassword = false;
                // パスワードを空表示にする
                mPasswordEdit.setText(null);
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
        return "hirake5ma";
    }

    /**
     * キャンセルボタンの押下処理
     *
     * @param view
     */
    public void onClickCancelButton(View view) {
        // Activityを閉じる
        finish();
    }

    /**
     * OKボタンの押下処理
     *
     * @param view
     */
    public void onClickOkButton(View view) {
        // passwordを保存するとか認証に使うとか必要な処理を行う

        String password = null;

        if (mIsDummyPassword) {
            // 最後までダミーパスワード表示だった場合は前回入力パスワードを確定パスワードとする
            password = getPreviousPassword();
        } else {
            // ダミーパスワード表示じゃない場合はユーザー入力パスワードを確定パスワードとする
            password = mPasswordEdit.getText().toString();
        }

        // パスワードをToast表示する
        Toast.makeText(this, "password is \"" + password + "\"",
                Toast.LENGTH_SHORT).show();

        // Activityを閉じる
        finish();
    }
}