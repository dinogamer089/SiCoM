package ui;

import helper.ClienteHelper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import mx.desarollo.entity.Cliente;

import java.io.Serializable;
import java.util.List;

/**
 * Managed Bean para manejar la lógica de la interfaz de usuario de Clientes.
 */
@Named("clienteUI")
@SessionScoped
public class ClienteBeanUI implements Serializable {

    private ClienteHelper clienteHelper;
    private List<Cliente> clientes;
    private Cliente nuevoCliente;

    public ClienteBeanUI() {
        clienteHelper = new ClienteHelper();
    }

    @PostConstruct
    public void init() {
        nuevoCliente = new Cliente();
    }

    public void obtenerTodosLosClientes() {
        try {
            clientes = clienteHelper.obtenerTodas();
            System.out.println("Lista de clientes cargada. Total: " + clientes.size());
        } catch (Exception e) {
            System.err.println("Error al cargar la lista de clientes: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo cargar la lista de clientes.");
        }
    }

    public void guardarCliente() {
        try {
            if (nuevoCliente.getNombre() == null || nuevoCliente.getNombre().trim().isEmpty()) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "El nombre del cliente es obligatorio");
                return;
            }
            if (nuevoCliente.getTelefono() == null || nuevoCliente.getTelefono().trim().isEmpty()) {
                mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "El teléfono es obligatorio");
                return;
            }

            clienteHelper.guardarCliente(nuevoCliente);
            mostrarMensaje(FacesMessage.SEVERITY_INFO, "Éxito", "Cliente " + nuevoCliente.getNombre() + " creado correctamente.");

            obtenerTodosLosClientes();

            nuevoCliente = new Cliente();

        } catch (Exception e) {
            System.err.println("Error al guardar cliente: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el cliente: " + e.getMessage());
        }
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

    public Cliente getNuevoCliente() {
        return nuevoCliente;
    }

    public void setNuevoCliente(Cliente nuevoCliente) {
        this.nuevoCliente = nuevoCliente;
    }

    private void mostrarMensaje(FacesMessage.Severity severity, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, resumen, detalle));
    }
}