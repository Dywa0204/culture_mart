package com.udin.culturemart.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.udin.culturemart.R;
import com.udin.culturemart.models.UserModel;
import com.udin.culturemart.utils.AESCrypt;
import com.udin.culturemart.utils.SweetAlert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegisterActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextInputEditText etPassword, etUsername, etFullname;
    TextInputLayout lPassword;
    TextView loginNow;
    Button registerButton;

    SweetAlert sweetAlert;
    SweetAlertDialog loadingDialog;

    UserModel user;

    boolean isShowPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sweetAlert = new SweetAlert(this);

        loginNow = findViewById(R.id.login_now);
        registerButton = findViewById(R.id.register_btn);

        etPassword = findViewById(R.id.register_password);
        etUsername = findViewById(R.id.register_username);
        etFullname = findViewById(R.id.register_fullname);

        lPassword = findViewById(R.id.register_password_layout);

        //REGISTER
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerValidity();
            }
        });

        //LOGIN NOW
        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //SET SHOW PASSWORD
        lPassword.setEndIconOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                isShowPassword = !isShowPassword;

                if (isShowPassword) {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    etPassword.setTransformationMethod(SingleLineTransformationMethod.getInstance());
                    lPassword.setEndIconDrawable(R.drawable.ic_visibility);
                } else {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    lPassword.setEndIconDrawable(R.drawable.ic_visibility_off);
                }
            }
        });
    }

    private void register(String username, String password, String fullname) throws Exception {
        String passwordEncrypt = AESCrypt.encrypt(password);

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", passwordEncrypt);
        userData.put("fullname", fullname);

        db.collection("users")
                .add(userData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        loadingDialog.dismiss();

                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("user_id", documentReference.getId());
                        editor.putString("user_username", username);
                        editor.putString("user_fullname", fullname);
                        editor.apply();

                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        sweetAlert.error("Daftar akun gagal", getString(R.string.err_connection));
                    }
                });
    }

    private void registerValidity(){
        loadingDialog = sweetAlert.loading("Sedang memuat...");

        String username = Objects.requireNonNull(etUsername.getText()).toString();
        String password = Objects.requireNonNull(etPassword.getText()).toString();
        String fullname = Objects.requireNonNull(etFullname.getText()).toString();

        if (checkValidity(username, password, fullname)) {
            checkUniqueUsername(username, password, fullname);
        } else {
            loadingDialog.dismiss();
            sweetAlert.error("Daftar akun gagal", "Harap isi semua kolom");
        }
    }

    private void checkUniqueUsername(String username, String password, String fullname) {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean isUsernameNotExist = true;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String user = document.getString("username");

                            assert user != null;
                            if (user.equals(username)) {
                                isUsernameNotExist = false;
                                break;
                            }
                        }

                        if (isUsernameNotExist) {
                            try {
                                register(username, password, fullname);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            loadingDialog.dismiss();
                            sweetAlert.error("Daftar akun gagal", "Username telah terpakai");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        sweetAlert.error("Daftar akun gagal", getString(R.string.err_connection));
                    }
                });
    }

    private boolean checkValidity(String username, String password, String fullname) {
        return !username.isEmpty() && !password.isEmpty() && !fullname.isEmpty();
    }
}