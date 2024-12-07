package com.example.chat1104.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chat1104.R;
import com.example.chat1104.databinding.ActivitySignInBinding;
import com.example.chat1104.utilities.Constants;
import com.example.chat1104.utilities.PrefrenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PrefrenceManager prefrenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefrenceManager = new PrefrenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        
        binding.buttonSignIn.setOnClickListener(v -> {
            if(isValidateSignInDetails()){
                SignIn();
            }
        });
    }

    /**
     * connecting to firebase to check if a user exist through their email or password
     * if their info is found in the firebase database, sign them in
     */
    private void SignIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        prefrenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        prefrenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        prefrenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_IMAGE));
                        prefrenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        loading(false);
                        showToast("Unable to Sign in");
                    }
                });
    }

    /**
     *
     * @param isLoading show the loading spinner if the program is loading, hide it if its not
     */
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * display a message through toast on the app screen
     * @param message
     */
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * check to make sure a user entered a valid email and password
     * @return true or false depending if a user entered a valid email and password
     */
    private boolean isValidateSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Please Enter Your Email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please Enter Valid Email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Please Enter Your Password");
            return false;
        }else{
            return true;
        }
    }


}