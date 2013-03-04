package jp.ergo.android.imhere;

import android.os.Build;

public class MessageBuilder {
	private final String mAddress;
	public MessageBuilder(final String address){
		mAddress = address;
	}
	
	@Override
	public String toString(){
		final String manifacturer = Build.MANUFACTURER;
		final String model = Build.MODEL;
		final String release = Build.VERSION.RELEASE;
		final StringBuilder builder = new StringBuilder();
		
		builder.append(manifacturer);
		builder.append(" ");
		builder.append(model);
		builder.append(" (");
		builder.append("Android ");
		builder.append(release);
		builder.append(")");
		builder.append(System.getProperty("line.separator"));
		builder.append(mAddress);

		
		
		return builder.toString();
	}
}
