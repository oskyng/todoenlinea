package com.example.oscarsanzana.todoenlineav2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.oscarsanzana.todoenlineav2.fragmentos.MenuActivity;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //SharedPreferenc
                preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
                String user = preferences.getString("usuario","");
                String pass = preferences.getString("contraseña", "");
                String name = preferences.getString("nombreGoogle","");
                String email = preferences.getString("email","");
                if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)){
                    Intent intent = new Intent(SplashActivity.this,MenuActivity.class);
                    startActivity(intent);
                } else if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email)){
                    Intent intent = new Intent(SplashActivity.this,MenuActivity.class);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
                    startActivity(intent);
                }


            }

        },3000);
    }
}
