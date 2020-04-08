package com.example.oscarsanzana.todoenlineav2.entidades;

public class Producto {
    private Integer id,precio_producto, stock_producto;
    private String sku, nombre_producto;
    private String fecha_vencimiento;
    private char dietetico;
    private Categoria categoria_id;
    private Marca marca_id;
    private String rutaImagen;
    private String status;

    public Producto() {
    }

    public Producto(Integer id, Integer precio_producto, Integer stock_producto, String sku, String nombre_producto, String fecha_vencimiento, char dietetico, Categoria categoria_id, Marca marca_id,String rutaImagen, String status) {
        this.id = id;
        this.precio_producto = precio_producto;
        this.stock_producto = stock_producto;
        this.sku = sku;
        this.nombre_producto = nombre_producto;
        this.fecha_vencimiento = fecha_vencimiento;
        this.dietetico = dietetico;
        this.categoria_id = categoria_id;
        this.marca_id = marca_id;
        this.rutaImagen = rutaImagen;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPrecio_producto() {
        return precio_producto;
    }

    public void setPrecio_producto(Integer precio_producto) {
        this.precio_producto = precio_producto;
    }

    public Integer getStock_producto() {
        return stock_producto;
    }

    public void setStock_producto(Integer stock_producto) {
        this.stock_producto = stock_producto;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre_producto() {
        return nombre_producto;
    }

    public void setNombre_producto(String nombre_producto) {
        this.nombre_producto = nombre_producto;
    }

    public String getFecha_vencimiento() {
        return fecha_vencimiento;
    }

    public void setFecha_vencimiento(String fecha_vencimiento) {
        this.fecha_vencimiento = fecha_vencimiento;
    }

    public char getDietetico() {
        return dietetico;
    }

    public void setDietetico(char dietetico) {
        this.dietetico = dietetico;
    }

    public Categoria getCategoria_id() {
        return categoria_id;
    }

    public void setCategoria_id(Categoria categoria_id) {
        this.categoria_id = categoria_id;
    }

    public Marca getMarca_id() {
        return marca_id;
    }

    public void setMarca_id(Marca marca_id) {
        this.marca_id = marca_id;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
