
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
import com.example.cholo.customerMapActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class travelerloginActivity extends AppCompatActivity {
    private Button customerLoginBtn;
    private  Button customerRegisterBtn;
    private TextView customerRegisterLink;
    private TextView customerStatus;
    private EditText emailCustomer,passwordCustomer;
    private FirebaseAuth mAuth;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travelerlogin);

        customerLoginBtn=(Button)findViewById(R.id.login_tra_btn);
        customerRegisterBtn=(Button)findViewById(R.id.register_tra_btn);
        customerRegisterLink=(TextView) findViewById(R.id.register_cust_link);
        customerStatus=(TextView)findViewById(R.id.customer_status);
        emailCustomer=(EditText)findViewById(R.id.emailTra);
        passwordCustomer=(EditText)findViewById(R.id.passwordTra);
        mAuth=FirebaseAuth.getInstance();
        loading=new ProgressDialog(this);

        customerRegisterBtn.setVisibility(View.INVISIBLE);
        customerRegisterBtn.setEnabled(false);

        customerRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerLoginBtn.setVisibility(View.INVISIBLE);
                customerRegisterLink.setVisibility(View.INVISIBLE);
                customerStatus.setText("Register Customer");
                customerRegisterBtn.setVisibility(View.VISIBLE);
                customerRegisterBtn.setEnabled(true);
            }
        });
        customerRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailCustomer.getText().toString();
                String password=passwordCustomer.getText().toString();

                RegisterCustomer(email,password);
            }
        });

        customerLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailCustomer.getText().toString();
                String password=passwordCustomer.getText().toString();

                SignInCustomer(email,password);
            }
        });
    }

    private void SignInCustomer(String email, String password) {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(travelerloginActivity.this, "enter your Email..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(travelerloginActivity.this, "enter your password..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loading.setTitle("customer login");
            loading.setMessage("please wait");
            loading.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(travelerloginActivity.this, "login successfull", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        String user_id=mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("User").child("Customers").child(user_id);
                        current_user_db.setValue(true);
                        Intent driverIntent=new Intent(travelerloginActivity.this,customerMapActivity.class);
                        startActivity(driverIntent);
                    }
                    else
                    {
                        Toast.makeText(travelerloginActivity.this, "login unsucessfull", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }

                }
            });
        }
    }

    private void RegisterCustomer(String email, String password) {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(travelerloginActivity.this, "enter your Email..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(travelerloginActivity.this, "enter your password..", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(travelerloginActivity.this, "registration successfull", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        String user_id=mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("User").child("Customers").child(user_id);
                        current_user_db.setValue(true);
                        Intent driverIntent=new Intent(travelerloginActivity.this, customerMapActivity.class);
                        startActivity(driverIntent);
                    }
                    else
                    {
                        Toast.makeText(travelerloginActivity.this, "registration unsucessfull", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }

                }
            });
        }

    }
}