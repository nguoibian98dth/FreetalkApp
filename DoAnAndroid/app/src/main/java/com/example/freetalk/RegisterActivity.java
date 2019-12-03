package com.example.freetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    //views
    private EditText mReg_emailET, mReg_nameET, mReg_passwordET;
    private Button mRegBtn;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //
    private FirebaseAuth mFirebaseAuth;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Actionbar and its title
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Tạo tài khoản");
        actionBar.hide();
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        //init
        mReg_emailET = findViewById(R.id.reg_email);
        mReg_nameET = findViewById(R.id.reg_display_name);
        mReg_passwordET = findViewById(R.id.reg_password);
        mRegBtn = (Button) findViewById(R.id.reg_create_btn);
        //Firebase Instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tạo tài khoản ĐỢI TÍ");


        //handle regiter btn click
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //INPUT EMAIL, PASS, NAME
                String email = mReg_emailET.getText().toString().trim();
                String displayName = mReg_nameET.getText().toString().trim();
                String password = mReg_passwordET.getText().toString().trim();
                //validate
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    //set error, focus email
                    mReg_emailET.setError("Email không hợp lệ");
                    mReg_emailET.setFocusable(true);
                }
                else if(password.length() < 6 )
                {
                    //set error password
                    mReg_passwordET.setError("Mật khẩu hiện tại dưới 6 kí tự");
                    mReg_passwordET.setFocusable(true);
                }
                else
                {
                    registerUser(email,password,displayName);
                }
            }
        });
    }

    private void registerUser(String email, String password, String displayName) {
        if (!email.isEmpty() && !password.isEmpty() && !displayName.isEmpty()) {
            progressDialog.show();
            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success,dimiss dialog and start Register Actitivy
                                progressDialog.dismiss();
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();

                                String email = user.getEmail();
                                String uid = user.getUid();
                                String name = mReg_nameET.getText().toString().trim();

                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", name);
                                hashMap.put("onlineStatus", "online");
                                hashMap.put("typingTo", "noOne");
                                hashMap.put("phone", "[Empty]");
                                hashMap.put("image", ""); //avatar
                                hashMap.put("cover", ""); //anh nền
                                //
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                //đặt 1 store user data với tên Users
                                DatabaseReference reference = database.getReference("Users");
                                //put data sd hashmap in db
                                reference.child(uid).setValue(hashMap);

                                Toast.makeText(RegisterActivity.this, "Đang tạo tài khoản..\n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                                finish();
                            } else {
                                progressDialog.dismiss();
                                // If sign in fails, display a message to the user.
                                Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Toast.makeText(RegisterActivity.this,"Vui lòng nhập đủ thông tin",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go previous activity
        return super.onSupportNavigateUp();
    }
}
