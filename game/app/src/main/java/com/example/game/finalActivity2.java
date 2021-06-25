package com.example.game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class finalActivity2 extends AppCompatActivity {
    private Button startAgain;
    private TextView s1;
    private String scoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final2);
        startAgain=(Button)findViewById(R.id.button);
        s1=(TextView)findViewById(R.id.s1);
        scoreText=getIntent().getExtras().get("score").toString();
        startAgain.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent mainIntent=new Intent(finalActivity2.this,MainActivity.class);
              startActivity(mainIntent);
          }
      });
        s1.setText("score" +scoreText);
    }
}