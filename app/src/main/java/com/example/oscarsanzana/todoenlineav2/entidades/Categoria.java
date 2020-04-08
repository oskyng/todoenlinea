package com.example.oscarsanzana.todoenlineav2.entidades;

public class Categoria {

    private Integer categoria_id;
    private String nombre_categoria, descripcion_categoria;
    private String status;

    public Categoria() {
    }

    public Categoria(Integer categoria_id, String nombre_categoria, String descripcion_categoria) {
        this.categoria_id = categoria_id;
        this.nombre_categoria = nombre_categoria;
        this.descripcion_categoria = descripcion_categoria;
    }

    public Integer getCategoria_id() {
        return categoria_id;
    }

    public void setCategoria_id(Integer categoria_id) {
        this.categoria_id = categoria_id;
    }

    public String getNombre_categoria() {
        return nombre_categoria;
    }

    public void setNombre_categoria(String nombre_categoria) {
        this.nombre_categoria = nombre_categoria;
    }

    public String getDescripcion_categoria() {
        return descripcion_categoria;
    }

    public void setDescripcion_categoria(String descripcion_categoria) {
        this.descripcion_categoria = descripcion_categoria;
    }
}
