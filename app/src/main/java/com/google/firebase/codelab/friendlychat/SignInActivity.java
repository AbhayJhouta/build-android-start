/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.codelab.friendlychat.databinding.ActivityMainBinding;
import com.google.firebase.codelab.friendlychat.databinding.ActivitySignInBinding;

import static com.google.firebase.codelab.friendlychat.R.string.default_web_client_id;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private ActivitySignInBinding mBinding;
    private GoogleSignInClient mSignInClient;//A client for interacting with the Google Sign In API.

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // Set click listeners
        mBinding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Configure Google Sign In
        //GoogleSignInOptions -contains options used to configure the Auth.GOOGLE_SIGN_IN_API.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //Builder returns the the new google sgin in options
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()//Specifies that email info is requested by your application
                .build();
        mSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize FirebaseAuth
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            //GoogleSignInAccount-Class that holds the basic account information of the signed in Google user.
            //Task-A task is a collection of activities that users interact with when performing a certain job. The activities are arranged in a stack—the back stack
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);//to get the signed in account from the intent data
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);//Gets the result of the Task, if it has already completed.
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        //Represents the Google Sign-In authentication provider
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);//where first parameter is a valid Google Sign-In id token, obtained from the Google Sign-In SDK,second a valid Google Sign-In access token, obtained from the Google Sign-In SDK
        mFirebaseAuth.signInWithCredential(credential)//throws exception in many cases like other user is there with same credentials
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // If sign in succeeds the auth state listener will be notified and logic to
                        // handle the signed in user can be handled in the listener.
                        Log.d(TAG, "signInWithCredential:success");
                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential", e);
                        Toast.makeText(SignInActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mSignInClient.getSignInIntent();//Gets an Intent to start the Google Sign In flow
        startActivityForResult(signInIntent, RC_SIGN_IN);//  for these type of avtivity which return some result,it starts the activity of intent and When this activity exits, your onActivityResult() method will be called when result is sent,when second parameter is If >= 0, this code will be
                                                          // returned in onActivityResult() when the activity exits


    }
}
