package mx.desarollo.facade;

import mx.desarollo.delegate.DelegateCliente;
import mx.desarollo.entity.Cliente;

public class FacadeCliente {
    private final DelegateCliente delegateCliente;

    public FacadeCliente() {
        this.delegateCliente = new DelegateCliente();
    }

    public java.util.List<Cliente> obtenerClientes(){
        return delegateCliente.findAllClientes();
    }

    public void guardarCliente(Cliente cliente){
        delegateCliente.saveCliente(cliente);
    }
}
