/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;


import mx.desarollo.integration.ServiceFacadeLocator;
import mx.desarollo.entity.Usuario;

import java.io.Serializable;

public class LoginHelper implements Serializable {


    /**
     * Metodo para hacer login llamara a la instancia de usuarioFacade
     * @param correo
     * @param password
     * @return
     */

    public Usuario Login(String correo, String password){
        return ServiceFacadeLocator.getInstanceFacadeUsuario().login(password, correo);
    }
    public static void guardarUsuario(Usuario usuario){
        ServiceFacadeLocator.getInstanceFacadeUsuario().saveUsario(usuario);
    }
}