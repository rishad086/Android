
package com.example.cholo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cholo.R;
import com.example.cholo.driverMapActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class driverLoginActivity extends AppCompatActivity {
    private Button driverLoginBtn;
    private  Button driverRegisterBtn;
    private TextView driverRegisterLink;
    private TextView driverStatus;
    private EditText EmailDriver,PasswordDriver;
    private FirebaseAuth mAuth;
    private ProgressDialog loading;
    private FirebaseAuth.AuthStateListener  firebaseAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        mAuth=FirebaseAuth.getInstance();
        driverLoginBtn=(Button)findViewById(R.id.login_dri_btn);
        driverRegisterBtn=(Button)findViewById(R.id.register_dri_btn);
        driverRegisterLink=(TextView) findViewById(R.id.register_driver);
        driverStatus=(TextView)findViewById(R.id.driver_status);
        EmailDriver=(EditText)findViewById(R.id.emailDri);
        PasswordDriver=(EditText)findViewById(R.id.passwordDri);
        loading=new ProgressDialog(this);

        driverRegisterBtn.setVisibility(View.INVISIBLE);
        driverRegisterBtn.setEnabled(false);

        driverRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverLoginBtn.setVisibility(View.INVISIBLE);
                driverRegisterLink.setVisibility(View.INVISIBLE);
                driverStatus.setText("Register Driver");
                driverRegisterBtn.setVisibility(View.VISIBLE);
                driverRegisterBtn.setEnabled(true);
            }
        });
        driverRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailDriver.getText().toString();
                String password=PasswordDriver.getText().toString();

                RegisterDriver(email,password);
            }
        });
        driverLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailDriver.getText().toString();
                String password=PasswordDriver.getText().toString();

                SignInDriver(email,password);
            }
        });
    }

    private void SignInDriver(String email, String password) {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(driverLoginActivity.this, "enter your Email..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(driverLoginActivity.this, "enter your password..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loading.setTitle("Driver login");
            loading.setMessage("please wait");
            loading.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(driverLoginActivity.this, "login successfull", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        String user_id=mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("User").child("Drivers").child(user_id);
                        current_user_db.setValue(true);
                        Intent driverIntent=new Intent(driverLoginActivity.this,driverMapActivity.class);
                        startActivity(driverIntent);
                    }
                    else
                    {
                        Toast.makeText(driverLoginActivity.this, "login unsucessfull", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }

                }
            });
        }

    }

    private void RegisterDriver(String email, String password) {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(driverLoginActivity.this, "enter your Email..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(driverLoginActivity.this, "enter your password..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loading.setTitle("Registration is going on");
            loading.setMessage("please wait");
            loading.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(driverLoginActivity.this, "registration successfull", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        String user_id=mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("User").child("Drivers").child(user_id);
                        current_user_db.setValue(true);
                        Intent driverIntent=new Intent(driverLoginActivity.this, driverMapActivity.class);
                        startActivity(driverIntent);
                    }
                    else
                    {
                        Toast.makeText(driverLoginActivity.this, "registration unsucessfull", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }

                }
            });
        }

    }
}