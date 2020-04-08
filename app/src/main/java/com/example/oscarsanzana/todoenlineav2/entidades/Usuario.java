package com.example.oscarsanzana.todoenlineav2.entidades;

public class Usuario {
    private Integer id;
    private String usuario;
    private String password;
    private String rol;
    private String sexo;
    private String rut;


    public Usuario() {
    }

    public Usuario(Integer id, String usuario, String password, String rol, String sexo, String rut) {
        this.id = id;
        this.usuario = usuario;
        this.password = password;
        this.rol = rol;
        this.sexo = sexo;
        this.rut = rut;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public Integer getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getPassword() {
        return password;
    }

    public String getRol() {
        return rol;
    }

    public String getSexo() {
        return sexo;
    }

    public String getRut() {
        return rut;
    }
}
