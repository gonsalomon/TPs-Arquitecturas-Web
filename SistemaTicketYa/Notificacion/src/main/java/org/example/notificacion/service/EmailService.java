package org.example.notificacion.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void enviarModificacion(
            String email,
            LocalDateTime fechaAnterior,
            LocalDateTime fechaNueva) {

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(email);
        mensaje.setSubject("Modificación de fecha de evento");

        mensaje.setText(
                "Hola!\n\n" +
                        "Te informamos que se ha modificado la fecha del evento " +
                        "para el que adquiriste entradas.\n\n" +
                        "Fecha anterior: " + fechaAnterior.format(FORMATO) + "\n" +
                        "Nueva fecha: " + fechaNueva.format(FORMATO) + "\n\n" +
                        "Lamentamos las molestias ocasionadas.\n" +
                        "Gracias por tu comprensión."
        );
        enviar(mensaje);
    }

    public void enviarCancelacion(String email) {

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(email);
        mensaje.setSubject("Cancelación de evento");

        mensaje.setText(
                "Hola!\n\n" +
                        "Te informamos que el evento para el que adquiriste " +
                        "entradas ha sido cancelado.\n\n" +
                        "Se iniciará el proceso correspondiente de reintegro.\n\n" +
                        "Lamentamos las molestias ocasionadas."
        );
        enviar(mensaje);
    }
    public void enviarConfirmacionOrden(String email) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Confirmacion de compra");
        mensaje.setText(
                "Hola!\n\n" +
                        "Te informamos que tu pago ha sido acreditado correctamente " +
                        "y que tu compra de entradas para el evento fue confirmada.\n\n" +
                        "Si deseás cancelar tu compra, podrás iniciar el proceso " +
                        "de solicitud de reintegro hasta 72 horas antes del evento.\n\n" +
                        "¡Esperamos que disfrutes del evento!\n\n" +
                        "¡Gracias por tu compra!"
        );
        enviar(mensaje);
    }
    public void enviarNuevoEventoArtista(String email) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Tu artista favorito estara en tu ciudad pronto!");
        mensaje.setText(
                "Hola!\n\n" +
                        "Te informamos que tu artista favorito " +
                        "realizara un show en los siguientes meses en tu ciudad.\n\n" +
                        "Si deseás adquirir entradas para el evento, estas " +
                        "estaran disponibles proximamente en los locales oficiales.\n\n" +
                        "¡Esperamos que te sumes al evento!\n\n"
        );
        enviar(mensaje);
    }

    private void enviar(SimpleMailMessage mensaje) {
        try {
            mailSender.send(mensaje);
        } catch (org.springframework.mail.MailException e) {
            log.warn("Fallo el envío de mail a {}: {}", java.util.Arrays.toString(mensaje.getTo()), e.getMessage());
        }
    }
}