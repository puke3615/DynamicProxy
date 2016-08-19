package com.puke.dynamicproxy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.puke.net.User;
import com.puke.net.proxy.ApiGenerator;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginApi loginApi = ApiGenerator.generateApi(LoginApi.class);
                User user = loginApi.login("123", "456");
                Toast.makeText(MainActivity.this, user.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
