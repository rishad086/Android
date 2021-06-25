package com.example.cholo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoActivity extends AppCompatActivity {
    private String getType;
    private CircleImageView profileImageView;
    private EditText nameEditText,phoneEdittext,driverCarName;
    private Button mConfirm,mback;
    private TextView profilechanebtn;
    private  DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String cheaker="";
    private Uri imageUri;
    private String myUrl="";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicsRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        getType =getIntent().getStringExtra("type");
        Toast.makeText(this,getType,Toast.LENGTH_SHORT).show();
        mAuth=FirebaseAuth.getInstance();
        databaseReference=FirebaseDatabase.getInstance().getReference().child("User").child(getType);
        storageProfilePicsRef=FirebaseStorage.getInstance().getReference().child("profile pictures");
        profileImageView = (CircleImageView) findViewById(R.id.profileImage);


        nameEditText = (EditText) findViewById(R.id.name);
        phoneEdittext = (EditText) findViewById(R.id.phnnum);
        driverCarName = (EditText) findViewById(R.id.car);
        mConfirm = (Button) findViewById(R.id.confirm);
        mback = (Button) findViewById(R.id.back);
        profilechanebtn = (TextView) findViewById(R.id.change_pic_btn);
        if(getType.equals("Drivers"))
        {
            driverCarName.setVisibility(View.VISIBLE);
        }

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cheaker.equals("clicked"))
                {
                    validateControllers();
                }
                else
                {
                    validateAndSaveOnleInformation();
                }

            }
        });
        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getType.equals("Drivers"))
                {
                    startActivity(new Intent(InfoActivity.this,driverMapActivity.class));
                }
                else
                {
                    startActivity(new Intent(InfoActivity.this,customerMapActivity.class));
                }
            }
        });
        profilechanebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cheaker="clicked";
                CropImage.activity().setAspectRatio(1,1).start(InfoActivity.this);
            }
        });
        getUserInformation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK && data!=null)
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            imageUri=result.getUri();

            profileImageView.setImageURI(imageUri);
        }
        else
        {
            if(getType.equals("Drivers"))
            {
                startActivity(new Intent(InfoActivity.this,driverMapActivity.class));
            }
            else {
                startActivity(new Intent(InfoActivity.this,customerMapActivity.class));
            }

            Toast.makeText(this,"Error,Try Again",Toast.LENGTH_SHORT).show();
        }
    }
    private void validateControllers()
    {
        if(TextUtils.isEmpty(nameEditText.getText().toString()))
        {
            Toast.makeText(this,"provide your name",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phoneEdittext.getText().toString()))
        {
            Toast.makeText(this,"provide your number",Toast.LENGTH_SHORT).show();
        }
        else if(getType.equals("Drivers") && TextUtils.isEmpty(driverCarName.getText().toString()))
        {
            Toast.makeText(this,"provide your car name",Toast.LENGTH_SHORT).show();
        }
        else if (cheaker.equals("clicked"))
        {
            uploadProfilePicture();
        }

    }

    private void uploadProfilePicture() {
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Information Storing");
        progressDialog.setMessage("please wait");
        progressDialog.show();

        if(imageUri!=null)
        {
            final StorageReference fileRef=storageProfilePicsRef.child(mAuth.getCurrentUser().getUid()+".jpg");
            uploadTask=fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        Uri downloadUrl=task.getResult();
                        myUrl=downloadUrl.toString();

                        HashMap<String,Object> userMap=new HashMap<>();
                        userMap.put("uid",mAuth.getCurrentUser().getUid());
                        userMap.put("name",nameEditText.getText().toString());
                        userMap.put("phone",phoneEdittext.getText().toString());
                        userMap.put("image",myUrl);

                        if(getType.equals("Drivers"))
                        {
                            userMap.put("car",driverCarName.getText().toString());
                        }
                        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                        progressDialog.dismiss();
                        if(getType.equals("Drivers"))
                        {
                            startActivity(new Intent(InfoActivity.this,driverMapActivity.class));
                        }
                        else
                        {
                            startActivity(new Intent(InfoActivity.this,customerMapActivity.class));

                        }

                    }
                }
            });
        }
        else
        {
            Toast.makeText(this,"Image is not selected",Toast.LENGTH_SHORT).show();
        }
    }
    private void validateAndSaveOnleInformation()
    {
        if(TextUtils.isEmpty(nameEditText.getText().toString()))
        {
            Toast.makeText(this,"provide your name",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phoneEdittext.getText().toString()))
        {
            Toast.makeText(this,"provide your number",Toast.LENGTH_SHORT).show();
        }
        else if(getType.equals("Drivers") && TextUtils.isEmpty(driverCarName.getText().toString()))
        {
            Toast.makeText(this,"provide your car name",Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String,Object> userMap=new HashMap<>();
            userMap.put("uid",mAuth.getCurrentUser().getUid());
            userMap.put("name",nameEditText.getText().toString());
            userMap.put("phone",phoneEdittext.getText().toString());


            if(getType.equals("Drivers"))
            {
                userMap.put("car",driverCarName.getText().toString());
            }
            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);

            if(getType.equals("Drivers"))
            {
                startActivity(new Intent(InfoActivity.this,driverMapActivity.class));
            }
            else
            {
                startActivity(new Intent(InfoActivity.this,customerMapActivity.class));

            }
        }



    }

    private void getUserInformation()
    {
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0)
                {
                    String name=snapshot.child("name").getValue().toString();
                    String phone=snapshot.child("phone").getValue().toString();
                    nameEditText.setText(name);
                    phoneEdittext.setText(phone);

                    if(getType.equals("Drivers"))
                    {
                        String car=snapshot.child("car").getValue().toString();
                        driverCarName.setText(car);
                    }

                    if(snapshot.hasChild("image"))
                    {
                        String image=snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
