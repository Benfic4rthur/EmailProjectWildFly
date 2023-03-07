package br.com.emailproject.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import br.com.emailproject.model.Email;
import br.com.emailproject.util.LogUtil;

@Stateless
public class EmailService extends Thread {

	private List<Email> emails;

	private static final String HEADER_CONTEXT = "text/html; charset=utf-8";

	public void enviar(Email email) {

		emails = new ArrayList<>();
		emails.add(email);
		send();

	}

	public void enviar(List<Email> emails) {
		this.emails = emails;
		send();
	}

	private EmailService copy() {
		EmailService emailService = new EmailService();
		emailService.emails = emails;
		return emailService;
	}

	public void send() {
		new Thread(this.copy()).start();
	}

	@Override
	public void run() {
		Properties properties = new Properties();
		properties.put("mail.smpt.starttls.enable", true);
		properties.put("mail.smpt.host", System.getProperty("email-project,mail.smtp.host"));
		properties.put("mail.smpt.port", System.getProperty("email-project,mail.smtp.port"));

		javax.mail.Session session = javax.mail.Session.getInstance(properties);
		session.setDebug(false);

		for (Email email : emails) {

			try {
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(System.getProperty("email-project.mail.from")));

				if (email.getDestinatario().contains("/")) {
					List<InternetAddress> emailsLocal = new ArrayList<>();
					for (String e : email.getDestinatario().split("/")) {
						emailsLocal.add(new InternetAddress(e));
					}
					message.addRecipients(Message.RecipientType.TO, emailsLocal.toArray(new InternetAddress[0]));
				} else {
					InternetAddress para = new InternetAddress();
					message.addRecipient(Message.RecipientType.TO, para);

				}
				message.setSubject(email.getAssunto());
				MimeBodyPart textpart = new MimeBodyPart();
				textpart.setHeader("Content-Type", HEADER_CONTEXT);
				textpart.setContent(email.getTexto(), HEADER_CONTEXT);
				Multipart mp = new MimeMultipart();
				mp.addBodyPart(textpart);
				message.setContent(mp);
				Transport.send(message);

			} catch (MessagingException e) {
				LogUtil.getLogger(EmailService.class).error("Erro ao enviar e-mail!" + e.getMessage());
			}
		}
	}
}