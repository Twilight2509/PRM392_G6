package com.example.mychatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mychatapp.R;
import com.example.mychatapp.databinding.ActivitySingUpBinding;
import com.example.mychatapp.firebase.PreferenceManager;
import com.example.mychatapp.utilities.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private String encodedImage;
    private ImageView imageProfile, imageBack;
    private EditText inputName, inputEmail;
    private MaterialButton buttonUpdate;
    private ProgressBar progressBar;
    private TextView textAddImage, textSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        preferenceManager = new PreferenceManager(getApplicationContext());

        imageProfile = findViewById(R.id.imageProfile);
        imageBack = findViewById(R.id.imageBack);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        progressBar = findViewById(R.id.progressBar);
        textAddImage = findViewById(R.id.textAddImage);
        textSignIn = findViewById(R.id.textSignIn);

        loadUserDetails();
        setListeners();
    }

    private void setListeners() {
        // Quay lại MainActivity khi nhấn vào imageBack
        imageBack.setOnClickListener(v -> finish());


        // Đăng xuất khi nhấn vào textSignIn
        textSignIn.setOnClickListener(v -> {
            // Xóa thông tin đăng nhập và trở về màn hình đăng nhập
            preferenceManager.clear();
            Intent intent = new Intent(UserProfileActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Cập nhật profile
        buttonUpdate.setOnClickListener(v -> {
                updateProfile();

        });

        // Chọn ảnh mới khi nhấn vào layoutImage
        findViewById(R.id.layoutImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void loadUserDetails() {
        // Hiển thị thông tin người dùng
        inputName.setText(preferenceManager.getString(Constants.KEY_NAME));
        inputEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));

        // Decode ảnh từ chuỗi Base64 và hiển thị
        String encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageProfile.setImageBitmap(bitmap);
            textAddImage.setVisibility(View.GONE);
        }
    }

    private void updateProfile() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, inputEmail.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .update(user)
                .addOnSuccessListener(unused -> {
                    loading(false);
                    preferenceManager.putString(Constants.KEY_NAME, inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, inputEmail.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    showToast("Profile Updated");
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageProfile.setImageBitmap(bitmap);
                        textAddImage.setVisibility(View.GONE);
                        encodedImage = encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );


    private void loading(Boolean isLoading) {
        if (isLoading) {
            buttonUpdate.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            buttonUpdate.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}

