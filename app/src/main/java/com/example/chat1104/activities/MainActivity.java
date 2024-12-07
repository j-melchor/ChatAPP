package com.example.chat1104.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat1104.databinding.ActivityMainBinding;
import com.example.chat1104.utilities.Constants;
import com.example.chat1104.utilities.PrefrenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PrefrenceManager prefrenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefrenceManager = new PrefrenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
    }

    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());

        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UserActivity.class)));
    }


    private void loadUserDetails(){
        binding.textName.setText(prefrenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(prefrenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    /**
     * display a message through toast on the app screen
     * @param message
     */
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(prefrenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(unused -> showToast("Token updated successfully"))
                .addOnFailureListener(e -> showToast("Unable to update Token"));
    }

    private void signOut(){
        showToast("Signing out...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(prefrenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    prefrenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                }).addOnFailureListener(e -> showToast("Unable to sign out"));
    }

}