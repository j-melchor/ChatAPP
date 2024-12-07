package com.example.chat1104.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat1104.adapters.UsersAdapter;
import com.example.chat1104.databinding.ActivityUserBinding;
import com.example.chat1104.listeners.UserListener;
import com.example.chat1104.models.User;
import com.example.chat1104.utilities.Constants;
import com.example.chat1104.utilities.PrefrenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity implements UserListener {

    private ActivityUserBinding binding;
    private PrefrenceManager prefrenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        prefrenceManager = new PrefrenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        setListeners();
        getUsers();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS).get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = prefrenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        } else{
                            showError();
                        }
                    }else{
                        showError();
                    }
                });
    }

    private void showError(){
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onUserClicked(User user) {
        // TODO
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}