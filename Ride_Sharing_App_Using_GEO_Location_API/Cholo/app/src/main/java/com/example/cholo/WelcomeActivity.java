package com.example.cholo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity { private Button w_d_b;
    private Button w_t_b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        w_d_b=(Button)findViewById(R.id.W_D);
        w_t_b=(Button)findViewById(R.id.W_T);

        w_t_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginRegistertravelerIntent=new Intent(WelcomeActivity.this, travelerloginActivity.class);
                startActivity(loginRegistertravelerIntent);
            }
        });
        w_d_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginRegisterdriverIntent=new Intent(WelcomeActivity.this, driverLoginActivity.class);
                startActivity(loginRegisterdriverIntent);
            }
        });
    }
}