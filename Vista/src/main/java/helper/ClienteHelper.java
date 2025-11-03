package helper;

import mx.desarollo.entity.Cliente;
import mx.desarollo.integration.ServiceFacadeLocator;

import java.util.ArrayList;
import java.util.List;

public class ClienteHelper {
    public List<Cliente> obtenerTodas() {
        System.out.println("=== ClienteHelper.obtenerTodas() ===");
        try {
            List<Cliente> clientes = ServiceFacadeLocator.getInstanceFacadeCliente().obtenerClientes();
            System.out.println("✓ Clientes obtenidos: " + (clientes != null ? clientes.size() : "null"));

            // Si es null, retornar lista vacía en lugar de null
            if (clientes == null) {
                return new ArrayList<>();
            }

            return clientes;
        } catch (Exception e) {
            System.err.println("✗ ERROR en ClienteHelper:");
            System.err.println("  Tipo: " + e.getClass().getName());
            System.err.println("  Mensaje: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, retornar lista vacía en lugar de lanzar excepción
            return new ArrayList<>();
        }
    }

    public void guardarCliente(Cliente cliente) {
        System.out.println("=== ClienteHelper.crearCliente() ===");
        try {
            ServiceFacadeLocator.getInstanceFacadeCliente().guardarCliente(cliente);
            System.out.println("✓ Cliente creado exitosamente");
        } catch (Exception e) {
            System.err.println("✗ ERROR al crear cliente:");
            e.printStackTrace();
            throw e;
        }
    }
}
