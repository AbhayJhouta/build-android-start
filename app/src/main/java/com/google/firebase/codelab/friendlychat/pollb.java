package com.google.firebase.codelab.friendlychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.codelab.friendlychat.databinding.ActivityPollbBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class pollb extends AppCompatActivity {
    public ActivityPollbBinding mbinding;
    //public DatabaseReference mdatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mbinding=ActivityPollbBinding.inflate(getLayoutInflater());
        setContentView(mbinding.getRoot());
        mbinding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference poll=FirebaseDatabase.getInstance().getReference().child("Poll");
                DatabaseReference setvalues=poll.child("Question");
                setvalues.setValue(mbinding.questionset.getText().toString());
                setvalues=poll.child("option1");
                setvalues.setValue(mbinding.option1.getText().toString());
                setvalues=poll.child("option2");
                setvalues.setValue(mbinding.option2.getText().toString());
                setvalues=poll.child("set");
                setvalues.setValue("s");
                startActivity(new Intent(pollb.this,pollG.class));
            }
        });






    }
}