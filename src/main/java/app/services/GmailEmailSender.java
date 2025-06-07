package app.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class GmailEmailSender {

    private final String username;
    private final String password;

    public GmailEmailSender() {
        this.username = System.getenv("MAIL_USERNAME");
        this.password = System.getenv("MAIL_PASSWORD");

        if (username == null || password == null) {
            throw new IllegalStateException("MAIL_USERNAME and MAIL_PASSWORD environment variables must be set.");
        }
    }

    public void sendPlainTextEmail(String receiver, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        // Opretter SMTP egenskaber (standardkonfiguration for at sende mails via Gmail SMTP)
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // TLS
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        // Autentificering:
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        // Opret en email:
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
        message.setSubject(subject);
        message.setText(body); // Plain text only
        // Send email
        Transport.send(message);
        System.out.println("Email sent successfully to " + receiver);
    }

    // 🧪 Main-method for testing
    public static void main(String[] args) {
        GmailEmailSender sender = new GmailEmailSender();

        String to = "Enilocin99@gmail.com";  // Erstat med din modtager
        String subject = "Testmail fra Java";
        String body = "Hej! Dette er en simpel testmail sendt med Java og Jakarta Mail.";

        try {
            sender.sendPlainTextEmail(to, subject, body);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
