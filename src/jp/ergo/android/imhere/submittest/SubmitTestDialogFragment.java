package jp.ergo.android.imhere.submittest;

import jp.ergo.android.imhere.ImhereBindService;
import jp.ergo.android.imhere.ImhereService;
import jp.ergo.android.imhere.ImhereServiceListener;
import jp.ergo.android.imhere.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public class SubmitTestDialogFragment extends DialogFragment{
	private final ServiceConnection mConnection;
	private ImhereBindService mService;

	private final SharedPreferences mSharedPreferences;
	public SubmitTestDialogFragment(final SharedPreferences sharedPreferences){
		mSharedPreferences = sharedPreferences;


		final ImhereServiceListener listener = new ImhereServiceListener.Stub() {
			//Callback methodを実装
			@Override
			public void onError(String message) throws RemoteException {
				// TODO 自動生成されたメソッド・スタブ

			}
		};

		mConnection = new ServiceConnection(){

			//ServiceConnection#onServiceConntected()の引数で渡される
			//IBinder objectを利用しAIDLで定義したInterfaceを取得
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = ImhereBindService.Stub.asInterface(service);
				try{
					//取得したinterfaceを利用しService用のAIDL fileで定義されたmethodでObserver登録/解除
					mService.setImhereServiceListener(listener);
				}catch(RemoteException e){
					// TODO
					e.printStackTrace();
				}
			}

			//Serviceを動かしてるProcessがcrashするかkillされない限り呼ばれない
			public void onServiceDisconnected(ComponentName name) {
				mService = null;
			}
		};


	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Intent intent = new Intent(getActivity(), ImhereService.class);
    	getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


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
