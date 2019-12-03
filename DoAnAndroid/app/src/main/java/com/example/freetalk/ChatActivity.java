package com.example.freetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.freetalk.adapters.AdapterChat;
import com.example.freetalk.models.ModelChat;
import com.example.freetalk.models.ModelUser;
import com.example.freetalk.notification.Data;
import com.example.freetalk.notification.Sender;
import com.example.freetalk.notification.Token;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.HasApiKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIV;
    TextView nameTV, userStatusTV;
    EditText messageET;
    ImageButton sendButton , attachButton;
    String onlineStatus;
    String typingStatus;

    private RequestQueue requestQueue;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;
    String hisUid;
    String myUid;
    String hisImage;

    private boolean notify = false;

    //kiem tra da gui message chua
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    //permission constant
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200; //BỘ NHỚ
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int GALLERY_PICK = 1;

    //image picked will be samed in this uri
    Uri image_uri = null;

    //permission array
    String[] cameraPermissions;
    String [] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        //init
        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView= findViewById(R.id.recyclerView);
        profileIV = findViewById(R.id.profileiv);
        nameTV = findViewById(R.id.nametv);
        userStatusTV = findViewById(R.id.userStatusTV);
        messageET = findViewById(R.id.messageTV);
        sendButton = findViewById(R.id.sendbtn);
        attachButton = findViewById(R.id.attachBTN);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        //init array of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //use Linear Layout for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //hien thi tu duoi
        linearLayoutManager.setStackFromEnd(true);
        //recyclerView properties
        recyclerView.setHasFixedSize(true); //set recycle co kich thuoc co dinh
        recyclerView.setLayoutManager(linearLayoutManager);

        //create apiService
        //apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);


        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        firebaseAuth = FirebaseAuth.getInstance();

         firebaseDatabase = FirebaseDatabase.getInstance();
         userDbRef = firebaseDatabase.getReference("Users");

         //truy van lay info user khac
        Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
         //get nam and picture user
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //kiem tra cho den khi dc yeu cau received
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();

                    //get value of onlineStatus
                    onlineStatus = "" + ds.child("onlineStatus").getValue();
                    typingStatus = ""+ds.child("typingTo").getValue();

                    // set data

                    if(typingStatus.equals(hisUid))
                    {
                        //typing.setText("Đang soạn tin...");
                        userStatusTV.setText("Đang soạn tin nhắn...");
                    }
                    else
                    {
                        if (onlineStatus.equals("online")) {
                            userStatusTV.setText(onlineStatus);
                        } else {
                            //format timeStamp about dd/mm/yyyy h:m AM/PM
//                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
//                        cal.setTimeInMillis(Long.parseLong(onlineStatus));
//                        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
//                        userStattusTV.setText("Lần cuối lúc: " +dateTime);

                            //
                            GetTimeAgo getTimeAgo = new GetTimeAgo();

                            long lastTime = Long.parseLong(onlineStatus);

                            String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                            userStatusTV.setText(lastSeenTime);
                            //

                        }
                    }

                    nameTV.setText(name);


                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.default_avatar).into(profileIV);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.default_avatar).into(profileIV);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //su kien click btn send mess
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                //get text from edit text
                String message = messageET.getText().toString().trim();
                if(TextUtils.isEmpty(message))
                {
                    Toast.makeText(ChatActivity.this,"Nội dung gửi không được để trống", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    sendMessage(message);
                    messageET.setText("");
                }
            }
        });

        //handle image click import image
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePicDiaLog();
            }
        });

        //check text change
        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length() > 0)
                {

                    checkTypingStatus(hisUid);
                   // userStatusTV.setText("đang soạn tin....");
                }
                else
                {
                    checkTypingStatus("noOne");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //check edit text
        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }
                else{
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        readMessages();
        
        seenMessages();
        
        
        //enable back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
                finish();
            }
        });

        
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi ảnh...");
        progressDialog.show();

        final String timeStamp = ""+System.currentTimeMillis();

        String fileNameAndPath = "ChatImage/"+"post_"+timeStamp;

        //get bitmap from image_uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_uri);

        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data= baos.toByteArray();// convert image to bytes
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded
                        progressDialog.dismiss();
                        //get url of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();

                        if(uriTask.isSuccessful())
                        {
                            //add image uri and other info to database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            //setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender",myUid);
                            hashMap.put("receiver",hisUid);
                            hashMap.put("message",downloadUri);
                            hashMap.put("timestamp",timeStamp);
                            hashMap.put("isSeen",false);
                            hashMap.put("type","image");

                            databaseReference.child("Chats").push().setValue(hashMap);

                            //send notification
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                            database.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    ModelUser user = dataSnapshot.getValue(ModelUser.class);

                                    if(notify)
                                    {
                                        sendNotification(hisUid,user.getName(),"Đã gửi bạn 1 ảnh");
                                    }
                                    notify = false;

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                            //create Chatlists
                            final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(myUid)
                                    .child(hisUid);
                            chatRef1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists())
                                    {

                                        chatRef1.child("id").setValue(hisUid);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(hisUid)
                                    .child(myUid);
                            chatRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists())
                                    {

                                        chatRef2.child("id").setValue(myUid);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }

    private boolean checkStoragePermission()
    {

        boolean resuit = ContextCompat.checkSelfPermission(ChatActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return resuit;
    }

    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(ChatActivity.this,storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission()
    {
        boolean resuit = ContextCompat.checkSelfPermission(ChatActivity.this,Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean resuit1 = ContextCompat.checkSelfPermission(ChatActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_DENIED);
        return resuit && resuit1;
    }

    private void requestCameraPermission()
    {
        ActivityCompat.requestPermissions(ChatActivity.this,cameraPermissions, CAMERA_REQUEST_CODE);

    }


    private void showImagePicDiaLog()
    {

        String options[] = {"Máy ảnh", "Thư viện"};
        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        //pick image from
        builder.setTitle("Lấy ảnh từ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)// camera clicked
                {
                    try {
                        //xử lí
                        if (!checkCameraPermission()) //if return false
                        {
                            requestCameraPermission();
                        } else {
                            pickFromCamera();
                        }
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(ChatActivity.this,""+e.getMessage() ,Toast.LENGTH_SHORT).show();
                    }
                }
                else if(which == 1)// gallery clicked
                {
                    try {
                        //xử lí
                        if (!checkStoragePermission()) //if return false
                        {
                            requestStoragePermission();
                        } else {
                            pickFromGallery();
                        }
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(ChatActivity.this,""+e.getMessage() ,Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        builder.create().show();
    }

    private void pickFromCamera()
    {
        try
        {
            //intent of picking image from device camera
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
            //put image uri;
            image_uri = ChatActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            //intent start camera

            /*
                    Intent Intent3=new   Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    startActivity(Intent3);
            */
            //  Intent Intent3=new   Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);//ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
            startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
        }
        catch (Exception e)
        {
            Toast.makeText(ChatActivity.this,""+e.getMessage() ,Toast.LENGTH_SHORT).show();
        }
    }

    private void pickFromGallery()
    {
        try {
            //Pick from gallery
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

            //startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

        }
        catch (Exception e)
        {
            //Toast.makeText(getActivity(),""+e.getMessage() ,Toast.LENGTH_SHORT).show();
        }

    }

    private void seenMessages() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid))
                    {
                        HashMap<String, Object> hasSeenHasMap = new HashMap<>();
                        hasSeenHasMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHasMap);
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        //get all data from Chats on firebase
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid))
                    {
                        chatList.add(chat);
                    }

                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();

                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String message) {

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            String timestamp = String.valueOf(System.currentTimeMillis());

            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("sender", myUid);
            hashMap.put("receiver", hisUid);
            hashMap.put("message", message);
            hashMap.put("timestamp", timestamp);
            hashMap.put("isSeen", false);
            hashMap.put("type","text");

            databaseReference.child("Chats").push().setValue(hashMap);


            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
            database.child(myUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    ModelUser user = dataSnapshot.getValue(ModelUser.class);

                    if(notify)
                    {
                        sendNotification(hisUid,user.getName(),message);
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        //create Chatlists
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid)
                .child(hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                {

                    chatRef1.child("id").setValue(hisUid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid)
                .child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                {

                    chatRef2.child("id").setValue(myUid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(final String hisUid,final String name,final String message) {
            DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
            Query query = allTokens.orderByKey().equalTo(hisUid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Token token = ds.getValue(Token.class);
                        Data data = new Data(myUid, name + ": " + message, "Tin nhắn mới", hisUid, R.mipmap.ic_launcher);

                        Sender sender = new Sender(data, token.getToken());

                        //fcm json object request
                        try {
                            JSONObject senderJsonObj= new JSONObject(new Gson().toJson(sender));
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Log.d("Json  respone","onRespone"+response.toString());
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Json  respone","onRespone"+error.toString());
                                }
                            }){
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {

                                    //put param
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Content-Type","application/json");
                                    headers.put("Authorization","key=AAAAEoBDNZg:APA91bELUz6PHQ4rU8dkisLSrhfghy0Dlqe2ZTQJiiiytdBwQ7RktrA5qaordYjZS_3pU3oiefvSJWPYqECeevXM0xlWO3os8KHudDH6P4BCrbVS1BATW2zu2YrTUwHeJGn3NHLK5bb0");

                                    return headers;
                                }
                            };

                            requestQueue.add(jsonObjectRequest);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }


    private void checkUserStatus()    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user !=null)
        {
            //được đăng nhập tại đây
            myUid = user.getUid();
        }
        else
        {
            //đã đăng nhập mở trang main
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("onlineStatus", status);
        //cap nhap gia tri onlineStatus cua user hien tai
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        //cap nhap gia tri typingTo cua user hien tai
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();

        super.onStart();
        //set online
        checkOnlineStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkTypingStatus("noOne");
        //set offline voi thoi gian roi khoi
        checkOnlineStatus("online");
        checkTypingStatus("noOne");

        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        //set offline voi thoi gian roi khoi
        checkOnlineStatus("online");
        checkTypingStatus("noOne");

        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    //neu kq la chap nhan
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //mo camera
                        pickFromCamera();
                    } else {
                        Toast.makeText(ChatActivity.this, "Vui lòng cấp quyền cho Camera & Thư viện", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            break;
            case STORAGE_REQUEST_CODE: {

                if (grantResults.length > 0)
                {
                    //neu kq la chap nhan
                    // boolean cameraAccepted = grantResults[0] ==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //mo thu vien
                        pickFromGallery();
                        Toast.makeText(ChatActivity.this, "qua", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "Vui lòng cấp quyền cho Thư viện ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if(resultCode == RESULT_OK)
        {

            if(requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                //image is picked form gallery, get uri of image
                image_uri = data.getData();

                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                //image is picked form camera, get uri of image
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

//        image_uri = data.getData();
//        uploadProfileCoverPhoto(image_uri);

        super.onActivityResult(requestCode, resultCode, data);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        //hide search view
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setTitle("Thông báo");
            builder.setMessage("Bạn có muốn đăng xuất không?");
            builder.setCancelable(false);
            builder.setPositiveButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(ChatActivity.this, "Đã hủy bỏ thao tác", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //mUserRef.child("onlineStatus").setValue(ServerValue.TIMESTAMP);
                    //checkUserStatus();
                    //setup timeStamp khi logout
                    String timestamp = String.valueOf(System.currentTimeMillis());
                    checkOnlineStatus(timestamp);
                    LoginManager.getInstance().logOut();
                    FirebaseAuth.getInstance().signOut();

                    startActivity(new Intent(ChatActivity.this, StartActivity.class));
                    finish();
                    Toast.makeText(ChatActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
