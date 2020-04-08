package com.example.oscarsanzana.todoenlineav2.fragmentos;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.oscarsanzana.todoenlineav2.ConexionSqLiteHelper;
import com.example.oscarsanzana.todoenlineav2.LoginActivity;
import com.example.oscarsanzana.todoenlineav2.MapsActivity;
import com.example.oscarsanzana.todoenlineav2.R;
import com.example.oscarsanzana.todoenlineav2.entidades.Categoria;
import com.example.oscarsanzana.todoenlineav2.entidades.Marca;
import com.example.oscarsanzana.todoenlineav2.entidades.Producto;
import com.example.oscarsanzana.todoenlineav2.entidades.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AgregarProductoFragment.OnFragmentInteractionListener, ListarProductoFragment.OnFragmentInteractionListener, AgregarCategoriaFragment.OnFragmentInteractionListener, AgregarMarcaFragment.OnFragmentInteractionListener, BuscarProductoFragment.OnFragmentInteractionListener {
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    private RequestQueue request;
    private JsonObjectRequest jsonObjectRequest;
    StringRequest stringRequest;
    private final String ip = "https://oscarsanzana97.000webhostapp.com";
    Producto producto;
    Marca marca;
    Categoria categoria;
    ArrayList<Producto> listaProducto = new ArrayList<>();
    ArrayList<Marca> listaMarca = new ArrayList<>();
    ArrayList<Categoria> listaCategoria = new ArrayList<>();
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fragment miFragment = new ListarProductoFragment();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        View hView = navigationView.getHeaderView(0);
        TextView tvNombre = hView.findViewById(R.id.tvNombre);
        TextView tvCorreo = hView.findViewById(R.id.tvCorreo);
        ImageView idImagen = hView.findViewById(R.id.idImagen);

        Bundle bundle=getIntent().getExtras();

        String user = preferences.getString("usuario","");
        String pass = preferences.getString("contraseña", "");
        String nombre = preferences.getString("nombreCompleto","");
        String rol = preferences.getString("rol", "");

        String url = preferences.getString("url","");
        String nombreGoogle = preferences.getString("nombreGoogle","");
        String email = preferences.getString("email","");


        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(nombre) && !TextUtils.isEmpty(rol)){
            tvNombre.setText(nombre);
            tvCorreo.setText(rol);

        } else if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(nombreGoogle) && !TextUtils.isEmpty(email)) {
            //cargarImagen
            Glide.with(this).load(url).into(idImagen);
            tvNombre.setText(nombreGoogle);
            tvCorreo.setText(email);
        }

        navigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().add(R.id.contenedor_fragment,miFragment).commit();
    }

    boolean twice;
    @Override
    public void onBackPressed() {

        if(twice==true){
            Intent intent= new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
        }

        Toast.makeText(MenuActivity.this,"presione retroceso para salir", Toast.LENGTH_SHORT).show();


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    twice= false;
                }
            },3000);
            twice=true;
//           super.onBackPressed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            logOut();
            removedSharedPreference();
        }else if(id==R.id.action_sincronizar){
            if (isNetDisponible() && isOnlineNet()){
                EnviarDatos();
            } else {
                Toast.makeText(this,"No hay conexion a internet",Toast.LENGTH_SHORT).show();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public void EnviarDatos() {
        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(this,"bd_productos",null,1);
        SQLiteDatabase db = conn.getReadableDatabase();
        Cursor cursor = db.query("productos", new String[]{"sku", "nombre_producto", "precio_producto", "stock_producto","fecha_vencimiento","dietetico","categoria_id","marca_id","imagen_producto","status","id"}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("sku", cursor.getString(0));
                parameters.put("nombre_producto", cursor.getString(1));
                parameters.put("precio_producto", cursor.getString(2));
                parameters.put("stock_producto", cursor.getString(3));
                parameters.put("fecha_vencimiento", cursor.getString(4));
                parameters.put("dietetico",cursor.getString(5));
                parameters.put("categoria_id",cursor.getString(6));
                parameters.put("marca_id",cursor.getString(7));
                parameters.put("imagen_producto",cursor.getString(8));
                parameters.put("status",cursor.getString(9));
                ProcessRequest(parameters);
                //Eliminamos la fila ya enviada.
                String[] parametros = {cursor.getString(10)};
                int consulta = db.delete("productos","id=?",parametros);
            } while (cursor.moveToNext());
        } else {
            Toast.makeText(MenuActivity.this,"No hay datos que sincronizar",Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void ProcessRequest(final Map<String, String> parameters) {
        String url = ip+"/wsTodoEnLineaProductosInsertar.php?";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.trim().equalsIgnoreCase("registra")){
                    Toast.makeText(MenuActivity.this,"Se ha registrado con exito",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MenuActivity.this,"No se ha registrado ",Toast.LENGTH_SHORT).show();
                    Log.i("RESPUESTA: ",""+response);
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MenuActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return parameters;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment miFragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            miFragment = new ListarProductoFragment();
            transaction.replace(R.id.contenedor_fragment,miFragment).commit();
        } else if (id == R.id.nav_gallery) {
            miFragment = new AgregarProductoFragment();
            transaction.replace(R.id.contenedor_fragment,miFragment).commit();
        } else if (id == R.id.nav_slideshow) {
            miFragment = new BuscarProductoFragment();
            transaction.replace(R.id.contenedor_fragment,miFragment).commit();
        } else if (id == R.id.nav_manage) {
            miFragment = new AgregarMarcaFragment();
            transaction.replace(R.id.contenedor_fragment,miFragment).commit();
        } else if (id == R.id.nav_maps) {
            Intent intent = new Intent(MenuActivity.this,MapsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void logOut(){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void removedSharedPreference(){
        preferences.edit().clear().apply();
    }

    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }

    public Boolean isOnlineNet() {
        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.cl");

            int val = p.waitFor();
            return (val == 0);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
