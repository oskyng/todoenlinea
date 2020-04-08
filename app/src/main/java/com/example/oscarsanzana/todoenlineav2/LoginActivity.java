package com.example.oscarsanzana.todoenlineav2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.oscarsanzana.todoenlineav2.fragmentos.MenuActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements Response.Listener<JSONObject>, Response.ErrorListener, GoogleApiClient.OnConnectionFailedListener {
    private EditText txUsuario;
    private EditText txPassword;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private GoogleApiClient googleApiClient;
    private SignInButton btnGoogle;

    CallbackManager callbackManager;
    LoginButton loginButton;

    //Componente de Progreso
    private ProgressDialog progressDialog;

    private RequestQueue request;
    private JsonObjectRequest jsonObjectRequest;

    //Segunda forma
    StringRequest stringRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txUsuario = findViewById(R.id.txUser);
        txPassword = findViewById(R.id.txPass);
        btnGoogle = findViewById(R.id.btnGoogle);

        callbackManager = CallbackManager.Factory.create();


        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");



        //SharedPreferenc
        preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        setCredencialesIfExist();

        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //vincula ciclo de vida del google con el activity
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(LoginActivity.this, LoginActivity.this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso )
                .build();

        //segundo parametro de opciones
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,777);
            }
        });

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Profile profile = Profile.getCurrentProfile();
                /*profilePictureView.setProfileId(profile.getId());*/
               try{
                   String id = profile.getId();
                   String name = profile.getFirstName()+" "+profile.getLastName();

                   remenberUserFacebook(name,id);
               } catch (Exception e){

               }
                Intent intent = new Intent(LoginActivity.this,MenuActivity.class);
                startActivity(intent);

            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this,"Cancel",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(FacebookException exception) {
                Log.i("ERRORF",exception.toString());
                Toast.makeText(LoginActivity.this,"Error: "+exception,Toast.LENGTH_SHORT).show();

            }
        });

        request = Volley.newRequestQueue(this);
    }

    private void setCredencialesIfExist(){
        String user = preferences.getString("usuario","");
        String pass = preferences.getString("contraseña", "");
        String nombre = preferences.getString("nombre","");
        String rol = preferences.getString("rol", "");

        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(nombre) && !TextUtils.isEmpty(rol)){
            txUsuario.setText(user);
            txPassword.setText(pass);
        }
    }

    private void remenberUser(String user, String pass, String nombre, String rol){
        editor = preferences.edit();
        editor.putString("usuario",user);
        editor.putString("contraseña",pass);
        editor.putString("nombreCompleto",nombre);
        editor.putString("rol",rol);
        editor.apply();
    }

    private void remenberUserGoogle(String name, String email, String id, String url){
        editor = preferences.edit();
        editor.putString("nombreGoogle",name);
        editor.putString("email",email);
        editor.putString("id",id);
        editor.putString("url",url);
        editor.apply();
    }

    private void remenberUserFacebook(String name, String id){
        editor = preferences.edit();
        editor.putString("nombreFacebook",name);
        editor.putString("id",id);
        editor.apply();
    }

    private boolean validarIngreso(String usuario, String password) {

        ConexionSqLiteHelper conn = new ConexionSqLiteHelper(this,"bd_usuarios",null,1);
        SQLiteDatabase db = conn.getReadableDatabase();

        try{

            String[] parametros = {usuario,password};
            String[] campos = {"id"};

            Cursor cursor = db.query("usuarios",campos,"nombre=? AND password = ?",parametros,null,null,null);

            cursor.moveToFirst();
            cursor.close();
            conn.close();
            return true;
        }
        catch (Exception ex){
            conn.close();
            return false;
        }
        finally {
            conn.close();
        }
    }

    public void irRegistro(View v){
        Intent intent = new Intent(this,RegistroActivity.class);
        startActivity(intent);
    }

    public void login(View v){
        ConnectivityManager con = (ConnectivityManager) Objects.requireNonNull(this).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = con.getActiveNetworkInfo();
        if (info != null && info.isConnected()){
            loginWs();
        } else {
            Toast.makeText(LoginActivity.this,"No hay conexion a internet, intentelo mas tarde",Toast.LENGTH_SHORT).show();
            return;
        }

    }

    private void loginWs() {
        try {
            String ip=getString(R.string.ip);
            String url=ip+"/wsTodoEnLineaUsuarioLogin.php?nombre="+txUsuario.getText().toString()+"&password="+txPassword.getText().toString();
            url = url.replace(" ", "%20");

            jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,this,this);
            request.add(jsonObjectRequest);
        } catch (Exception e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this,"No se ha podido conectar",Toast.LENGTH_SHORT).show();
        progressDialog.hide();
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            JSONArray json = response.optJSONArray("usuario");
            JSONObject jsonObject;
            jsonObject = json.getJSONObject(0);

            if (Integer.parseInt(jsonObject.optString("id")) != 0){
                String nombre = jsonObject.optString("nombreCompleto");
                String rol = jsonObject.optString("rol");
                remenberUser(txUsuario.getText().toString(), txPassword.getText().toString(),nombre,rol);
                Intent intent = new Intent(this,MenuActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this,"Usuario no registrado",Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 777){

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);

            GoogleSignInAccount account = result.getSignInAccount();
            Intent intent = new Intent(this,MenuActivity.class);
            startActivity(intent);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if(result.isSuccess()){
            //ir nueva
            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getDisplayName();
            String email = account.getEmail();
            String id = account.getId();
            String url = account.getPhotoUrl().toString();

            remenberUserGoogle(name,email,id,url);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
