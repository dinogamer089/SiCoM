/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;


import mx.desarollo.entity.Administrador;
import mx.desarollo.entity.Empleado;
import mx.desarollo.integration.ServiceFacadeLocator;
import java.io.Serializable;

public class LoginHelper implements Serializable {


    /**
     * Metodo para hacer login llamara a la instancia de usuarioFacade
     * @param correo
     * @param password
     * @return
     */

    public Object login(String correo, String password) {
        return ServiceFacadeLocator.getInstanceFacadeLogin().login(password, correo);
    }
    public void saveAdministrador(Administrador admin) {
        ServiceFacadeLocator.getInstanceFacadeLogin().saveAdministrador(admin);
    }

    public void saveEmpleado(Empleado empleado) {
        ServiceFacadeLocator.getInstanceFacadeLogin().saveEmpleado(empleado);
    }
}