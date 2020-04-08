package com.example.oscarsanzana.todoenlineav2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ConexionSqLiteHelper extends SQLiteOpenHelper {
    private final String CREAR_TABLA_USUARIO = "CREATE TABLE usuarios(id INTEGER PRIMARY KEY AUTOINCREMENT,nombre VARCHAR(100),password VARCHAR(100),rol VARCHAR(100),sexo VARCHAR(100),rut VARCHAR(100))";

    private final String CREAR_TABLA_PRODUCTO = "CREATE TABLE productos(id INTEGER PRIMARY KEY AUTOINCREMENT, sku varchar(50) NOT NULL, nombre_producto varchar(25) NOT NULL, precio_producto INTEGER NOT NULL, " +
            "stock_producto INTEGER NOT NULL, fecha_vencimiento date NOT NULL, dietetico char NOT NULL, categoria_id INTEGER NOT NULL, marca_id INTEGER NOT NULL,imagen_producto VARCHAR(100), status VARCHAR(1) NOT NULL, " +
            "FOREIGN KEY (categoria_id) REFERENCES categorias (categoria_id), FOREIGN KEY (marca_id) REFERENCES marcas" +
            " (marca_id))";

    private final String CREAR_TABLA_CATEGORIA = "CREATE TABLE categorias(categoria_id INTEGER PRIMARY KEY NOT NULL, nombre_categoria varchar(20) NOT NULL, descripcion_categoria varchar(255) NOT NULL)";

    private final String CREAR_TABLA_MARCA = "CREATE TABLE marcas(marca_id INTEGER PRIMARY KEY AUTOINCREMENT,nombre_marca VARCHAR(100))";

    final String insert7 = "INSERT INTO productos(sku,nombre_producto,precio_producto,stock_producto,fecha_vencimiento,dietetico,categoria_id,marca_id,) " +
            "VALUES ('123qwe','cereal',2000,20,'20/08/2020','s',1,1)";

    public ConexionSqLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_USUARIO);
        db.execSQL(CREAR_TABLA_CATEGORIA);
        db.execSQL(CREAR_TABLA_MARCA);
        db.execSQL(CREAR_TABLA_PRODUCTO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS categorias");
        db.execSQL("DROP TABLE IF EXISTS marcas");
        db.execSQL("DROP TABLE IF EXISTS productos");
        onCreate(db);

    }
}
