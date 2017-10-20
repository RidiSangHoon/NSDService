package com.example.leesanghoon.nsdservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button clientBtn, serverBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        clientBtn = (Button)findViewById(R.id.client_btn);
        serverBtn = (Button)findViewById(R.id.server_btn);

        clientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ClientActivity.class);
                startActivity(intent);
            }
        });

        serverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ServerActivity.class);
                startActivity(intent);
            }
        });
    }
}
