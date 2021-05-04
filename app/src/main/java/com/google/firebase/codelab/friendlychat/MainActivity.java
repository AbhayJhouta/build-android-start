/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.codelab.friendlychat.databinding.ActivityMainBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String MESSAGES_CHILD = "messages";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";

    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private SharedPreferences mSharedPreferences;
    private GoogleSignInClient mSignInClient;

    private ActivityMainBinding mBinding;//activity_main. xml will generate a class called ActivityMainBinding
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mDatabase;
    NotificationManagerCompat notificationManager ;
    String CHANNEL_ID="13";
    int id1=0;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;
    // Firebase instance variables
    // TODO(you): Add instance variables here
    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        // First upload the image to Cloud Storage
        storageReference.putFile(uri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // After the image loads, get a public downloadUrl for the image
                        // and add it to the message.
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        FriendlyMessage friendlyMessage = new FriendlyMessage(
                                                null, getUserName(), getUserPhotoUrl(), uri.toString(),getDateTime());
                                        mDatabase.getReference()
                                                .child(MESSAGES_CHILD)
                                                .child(key)
                                                .setValue(friendlyMessage);
                                    }
                                });
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Image upload task was not successful.", e);
                    }
                });
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String description = "channel 1";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "one", importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {//Android Bundle is used to pass data between activities. The values that are to be passed are mapped to String keys which are later used in the next activity to retrieve the values.
        super.onCreate(savedInstanceState);


        mBinding = ActivityMainBinding.inflate(getLayoutInflater());//LayoutInflater is a class used to reprsent layout XML file into its corresponding view objects which can be used in Java programs
        setContentView(mBinding.getRoot());//Returns the outermost View in the layout file associated with the Binding.
        mFirebaseAuth = FirebaseAuth.getInstance();
        createNotificationChannel();
        if (mFirebaseAuth.getCurrentUser() == null) //Returns the currently signed-in FirebaseUser or null if there is none.
        {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        DatabaseReference arrow=FirebaseDatabase.getInstance().getReference().child("Poll").child("set");
        arrow.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {


                if(snapshot.getValue().toString().equals("us"))
                {
                    mBinding.arrow.setVisibility(View.INVISIBLE);
                }
                else
                { mBinding.arrow.setVisibility(View.VISIBLE);
                }

            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed, how to handle?

            }

        });


        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);//on it

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mSignInClient = GoogleSignIn.getClient(this, gso);

        mLinearLayoutManager = new LinearLayoutManager(this);//tells the layout of recyclerveiw should start from down
        mLinearLayoutManager.setStackFromEnd(true);//it should start from end
        mBinding.messageRecyclerView.setLayoutManager(mLinearLayoutManager);

        // Initialize Realtime Database
        mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference messagesRef = mDatabase.getReference().child(MESSAGES_CHILD);
     /*   messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                     if(snapshot.exists()) {
                         id1 = (int) snapshot.getChildrenCount();
                     }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });    */
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.child("name").getValue().toString()!=getUserName()) {
                    if(snapshot.child("text").exists()) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(snapshot.child("name").getValue().toString())
                                .setContentText(snapshot.child("text").getValue().toString())
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        notificationManager = NotificationManagerCompat.from(MainActivity.this);
                        notificationManager.notify(13, builder.build());
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

// The FirebaseRecyclerAdapter class comes from the FirebaseUI library
// See: https://github.com/firebase/FirebaseUI-Android
        FirebaseRecyclerOptions<FriendlyMessage> options =//configure the the firebaseRecycler adapter by building FirebaseRecyclerOptions
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(messagesRef, FriendlyMessage.class)//binds refernce and class with recylerveiw
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {//firebaseresyclerAdapter binds a Query to a RecyclerView and responds to all real-time events included items being added, removed, moved, or changed
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());//
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(MessageViewHolder vh, int position, FriendlyMessage message) {
                mBinding.progressBar.setVisibility(ProgressBar.INVISIBLE);
                vh.bindMessage(message);
               /* NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this,CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("user")
                        .setContentText(message.getText())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                notificationManager= NotificationManagerCompat.from(MainActivity.this);
                notificationManager.notify(13, builder.build());*/
            }
        };
        Button bpoll=findViewById(R.id.pollb);
        bpoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference pollcheck=FirebaseDatabase.getInstance().getReference().child("Poll").child("set");
                pollcheck.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {


                        if(snapshot.getValue().toString().equals("us"))
                            {
                               startActivity(new Intent(MainActivity.this,pollb.class));
                            }
                        else
                            { startActivity(new Intent(MainActivity.this,pollG.class));
                            }

                        }


                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed, how to handle?

                    }

                });

            }
        });
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mBinding.messageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mBinding.messageRecyclerView.setAdapter(mFirebaseAdapter);


// Scroll down when a new message arrives
// See MyScrollToBottomObserver.java for details
        mFirebaseAdapter.registerAdapterDataObserver(//on it
                new MyScrollToBottomObserver(mBinding.messageRecyclerView, mFirebaseAdapter, mLinearLayoutManager));

        mBinding.messageEditText.addTextChangedListener(new TextWatcher() {//onit
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mBinding.sendButton.setEnabled(true);
                } else {
                    mBinding.sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mBinding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FriendlyMessage friendlyMessage = new
                        FriendlyMessage(mBinding.messageEditText.getText().toString(),
                        getUserName(),
                        getUserPhotoUrl(),
                        null /* no image */,getDateTime());

                mDatabase.getReference().child(MESSAGES_CHILD).push().setValue(friendlyMessage);


                mBinding.messageEditText.setText("");
            }
        });

        mBinding.addMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);//ctivity Action: Allow the user to select and return one or more existing documents
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        mSignInClient.signOut();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    @Nullable
    private String getUserPhotoUrl() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            return user.getPhotoUrl().toString();
        }
        return null;
    }
    private String getDateTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
    private String getUserName() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            return user.getDisplayName();
        }
        return ANONYMOUS;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                final Uri uri = data.getData();
                Log.d(TAG, "Uri: " + uri.toString());

                final FirebaseUser user = mFirebaseAuth.getCurrentUser();
                FriendlyMessage tempMessage = new FriendlyMessage(
                        null, getUserName(), getUserPhotoUrl(), LOADING_IMAGE_URL,getDateTime());
                mDatabase.getReference().child(MESSAGES_CHILD).push()
                        .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                                   DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException());
                                    return;
                                }

                                // Build a StorageReference and then upload the file
                                String key = databaseReference.getKey();
                                StorageReference storageReference =
                                        FirebaseStorage.getInstance()
                                                .getReference(user.getUid())
                                                .child(key)
                                                .child(uri.getLastPathSegment());

                                putImageInStorage(storageReference, uri, key);
                            }
                        });
            }
        }

    }
}
