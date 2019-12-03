package com.example.freetalk;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //Storage
    StorageReference storageReference;


    //path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    ImageView avatar_profile;
    CircleImageView avatar_profileC;
    ImageView cover_image;
    TextView nameTV, emailTV, phoneTV;
    FloatingActionButton fab;

    //progress diaglog
    ProgressDialog pd;

    //permission constant
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200; //BỘ NHỚ
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    private static final int GALLERY_PICK = 1;

    //khai báo 1 mảng chứa request
    String cameraPermissions[];
    String storagePermissions[];

    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //firebase storage reference

        //init array of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init
        avatar_profileC= view.findViewById(R.id.avatar_profile);
        cover_image= view.findViewById(R.id.cover);
        nameTV= view.findViewById(R.id.nameTV);
        emailTV= view.findViewById(R.id.emailTV);
        phoneTV= view.findViewById(R.id.phonenumberTV);
        fab = view.findViewById(R.id.fab_profile);
        //

        pd= new ProgressDialog(getActivity());
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //kiem tra yêu cầu lấy data
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    //lấy dữ liệu
                    String name = ""+ds.child("name").getValue();
                    String email = ""+ds.child("email").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String cover= ""+ds.child("cover").getValue();

                    //đổ dữ liệu
                    nameTV.setText(name);
                    emailTV.setText(email);
                    phoneTV.setText(phone);

                    try
                    {
                        Picasso.get().load(image).into(avatar_profileC);
                    }
                    catch (Exception ex)
                    {
                        //nếu ko có ảnh sẽ set giá trị mặc định
                        Picasso.get().load(R.drawable.default_avatar).into(avatar_profileC);

                    }

                    try
                    {
                        Picasso.get().load(cover).into(cover_image);
                    }
                    catch (Exception ex)
                    {
                        //nếu ko có ảnh sẽ set giá trị mặc định
                        Picasso.get().load(R.drawable.ic_add_image).into(cover_image);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //sự kiện click fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private boolean checkStoragePermission()
    {

        boolean resuit = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return resuit;
    }

    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(getActivity(),storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission()
    {
        boolean resuit = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean resuit1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_DENIED);
        return resuit && resuit1;
    }

    private void requestCameraPermission()
    {
       ActivityCompat.requestPermissions(getActivity(),cameraPermissions, CAMERA_REQUEST_CODE);

    }

    private void showEditProfileDialog()
    {
        //khai báo 1 mảng chứa thông tin chinh sửa
        String options[] = {"Thay đổi ảnh đại diện", "Thay đổi ảnh bìa", "Thay đổi tên hiển thị", "Thay đổi số điện thoại"};
        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Vui lòng chọn tính năng");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item click
                if(which == 0)// chỉnh sửa ảnh dại diện
                {
                    pd.setMessage("Đang thay đổi ảnh đại diện");
                    profileOrCoverPhoto = "image";
                    showImagePicDiaLog();

                }
                else if(which == 1)// chỉnh sửa ảnh nền
                {
                    pd.setMessage("Đang thay đổi ảnh nền");
                    profileOrCoverPhoto = "cover";
                    showImagePicDiaLog();

                }
                else if(which == 2) // chỉnh sửa tên
                {
                    pd.setMessage("Đang thay đổi tên");
                    showNamePhoneUpdateDialog("name");
                }
                else if(which == 3)// chỉnh sửa sdt
                {
                    pd.setMessage("Đang thay đổi số điện thoại");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        //tạo và hiển thị dialog
        builder.create().show();
    }

    private void showImagePicDiaLog()
    {

        String options[] = {"Máy ảnh", "Thư viện"};
        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                        Toast.makeText(getActivity(),""+e.getMessage() ,Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(),""+e.getMessage() ,Toast.LENGTH_SHORT).show();
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
            image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
            Toast.makeText(getActivity(),""+e.getMessage() ,Toast.LENGTH_SHORT).show();
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

    private void showNamePhoneUpdateDialog(final String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Thay đổi " + key);
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        // add views in layout
        final EditText editText = new EditText(getActivity());
        editText.setHint("Nhập vào " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add buttons in dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value))
                {
                    pd.show();
                    HashMap<String,Object> result = new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Đã cập nhât" ,Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),e.getMessage() ,Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else
                {
                    Toast.makeText(getActivity(),"Vui lòng nhập vào giá trị" ,Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //create and show dialog
        builder.create().show();
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
                        Toast.makeText(getActivity(), "Vui lòng cấp quyền cho Camera & Thư viện", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "qua", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Vui lòng cấp quyền cho Thư viện ảnh", Toast.LENGTH_SHORT).show();
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

                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                //image is picked form camera, get uri of image
                uploadProfileCoverPhoto(image_uri);
            }
        }

//        image_uri = data.getData();
//        uploadProfileCoverPhoto(image_uri);

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri)
    {
        pd.show();
        // đường dẫn và tên image dc get từ0 firebase storage
        String filePathAndName = storagePath+ ""+ profileOrCoverPhoto +"_"+ user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, now get it's url and store in user's db
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!((Task) uriTask).isSuccessful());
                        Uri downloadUri= uriTask.getResult();

                        //check if image is uploaded or not and url is received
                        if(uriTask.isSuccessful())
                        {
                            //image uploaded
                            //add/update url in user's db
                            HashMap<String,Object> results = new HashMap<>();
                             /* lần đầu sd profileOrCoverPhoto này có giá tri "image" of "cover"
                            dựa vào keys của user's db như url của image sẽ dc save lần đầu
                            Lần 2 trở đi  storage url của image đã được nằm trên Storage Firebase
                             url sẽ dc lưu như 1 giá trị ghi chồng lên khóa "image* và "cover"*/
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Ảnh đã được thay dổi",Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Lỗi khi đang thay dổi",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Một vài lỗi đã xảy ra",Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void checkUserStatus()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user !=null)
        {
            //được đăng nhập tại đây

        }
        else
        {
            startActivity(new Intent(getActivity(), StartActivity.class));
            getActivity().finish();

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(false);// lam xuat hien menu
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    //handle menu Item Logout click

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout)
        {
            LoginManager.getInstance().logOut();
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}
