package com.example.freetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.freetalk.models.ModelUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    String name,phone;
    String avatar;
    String cover;

    private Button mbtnLogin ;
    private TextView recoverPass;
    private EditText mEmailEt,mPasswordEt;

    private FirebaseAuth mAuth;

    private  DatabaseReference databaseReference;

    HashMap<Object, String> hashMap = new HashMap<>();

    //
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        //Actionbar and its title
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Đăng nhập");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //init
        mEmailEt = findViewById(R.id.login_email);
        mPasswordEt= findViewById(R.id.login_password);
        recoverPass= findViewById(R.id.forgotPasswordTV);
        mbtnLogin=findViewById(R.id.login_btn);
        //
        mAuth= FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        //



        //login click
        mbtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String email= mEmailEt.getText().toString();
                String pass= mPasswordEt.getText().toString();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    //set error, focus email
                    mEmailEt.setError("Email không tồn tại");
                    mEmailEt.setFocusable(true);
                }
                else
                {
                    loginUser(email,pass);


                }
            }
        });
        // not have acc tv click
        recoverPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowRecoverPasswordDialog();
            }
        });

        //init progress
        pd= new ProgressDialog(this);


    }


    private void ShowRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Khôi phục mật khẩu");
        //tạo mới một layout RecoverPass
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailET = new EditText(this);
        emailET.setHint("Email");
        //set kiểu email
        emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailET.setMinEms(16);
        linearLayout.addView(emailET);
        linearLayout.setPadding(10,10,20,10);

        builder.setView(linearLayout);
        //button  OK
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String email = emailET.getText().toString().trim();
                    //hàm tự đinh nghĩa khôi phục
                    if (email != "")
                    {
                        beginRecovery(email);
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,"Lỗi",Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception ex)
                {
                    Toast.makeText(LoginActivity.this,"Email trống", Toast.LENGTH_LONG).show();
                }
            }
        });

        //button cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        //SHOW
        try
        {
                pd.setMessage("Đang gửi email...");
                pd.show();
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Email đã gửi", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        }
            catch (Exception e)
            {
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
    }


    private void loginUser(String email, String pass) {
        //SHOW

        if (!pass.isEmpty()) {
            pd.setMessage("Đang đang nhập ĐỢI TÍ");
            pd.show();
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser() ;
                            String email = user.getEmail();
                            String uid = user.getUid();
//
//                                HashMap<Object, String> hashMap = new HashMap<>();
//                                hashMap.put("email", email);
//                                hashMap.put("uid", uid);
//                                hashMap.put("name","Test Dev");
//                                hashMap.put("onlineStatus", "online");
//                                hashMap.put("typingTo", "noOne");
//                                hashMap.put("phone", "[Empty]");
//                                hashMap.put("image", "https://firebasestorage.googleapis.com/v0/b/freetalk-69177.appspot.com/o/Users_Profile_Cover_Imgs%2Fimage_GICLvjpVTncQad6jxWQUVH1kKzv2?alt=media&token=9a38c780-f1f5-4bfd-ac47-aacb79bc954a"); //avatar
//                                hashMap.put("cover", "https://firebasestorage.googleapis.com/v0/b/freetalk-69177.appspot.com/o/Users_Profile_Cover_Imgs%2Fcover_GICLvjpVTncQad6jxWQUVH1kKzv2?alt=media&token=012addef-e36b-4f3d-a699-a2d07b112e87"); //anh nền
//                                //
//                                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                                //đặt 1 store user data với tên Users
//                                DatabaseReference reference = database.getReference("Users");
//                                //put data sd hashmap in db
//                                reference.child(uid).setValue(hashMap);

                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("onlineStatus", "online");
                                //cap nhap gia tri onlineStatus cua user hien tai
                                dbRef.updateChildren(hashMap);



                                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                finish();
                            } else {
                                pd.dismiss();
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //error
                    Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Toast.makeText(LoginActivity.this,"Vui lòng nhập đủ thông tin",Toast.LENGTH_SHORT).show();
        }
    }


    public boolean onSupportNavigateUp() {
        onBackPressed();//go previous activity
        return super.onSupportNavigateUp();
    }
}
