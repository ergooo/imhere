package jp.ergo.android.imhere;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GmailSender {
	private final String mUser;
	private final String mPassword;
	
	public GmailSender(final String user, final String password){
		mUser = user;
		mPassword = password;
	}
	/**
	 * @param title
	 * @param message
	 * @param to 送信先メールアドレス
	 */
	public void sendEmail(final String title, final String message, final String to){
		System.out.println("sendEmail(): title: " + title + ", message: " + message);
		final Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");// SMTPサーバ名
		props.put("mail.smtp.port", "587"); // SMTPサーバポート
		props.put("mail.smtp.auth", "true");// smtp auth
		props.put("mail.smtp.starttls.enable", "true");// STTLS

		final Session sess = Session.getInstance(props);
		final MimeMessage mimeMsg = new MimeMessage(sess);

		try {

			mimeMsg.setFrom(new InternetAddress(mUser));//Fromアドレス
			mimeMsg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));//送信先アドレス
			mimeMsg.setContent("body", "text/plain; utf-8");
			mimeMsg.setHeader("Content-Transfer-Encoding", "7bit");
			mimeMsg.setSubject(title);//件名
			mimeMsg.setText(message, "utf-8");//本文

			final Transport transport = sess.getTransport("smtp");
		
			try{
				transport.connect(mUser, mPassword);
				transport.sendMessage(mimeMsg, mimeMsg.getAllRecipients());// メール送信
			}finally{
				transport.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
