package jp.ergo.android.imhere;

import jp.ergo.android.imhere.ImhereServiceListener;

interface ImhereBindService{


	//AIDL fileで定義されるmethodの引数、戻り値に指定出来るのはprimitive型、String、
	//List、Map、CharSequence、その他のAIDLに定義されたinterface、Parcelable interfaceを実装したclass
	//ImhereServiceListenerはAIDLで定義されているのでOK
	void setImhereServiceListener(ImhereServiceListener observer);
}