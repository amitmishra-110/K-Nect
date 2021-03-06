package com.example.wherechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{

    private  String messageReceiverID,messageReceiverName,messageReceiverImage,
            messageSenderID;
    private TextView userName,userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ImageButton sendMessageButton,sendFilesButton;
    private EditText messageInputText;
    private final List<Messages>messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private  MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private  String saveCurrentTime,saveCurrentDate;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        messageReceiverID=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage=getIntent().getExtras().get("visit_image").toString();



        IntialiseController();


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);


        sendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendMesssage();

            }
        });
        displayLastSeen();




    }

    private void IntialiseController()
    {
        chatToolbar=(Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView= layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=(CircleImageView)findViewById(R.id.custom_profile_image);
        userName=(TextView)findViewById(R.id.custom_profile_name);
        userLastSeen=(TextView)findViewById(R.id.custom_user_last_seen);


        sendMessageButton=(ImageButton)findViewById(R.id.send_message_btn);
        sendFilesButton=(ImageButton)findViewById(R.id.send_files_btn);
        messageInputText=(EditText)findViewById(R.id.input_message);


        messageAdapter=new MessageAdapter(messagesList);
        userMessagesList=(RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);

        userMessagesList.setAdapter(messageAdapter);


        Calendar calender= Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate=currentDate.format(calender.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calender.getTime());



    }

    private  void displayLastSeen()
    {
        rootRef.child("Users").child(messageReceiverID).
                addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                             String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if(state.equals("Online"))
                            {
                               userLastSeen.setText("Online");

                            }
                            else if(state.equals("Offline"))
                            {
                               userLastSeen.setText("Last Seen:" +date +" "+time);

                            }

                        }
                        else
                        {
                           userLastSeen.setText("Offline");

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
       .addChildEventListener(new ChildEventListener()
       {
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
           {
               Messages messages=dataSnapshot.getValue(Messages.class);

               messagesList.add(messages);

               userMessagesList.smoothScrollToPosition(userMessagesList.
                       getAdapter().getItemCount());




               messageAdapter.notifyDataSetChanged();



           }

           @Override
           public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
           {

           }

           @Override
           public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
           {

           }

           @Override
           public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }

    private void sendMesssage()
    {
        String messageText=messageInputText.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Please enter  your message", Toast.LENGTH_SHORT).show();

        }
        else
        {
            String messageSenderRef="Messages/"+messageSenderID + "/" +messageReceiverID;
            String messageReceiverRef="Messages/"+messageReceiverID + "/" +messageSenderID;


            DatabaseReference userMessageKeyRef=rootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID=userMessageKeyRef.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageReceiverID);
            messageTextBody.put("messageID",messagePushID);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);



            Map messageBodyDetails=new HashMap();
           messageBodyDetails.put(messageSenderRef + "/" +messagePushID,messageTextBody);
           messageBodyDetails.put(messageReceiverRef + "/" +messagePushID,messageTextBody);

           rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener()
           {
               @Override
               public void onComplete(@NonNull Task task)
               {
                   if(task.isSuccessful())
                   {
                       Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();

                   }
                   else
                   {
                       Toast.makeText(ChatActivity.this," Error", Toast.LENGTH_SHORT).show();

                   }
                   messageInputText.setText("");

               }
           });


        }
    }
}
