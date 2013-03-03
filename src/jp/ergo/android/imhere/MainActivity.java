package jp.ergo.android.imhere;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// FIXME
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout);

		final EditText userEditText= new EditText(this);
		final EditText passwordEditText = new EditText(this);

		layout.addView(userEditText);
		layout.addView(passwordEditText);

		final Button submit = new Button(this);
		submit.setText("送信");
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String user = userEditText.getText().toString();
				final String password = passwordEditText.getText().toString();

				sendEmail(user, password);
			}
		});
		layout.addView(submit);
	}


	private void sendEmail(final String user, final String password){
		final Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");// SMTPサーバ名
		props.put("mail.smtp.port", "587"); // SMTPサーバポート
		props.put("mail.smtp.auth", "true");// smtp auth
		props.put("mail.smtp.starttls.enable", "true");// STTLS

		final Session sess = Session.getInstance(props);
		final MimeMessage mimeMsg = new MimeMessage(sess);

		try {

			mimeMsg.setFrom(new InternetAddress(user));//Fromアドレス
			mimeMsg.setRecipient(Message.RecipientType.TO, new InternetAddress(user));//送信先アドレス
			mimeMsg.setContent("body", "text/plain; utf-8");
			mimeMsg.setHeader("Content-Transfer-Encoding", "7bit");
			mimeMsg.setSubject("テスト送信");//件名
			mimeMsg.setText("アンドロイドからの送信", "utf-8");//本文

			final Transport transport = sess.getTransport("smtp");
			transport.connect(user, password);
			transport.sendMessage(mimeMsg, mimeMsg.getAllRecipients());// メール送信
			transport.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
