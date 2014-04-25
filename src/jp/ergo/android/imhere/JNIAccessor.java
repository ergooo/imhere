package jp.ergo.android.imhere;

public class JNIAccessor {
    static {
        System.loadLibrary("imherejni");
    }
	public static native String stringA();
	public static native String stringB();

}
