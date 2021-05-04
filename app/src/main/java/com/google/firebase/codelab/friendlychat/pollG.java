package com.google.firebase.codelab.friendlychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.codelab.friendlychat.databinding.ActivityPollGBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class pollG extends AppCompatActivity {
    public ActivityPollGBinding mbinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mbinding=ActivityPollGBinding.inflate(getLayoutInflater());
        setContentView(mbinding.getRoot());
        mbinding.progressBar1.setMax(10);
        mbinding.progressBar2.setMax(10);
        DatabaseReference pollcheck= FirebaseDatabase.getInstance().getReference().child("Poll");
        pollcheck.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mbinding.questionn.setText("Q."+snapshot.child("Question").getValue().toString());
                mbinding.op1.setText(snapshot.child("option1").getValue().toString());
                mbinding.op2.setText(snapshot.child("option2").getValue().toString());
                mbinding.progressBar1.setProgress(Integer.parseInt(snapshot.child("op1v").getValue().toString()));
                mbinding.progressBar2.setProgress(Integer.parseInt(snapshot.child("op2v").getValue().toString()));

            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed, how to handle?

            }


        });
        mbinding.op1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               int current= mbinding.progressBar1.getProgress();
                mbinding.progressBar1.setProgress(current+1);
                final DatabaseReference op1i=FirebaseDatabase.getInstance().getReference().child("Poll").child("op1v");
                DatabaseReference op1add= FirebaseDatabase.getInstance().getReference().child("Poll");
                op1add.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                          op1i.setValue(Integer.parseInt(snapshot.child("op1v").getValue().toString())+1);
                    }


                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed, how to handle?

                    }


                });
               mbinding.op1.setEnabled(false);
               mbinding.op2.setEnabled(false);
            }

        });
        mbinding.op2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int current= mbinding.progressBar2.getProgress();
                mbinding.progressBar2.setProgress(current+1);
                final DatabaseReference op2i=FirebaseDatabase.getInstance().getReference().child("Poll").child("op2v");
                DatabaseReference op2add= FirebaseDatabase.getInstance().getReference().child("Poll");
                op2add.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        op2i.setValue(Integer.parseInt(snapshot.child("op2v").getValue().toString())+1);
                    }


                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed, how to handle?

                    }


                });
                mbinding.op1.setEnabled(false);
                mbinding.op2.setEnabled(false);
            }

        });
        mbinding.refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference refreshcheck= FirebaseDatabase.getInstance().getReference().child("Poll");
                refreshcheck.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        mbinding.progressBar1.setProgress(Integer.parseInt(snapshot.child("op1v").getValue().toString()));
                        mbinding.progressBar2.setProgress(Integer.parseInt(snapshot.child("op2v").getValue().toString()));
                    }


                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed, how to handle?

                    }


                });
            }

        });
        mbinding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference poll=FirebaseDatabase.getInstance().getReference().child("Poll").child("set");
                poll.setValue("us");
                poll=FirebaseDatabase.getInstance().getReference().child("Poll").child("op1v");
                poll.setValue(0);
                poll=FirebaseDatabase.getInstance().getReference().child("Poll").child("op2v");
                poll.setValue(0);
                startActivity(new Intent(pollG.this,MainActivity.class));
            }
        });

    }
}