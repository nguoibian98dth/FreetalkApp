package com.example.freetalk.adapters;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.freetalk.R;
import com.example.freetalk.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static  final  int MSG_TYPE_LEFT = 0;
    private static  final  int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser firebaseUser;

    private ArrayList<ClipData.Item> mItems;


    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflater(chuyen xml thanh view): row_chat_left.xml for receiver, row_chat_right.xml for sender
        if(viewType == MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent,false);
            return new MyHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent,false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {

        //get data
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();
        String type = chatList.get(position).getType();

        //format timeStamp about dd/mm/yyyy h:m AM/PM
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        if(type.equals("text")){
            //set visibility message textview
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);

            holder.messageTv.setText(message);
        }
        else{
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
        }

        //set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);
        try
        {

            Picasso.get().load(imageUrl).into(holder.profileIv);
        }
        catch(Exception e)
        {

        }

        // click to show delete dialog
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xóa tin nhắn");
                builder.setMessage("Bạn có chắc muốn xóa tin nhắn này không ?");
                //delete button
                builder.setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteMessage(position);
                    }
                });
                //cancel button
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        //set status message
        //neu position = size -1 -> da co 1 mess seen di o vi tri position = size -> mess do da seen
        if(position == chatList.size()-1)
        {
            if(chatList.get(position).isSeen())//if true
                holder.isSeenTv.setText("Đã xem");
            else
            {
                holder.isSeenTv.setText("Đã gửi");
            }
        }
        else
        {
            holder.isSeenTv.setVisibility(View.GONE);
        }

    }

    private void deleteMessage(int position) {

        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

         /*
            ý tưởng
                Lấy ra timestamp của message dc click
                So sánh timestamp của message dc click với tất cả mesage in chats
                khi cả 2 giá trị khớp >> delete that message
         * */
         String msgTimesStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimesStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    /*
                        So sánh giá trị sender vs current user
                        Nếu giống nhau thì cho phép xóa message của 9 sender
                    */

                    if(ds.child("sender").getValue().equals(myUID))
                    {
                        //REMOVE mesage from Chats firebase
                        //ds.getRef().removeValue();

                        //set value of message "Tin nhắn naỳ đã bị xóa"
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("message","Tin nhắn này đã bị xóa...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context,"Tin nhắn đã được xóa...",Toast.LENGTH_SHORT).show();
                    }

                    else
                    {
                        Toast.makeText(context,"Bạn chỉ được xóa tin nhắn của mình thôi",Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        //return size of list
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //if sender is current user then return MSG_TYPE_RIGHT
        if(chatList.get(position).getSender().equals(firebaseUser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder
    {
        //views
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout; // for click listener show delete


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            messageIv = itemView.findViewById(R.id.messageIV);
            profileIv = itemView.findViewById(R.id.profileIV);
            messageTv = itemView.findViewById(R.id.messageTV);
            timeTv = itemView.findViewById(R.id.timeTV);
            isSeenTv = itemView.findViewById(R.id.isSeenTV);
            messageLayout = itemView.findViewById(R.id.messageLayout);

        }
    }
}
