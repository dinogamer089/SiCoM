package ui;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.Serializable;
import java.util.Properties;

@Named("contactoBean")
@ViewScoped
public class ContactoBean implements Serializable {

    private String nombre;
    private String apellido;
    private String correo;
    private String asunto;
    private String mensaje;

    /**
     * Metodo para procesar el envío del correo electrónico de contacto.
     * Configura las propiedades SMTP de Gmail, crea la sesión autenticada y transporta el mensaje al destinatario.
     * @Throws Si el servidor SMTP rechaza la conexión, la autenticación falla o ocurre un error de transporte (MessagingException).
     */
    public void enviar() {
        final String miCorreo = "contactoeventoscampestre@gmail.com";
        final String miPassword = "gdvj tjjo ycxq ltno";
        final String correoDestino = "diego.sanchez33@uabc.edu.mx";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.starttls.required", "true");
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(prop, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(miCorreo, miPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(miCorreo));

            // A quién le llega el correo
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));

            message.setSubject("Contacto Web: " + asunto);

            String contenido = "Nombre: " + nombre + " " + apellido + "\n" +
                    "Correo cliente: " + correo + "\n\n" +
                    "Mensaje:\n" + mensaje;

            message.setText(contenido);

            Transport.send(message);

            // Mensaje de éxito en pantalla
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "¡Enviado!", "Correo enviado con éxito."));

            limpiarFormulario();

        } catch (MessagingException e) {
            e.printStackTrace(); // Imprime el error en la consola para verlo
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Fallo al enviar: " + e.getMessage()));
        }
    }

    /**
     * Metodo auxiliar para restablecer los campos del formulario.
     * Se invoca únicamente tras un envío exitoso para limpiar la vista.
     * @Throws No aplica excepciones específicas, operación en memoria.
     */
    private void limpiarFormulario() {
        this.nombre = ""; this.apellido = ""; this.correo = ""; this.asunto = ""; this.mensaje = "";
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}