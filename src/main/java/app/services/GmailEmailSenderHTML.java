package app.services;

import app.config.ThymeleafConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Properties;

public class GmailEmailSenderHTML {

    private final String username;
    private final String password;
    private final TemplateEngine templateEngine;

    public GmailEmailSenderHTML() {
        // Get login from environment variables
        this.username = System.getenv("MAIL_USERNAME");
        this.password = System.getenv("MAIL_PASSWORD");

        if (username == null || password == null) {
            throw new IllegalStateException("MAIL_USERNAME og MAIL_PASSWORD miljøvariabler skal være sat.");
        }

        // Reuse the configuration from ThymeleafConfig
        this.templateEngine = ThymeleafConfig.templateEngine();
    }

    public String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    // 🧪 Main-method for testing
    public static void main(String[] args) {
        GmailEmailSenderHTML sender = new GmailEmailSenderHTML();

        String receiver = "Enilocin99@gmail.com";   // Erstat med din modtager
        String subject = "HTML test med Thymeleaf - PAYMENT";

        // Opret en Thymeleaf kontekst med variabler. Tilføj dine egne værdier.
        Map<String, Object> variables = Map.of(
                "title", "Velkommen!",
                "name", "Daniel",
                "message", "Dette er en HTML-mail genereret med Thymeleaf og sendt med Gmail SMTP.",
                "salesperson", "Anders And Andersen",
                "orderId", "12",
                "salespersonNumber", "99 88 77 66"
        );

        String html = sender.renderTemplate("tilbud-email.html", variables); // bruger templates/ordre-email.html

        try {
            sender.sendHtmlEmail(receiver, subject, html);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
