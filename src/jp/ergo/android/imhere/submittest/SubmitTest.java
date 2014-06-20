package jp.ergo.android.imhere.submittest;

import javax.mail.AuthenticationFailedException;
import javax.mail.internet.AddressException;

import jp.ergo.android.imhere.ImhereBindService;
import jp.ergo.android.imhere.ImhereService;
import jp.ergo.android.imhere.ImhereServiceListener;
import jp.ergo.android.imhere.utils.Logger;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;

public class SubmitTest {
	final Context mContext;
	private final ServiceConnection mConnection;
	private ImhereBindService mService;
	private final ProgressDialog mProgressDialog;
	public SubmitTest(final Context context, final FragmentManager fragmentManager){
		mContext = context;
		// unbindするときのために変数に入れとく
		mConnection = createServiceConnection(fragmentManager);
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setMessage("Loading...");
		mProgressDialog.setCancelable(false);
	}

	public void showSubmitTestDialog(final FragmentManager fragmentManager){
		final Intent intent = new Intent(mContext, ImhereService.class);
    	mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		new SubmitTestDialogFragment(PreferenceManager.getDefaultSharedPreferences(mContext), mProgressDialog).show(fragmentManager, "submitTestDialog");

	}
	/**
	 * サービスに渡すコールバックを作成
	 * @param fragmentManager
	 * @return
	 */
	private ImhereServiceListener createServiceListener(final FragmentManager fragmentManager){
		final ImhereServiceListener listener = new ImhereServiceListener.Stub() {
			//Callback methodを実装

			@Override
			public void onLocationProviderNotFound() throws RemoteException {
				new LocationSuggestionDialogFragment().show(fragmentManager, "dialog");
				mContext.unbindService(mConnection);
				if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
			}

			@Override
			public void onAccountNotValid() throws RemoteException {
				new AccountNotValidDialogFragment().show(fragmentManager, "dialog");
				mContext.unbindService(mConnection);
				if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();

			}

			@Override
			public void onGeocoderNotWorking() throws RemoteException {
				new GeocoderNotWorkingDialogFragment().show(fragmentManager, "dialog");
				mContext.unbindService(mConnection);
				if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();

			}

			@Override
			public void onMailSendingFailed(String message) throws RemoteException {
				Logger.e(message);
				if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
				if(message.equals(AuthenticationFailedException.class.getName())
						|| message.equals(AddressException.class.getName())){
					// 認証失敗につきアカウント確認ダイアログを出す
					new AccountNotValidDialogFragment().show(fragmentManager, "dialog");
					mContext.unbindService(mConnection);
				}else{
					new MailSendingFailedDialogFragment().show(fragmentManager, "dialog");
					mContext.unbindService(mConnection);
				}
				if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
			}

			@Override
			public void onComplete() throws RemoteException {
				Logger.d("onComplete()");
				if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
				mContext.unbindService(mConnection);
			}

		};
		return listener;
	}

	private ServiceConnection createServiceConnection(final FragmentManager fragmentManager) {
		// TODO 自動生成されたメソッド・スタブ
		return  new ServiceConnection(){

			//ServiceConnection#onServiceConntected()の引数で渡される
			//IBinder objectを利用しAIDLで定義したInterfaceを取得
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = ImhereBindService.Stub.asInterface(service);
				try{
					//取得したinterfaceを利用しService用のAIDL fileで定義されたmethodでObserver登録/解除
					mService.setImhereServiceListener(createServiceListener(fragmentManager));
				}catch(RemoteException e){
					// TODO
					e.printStackTrace();
				}
			}

			//Serviceを動かしてるProcessがcrashするかkillされない限り呼ばれない
			public void onServiceDisconnected(ComponentName name) {
				Logger.d("SubmitTest#onServiceDisconnected() " + name);
				mService = null;
			}
		};
	}

}
