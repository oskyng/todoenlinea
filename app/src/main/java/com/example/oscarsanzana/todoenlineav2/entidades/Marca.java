package com.example.oscarsanzana.todoenlineav2.entidades;

public class Marca {
    private Integer marca_id;
    private String nombre_marca;

    public Marca() {
    }

    public Marca(Integer marca_id, String nombre_marca) {
        this.marca_id = marca_id;
        this.nombre_marca = nombre_marca;
    }

    public Marca(String nombre_marca) {
        this.nombre_marca = nombre_marca;
    }

    public Integer getId() {
        return marca_id;
    }

    public void setId(Integer marca_id) {
        this.marca_id = marca_id;
    }

    public String getNombcre_marca() {
        return nombre_marca;
    }

    public void setNombre_marca(String nombre_marca) {
        this.nombre_marca = nombre_marca;
    }
}
