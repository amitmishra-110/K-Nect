package com.example.wherechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{

    private  String receiverUserID,current_State,senderUserId;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton,declineRequestButton;
    private FirebaseAuth mAuth;

    private DatabaseReference userRef,chatReqRef,contactsRef,notificationRef;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

       mAuth=FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef= FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
       notificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");


       receiverUserID=getIntent().getExtras().get("visit_user_id").toString();
        senderUserId=mAuth.getCurrentUser().getUid();



        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView)findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView)findViewById(R.id.visit_user_status);
        sendMessageRequestButton=(Button)findViewById(R.id.send_message_request_button);
        declineRequestButton=(Button)findViewById(R.id.decline_message_request_button);
        current_State="new";
        retrieveUserInfo();

    }

    private void retrieveUserInfo()
    {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String  userImage=dataSnapshot.child("image").getValue().toString();
                    String  userName=dataSnapshot.child("name").getValue().toString();
                    String  userStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);


                    manageChatRequest();
                }

               else
                {
                    String  userName=dataSnapshot.child("name").getValue().toString();
                    String  userStatus=dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();
                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    private void manageChatRequest()
    {

        chatReqRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                    if(dataSnapshot.hasChild(receiverUserID))
                    {
                        String request_type=dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                                if(request_type.equals("sent"))
                                {
                                    current_State="request_sent";
                                    sendMessageRequestButton.setText("Cancel chat request");
                                }
                                else if (request_type.equals("received"))
                                {
                                    current_State="request_received";
                                    sendMessageRequestButton.setText("Accept Request");

                                    declineRequestButton.setVisibility(View.VISIBLE);
                                    declineRequestButton.setEnabled(true);

                                    declineRequestButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            cancelChatRequest();
                                        }
                                    });
                                }
                    }
                    else
                    {
                        contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.hasChild(receiverUserID))
                                {
                                    current_State="Friends";
                                    sendMessageRequestButton.setText("Remove this contact");

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
        });

        if(!senderUserId.equals(receiverUserID))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    sendMessageRequestButton.setEnabled(false);
                    if(current_State.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if(current_State.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if(current_State.equals("request_received"))
                    {
                       acceptChatRequest();
                    }
                    if(current_State.equals("Friends"))
                    {
                       removeSpecificContact();
                    }

                }
            });
        }
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact()
    {
       contactsRef.child(senderUserId).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    contactsRef.child(receiverUserID).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendMessageRequestButton.setEnabled(true);
                                current_State="new";
                                sendMessageRequestButton.setText("Send Message");

                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);
                            }
                        }
                    });
                }

            }
        });
    }

    private void acceptChatRequest()
        {
            contactsRef.child(senderUserId).child(receiverUserID).child("Contacts")
                    .setValue("Saved")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {

                                contactsRef.child(receiverUserID).child(senderUserId).child("Contacts")
                                        .setValue("Saved")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    chatReqRef.child(senderUserId).child(receiverUserID)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        chatReqRef.child(receiverUserID).child(senderUserId)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        sendMessageRequestButton.setEnabled(true);
                                                                                        current_State="Friends";

                                                                                        sendMessageRequestButton.setText("Remove this contact");

                                                                                        declineRequestButton.setVisibility(View.INVISIBLE);
                                                                                        declineRequestButton.setEnabled(false);


                                                                                    }
                                                                                });
                                                                    }

                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    });
    }

    private void cancelChatRequest()
    {

        chatReqRef.child(senderUserId).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    chatReqRef.child(receiverUserID).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {

                                sendMessageRequestButton.setEnabled(true);
                                current_State="new";
                                sendMessageRequestButton.setText("Send Message");

                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);
                            }
                        }
                    });
                }

            }
        });
    }

    private void sendChatRequest()
    {
        chatReqRef.child(senderUserId).child(receiverUserID).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    chatReqRef.child(receiverUserID).child(senderUserId)
                            .child("request_type")
                            .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                HashMap<String,String> chatNotificationMap=new HashMap<>();
                                chatNotificationMap.put("from",senderUserId);
                                chatNotificationMap.put("type","request");

                                notificationRef.child(receiverUserID).push()
                                        .setValue(chatNotificationMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {

                                                    sendMessageRequestButton.setEnabled(true);
                                                    current_State="request_sent";
                                                    sendMessageRequestButton.setText("Cancel chat request");

                                                }

                                            }
                                        });




                            }
                        }
                    });

                }
            }
        });
    }
}
