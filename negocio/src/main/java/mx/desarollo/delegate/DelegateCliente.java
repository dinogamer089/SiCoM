package mx.desarollo.delegate;

import mx.avanti.desarollo.integration.ServiceLocator;
import mx.desarollo.entity.Cliente;

import java.util.List;

public class DelegateCliente {
    public List<Cliente> findAllClientes(){
        return ServiceLocator.getInstanceClienteDAO().findAll();
    }

    public void saveCliente(Cliente cliente){
        ServiceLocator.getInstanceClienteDAO().save(cliente);}
}
