package com.example.oscarsanzana.todoenlineav2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.oscarsanzana.todoenlineav2.entidades.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegistroActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,Response.Listener<JSONObject>, Response.ErrorListener{
    private String sexoSeleccionado;
    private Spinner spRol;
    private Usuario usuario;

    private EditText txRegistroUsuario;
    private EditText txRegistroPass;
    private EditText txDv;
    private EditText txReRegistroPass;
    private EditText txRegistroRut;
    private EditText txNombreCompleto;
    private Button btnRegistrar;

    private String rol;

    //Componente de Progreso
    private ProgressDialog progressDialog;

    private RequestQueue request;
    private JsonObjectRequest jsonObjectRequest;

    //Segunda forma
    StringRequest stringRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        usuario = new Usuario();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        RadioGroup radioGroup = findViewById(R.id.rgSexo);

        txRegistroUsuario = findViewById(R.id.txRegistroUsuario);
        txRegistroPass = findViewById(R.id.txRegistroPass);
        txReRegistroPass = findViewById(R.id.txReRegistroPass);
        txRegistroRut = findViewById(R.id.txRegistroRut);
        txDv = findViewById(R.id.txDv);
        txNombreCompleto = findViewById(R.id.txNombreCompleto);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        radioGroup.setOnCheckedChangeListener(this);

        spRol = findViewById(R.id.spRol);

        List listaRol = new ArrayList<>();
        listaRol.add("Gerente");
        listaRol.add("Reponedor");
        listaRol.add("Cajero");
        listaRol.add("Vendedor");

        ArrayAdapter arrayAd = new ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,listaRol);
        arrayAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spRol.setAdapter(arrayAd);

        spRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rol = spRol.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        request = Volley.newRequestQueue(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.rbMasculino){
            sexoSeleccionado = "Masculino";
        }

        if(checkedId == R.id.rbFemenino){
            sexoSeleccionado = "Femenino";
        }
    }

    private void registrarUsuariosSql(Usuario usuario){

        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(this,"bd_usuarios",null,1);
        try (SQLiteDatabase db = conn.getWritableDatabase()) {
            //"CREATE TABLE usuarios(id INTEGER PRIMARY KEY AUTOINCREMENT,nombre VARCHAR(100),password VARCHAR(100),localidad VARCHAR(100),sexo VARCHAR(100),rut VARCHAR(100))";
            String insert = "INSERT INTO usuarios(nombre,password,rol,sexo,rut) VALUES ('" + usuario.getUsuario() + "','" + usuario.getPassword() + "','" + usuario.getRol() + "','" + usuario.getSexo() + "','" + usuario.getRut() + "')";
            db.execSQL(insert);
            Toast.makeText(this, "Usuario creado", Toast.LENGTH_SHORT).show();
            db.close();
        } catch (Exception ex) {
            conn.getWritableDatabase().close();
        }
    }

    public boolean validarUsuarios(String user){
        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(this,"bd_usuarios",null,1);
        SQLiteDatabase db = conn.getWritableDatabase();
        try{

            String[] parametros = {user};
            String[] campos = {"id"};

            Cursor cursor = db.query("usuarios",campos,"nombre=?",parametros,null,null,null);

            return cursor.moveToFirst();

        }
        catch (Exception ex){
            conn.close();
            return false;
        }
        finally {
            conn.close();
        }
    }

    public void registrarUsuario(View v){
        String pass = txRegistroPass.getText().toString();
        String rePass = txReRegistroPass.getText().toString();
        String rut = txRegistroRut.getText().toString();
        String dv = txDv.getText().toString();
        String nombre = txRegistroUsuario.getText().toString();
        String nombreCompleto = txNombreCompleto.getText().toString();
        if (pass.length() == 0){
            Toast.makeText(this,"El campo contraseña no puede ir vacio",Toast.LENGTH_SHORT).show();
            return;
        } else if (pass.length() < 5){
            Toast.makeText(this,"Contraseña debe ser minimo 6 caracteres",Toast.LENGTH_SHORT).show();
            return;
        }
        if (rePass.length() == 0){
            Toast.makeText(this,"El campo Repita contraseña no puede ir vacio",Toast.LENGTH_SHORT).show();
            return;
        } else if (!pass.equals(rePass)){
            Toast.makeText(this,"Las contraseñas no coinsiden",Toast.LENGTH_SHORT).show();
            return;
        }
        if (nombre.length() == 0){
            Toast.makeText(this,"El campo nombre de usuario no debe ir vacio",Toast.LENGTH_SHORT).show();
            return;
        } else if (nombre.length() >= 25){
            Toast.makeText(this,"Nombre de usuario no debe ser mayor a 25 caracteres",Toast.LENGTH_SHORT).show();
            return;
        }
        if (dv.length() == 0) {
            Toast.makeText(this,"El campo DV no debe ir vacio",Toast.LENGTH_SHORT).show();
            return;
        } else if (dv.length() > 1) {
            Toast.makeText(this,"El campo DV no debe superar los 1 digitos",Toast.LENGTH_SHORT).show();
            return;
        }else if (!dv.matches("[k0-9]")) {
            Toast.makeText(this,"El campo DV debe contener valores del 0 al 9 o k",Toast.LENGTH_SHORT).show();
            return;
        }
        if (rut.length() == 0) {
            Toast.makeText(this,"El campo RUT no debe ir vacio",Toast.LENGTH_SHORT).show();
            return;
        } else if (rut.length() > 8){
            Toast.makeText(this,"El campo RUT no debe superar los 8 digitos",Toast.LENGTH_SHORT).show();
            return;
        }
        if (nombreCompleto.length() == 0){
            Toast.makeText(this,"El campo nombre de usuario no debe ir vacio",Toast.LENGTH_SHORT).show();
            return;
        } else if (nombreCompleto.length() >= 100){
            Toast.makeText(this,"Nombre de usuario no debe ser mayor a 100 caracteres",Toast.LENGTH_SHORT).show();
            return;
        }
        registrarUsuarioWs();
    }

    private void registrarUsuarioWs() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();

        String rutCompleto = txRegistroRut.getText().toString()+"-"+txDv.getText().toString();

        String ip=getString(R.string.ip);
        String url=ip+"/wsTodoEnLineaUsuarioInsertar.php?nombreUser="+txRegistroUsuario.getText().toString()+"&password="+txRegistroPass.getText().toString()
                +"&nombreCompleto="+txNombreCompleto.getText().toString()+"&rol="+rol+"&rut="+rutCompleto+"&sexo="+sexoSeleccionado;
        url = url.replace(" ", "%20");

        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this,"Error: "+ error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        Toast.makeText(RegistroActivity.this,"Usuario ingresado correctamente",Toast.LENGTH_SHORT).show();
        progressDialog.hide();
        Intent intent = new Intent(RegistroActivity.this,LoginActivity.class);
        startActivity(intent);

    }
}
