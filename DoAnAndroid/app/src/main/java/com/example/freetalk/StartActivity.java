package com.example.freetalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

public class StartActivity extends AppCompatActivity {

    private Button mLoginBtn;
    private TextView noHaveAcc;

    //
    private CallbackManager callbackManager;
    private LoginButton mFacebookBtn;
    private ProgressDialog mFacebookProgress;
    private String TAG = "";
    //

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    //int CAMERA_PIC_REQUEST =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getSupportActionBar().hide();

        //init views

        noHaveAcc = findViewById(R.id.noHaveAcc) ;
        mLoginBtn= (Button) findViewById(R.id.login_btn);

        mFacebookProgress = new ProgressDialog(this);


        callbackManager = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);

        mFacebookBtn = (LoginButton) findViewById(R.id.loginfb_btn);

        //handle mFacbookBtn
        mFacebookBtn.setReadPermissions(Arrays.asList("email", "public_profile"));
        mFacebookBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, loginResult.toString());
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, error.toString());
            }
        });

        mAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {
                }
            }
        };




        //handle login_btn
        noHaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this,LoginActivity.class));
            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token){

        Log.d(TAG, token.toString());
        mFacebookProgress.setTitle("Đợi tí!");
        mFacebookProgress.setMessage("Đang đăng nhập...");
        mFacebookProgress.setCanceledOnTouchOutside(false);
        mFacebookProgress.show();
        AuthCredential authCredential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("Thành công: ", task.isSuccessful() + "");

                if (!task.isSuccessful()) {
                    Log.d("Thất bại: ", task.getException() + "");
                } else {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();
                    String display_name = current_user.getDisplayName();
                    String email = current_user.getEmail();
                    String phone = current_user.getPhoneNumber();
                    String image = current_user.getPhotoUrl().toString();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    //String device_token = FirebaseInstanceId.getInstance().getToken();

                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", uid);
                    userMap.put("name", display_name);
                    userMap.put("email",email);
                    userMap.put("phone", "[Empty]");
                    userMap.put("image", image);
                    userMap.put("cover", "");
                    userMap.put("onlineStatus", "online");
                    userMap.put("typingTo", "noOne");

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent mainIntent = new Intent(StartActivity.this, DashboardActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }


}
