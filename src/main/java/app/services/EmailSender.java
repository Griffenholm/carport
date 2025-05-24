package app.services;
import app.entities.Customer;
import app.entities.Order;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import java.util.Map;

public class EmailSender {

    private final GmailEmailSenderHTML emailSender;

    public EmailSender() {
        this.emailSender = new GmailEmailSenderHTML();
    }
    public void sendOrderConfirmationEmail(Order order) throws MessagingException {
        try {
            GmailEmailSenderHTML emailSender = new GmailEmailSenderHTML();

            // Write variables to from the order
            Map<String, Object> variables = Map.of(
                    "title", "Tak for din bestilling!",
                    "name", order.getCustomer().getName(),
                    "orderId", order.getOrderId(),
                    "salesperson", order.getSalesperson().getName(),
                    "salespersonNumber", order.getSalesperson().getPhoneNumber()
            );

            String html = emailSender.renderTemplate("ordre-email.html", variables);
            emailSender.sendHtmlEmail("Enilocin99@gmail.com", "Tak for din bestilling hos Fog Carporte", html);

        } catch (MessagingException e) {
            System.err.println("Fejl ved afsendelse af e-mail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendFinalOfferEmail(Order order) throws MessagingException {
        Map<String, Object> variables = Map.of(
                "name", order.getCustomer().getName(),
                "orderId", order.getOrderId(),
                "salesperson", order.getSalesperson().getName(),
                "salespersonNumber", order.getSalesperson().getPhoneNumber()
        );

        String html = emailSender.renderTemplate("tilbud-email.html", variables);
        emailSender.sendHtmlEmail("Enilocin99@gmail.com", "Dit carport tilbud er klar", html);
    }
}
