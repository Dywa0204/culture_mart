package com.udin.culturemart.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.udin.culturemart.R;
import com.udin.culturemart.models.ChatModel;
import com.udin.culturemart.models.MessageModel;
import com.udin.culturemart.models.UserModel;
import com.udin.culturemart.utils.AESCrypt;
import com.udin.culturemart.utils.SweetAlert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    TextInputEditText etUsername, etPassword;
    TextInputLayout lPassword;
    Button loginBtn, loginGoogle, loginFb;
    TextView registerNow;
    LoginButton loginFBideBtn;

    SweetAlert sweetAlert;
    SweetAlertDialog loadingDialog;

    UserModel user;

    boolean isShowPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();
        createRequest();

        etUsername = findViewById(R.id.login_username);
        etPassword = findViewById(R.id.login_password);
        lPassword = findViewById(R.id.login_password_layout);

        loginBtn = findViewById(R.id.login_btn);
        loginGoogle = findViewById(R.id.login_google);
        loginFb = findViewById(R.id.login_fb);
        loginFBideBtn = findViewById(R.id.login_fb_hide_btn);

        registerNow = findViewById(R.id.register_now);

        sweetAlert = new SweetAlert(this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        loginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginGoogle();
            }
        });

        loginFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog = sweetAlert.loading("Sedang memuat...");
                loginFBideBtn.performClick();
            }
        });

        registerNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        lPassword.setEndIconOnClickListener(new View.OnClickListener() {
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

        loginFBideBtn.setReadPermissions("email", "public_profile");
        loginFBideBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                loadingDialog.dismiss();
            }

            @Override
            public void onError(@NonNull FacebookException error) {

            }

        });
    }

    //LOGIN WITH USERNAME AND PASSWORD
    private void login() {
        loadingDialog = sweetAlert.loading("Sedang memuat...");

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean isUserExist = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = document.getString("username");
                            String password = document.getString("password");
                            String fullname = document.getString("fullname");
                            String id = document.getId();

                            assert username != null;
                            if (username.equals(Objects.requireNonNull(etUsername.getText()).toString())) {
                                isUserExist = true;

                                user = new UserModel(id, username, password, fullname);
                            }
                        }

                        loadingDialog.dismiss();

                        if (isUserExist) {
                            try {
                                String passwordDecrypt = AESCrypt.decrypt(user.getPassword());

                                if (passwordDecrypt.contentEquals(Objects.requireNonNull(etPassword.getText()))) {
                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("user_id", user.getId());
                                    editor.putString("user_username", user.getUsername());
                                    editor.putString("user_fullname", user.getFullname());
                                    editor.apply();

                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    sweetAlert.error("Masuk akun gagal", "Password salah");
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                        } else {
                            sweetAlert.error("Masuk akun gagal", "Username tidak ditemukan");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        sweetAlert.error("Masuk akun gagal", getString(R.string.err_connection));
                    }
                });
    }

    private void loginGoogle() {
        loadingDialog = sweetAlert.loading("Sedang memuat...");

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                loadingDialog.dismiss();

                if (Objects.requireNonNull(e.getMessage()).contains("12501")) {
                    sweetAlert.error("Masuk akun gagal", "Akun Google tidak diketahui");
                } else {
                    sweetAlert.error("Masuk akun gagal", getString(R.string.err_connection));
                }
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            assert user != null;
                            String username = Objects.requireNonNull(user.getEmail()).split("@")[0];
                            String fullname = user.getDisplayName();

                            loginWithSSO(username, fullname);
                        } else {
                            loadingDialog.dismiss();
                            sweetAlert.error("Masuk akun gagal", getString(R.string.err_connection));
                        }
                    }
                });
    }

    private void loginWithSSO(String ssoUsername, String ssoFullname) {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean isUserExist = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = document.getString("username");
                            String fullname = document.getString("fullname");
                            String id = document.getId();

                            assert username != null;
                            if (username.equals(ssoUsername)) {
                                isUserExist = true;

                                user = new UserModel(id, username, fullname);
                            }
                        }

                        if (isUserExist) {
                            loadingDialog.dismiss();
                            startHomeActivity();
                        } else {
                            registerNewSSOUser(ssoUsername, ssoFullname);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        sweetAlert.error("Masuk akun gagal", getString(R.string.err_connection));
                    }
                });
    }

    private void registerNewSSOUser(String ssoUsername, String ssoFullname) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", ssoUsername);
        userData.put("fullname", ssoFullname);

        db.collection("users")
                .add(userData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        loadingDialog.dismiss();
                        user = new UserModel(documentReference.getId(), ssoUsername, ssoFullname);
                        startHomeActivity();
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

    private void startHomeActivity() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user_id", user.getId());
        editor.putString("user_username", user.getUsername());
        editor.putString("user_fullname", user.getFullname());
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            assert user != null;
                            String username = Objects.requireNonNull(user.getDisplayName()).replaceAll(" ", "").toLowerCase();
                            String fullname = user.getDisplayName();

                            loginWithSSO(username, fullname);
                        } else {
                            loadingDialog.dismiss();
                            sweetAlert.error("Daftar akun gagal", getString(R.string.err_connection));
                        }
                    }
                });
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
    }

    private void getChats() {
        loadingDialog = sweetAlert.loading("Memuat daftar chat...");

        for (ChatModel chatList : user.getChatList()) {
            db.collection("chats")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ArrayList<ChatModel> chatLists = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getId().equals(chatList.getId())) {
                                    MessageModel lastMessage = null;
                                    ArrayList<Object> messagesTemp = (ArrayList<Object>) document.get("messages");
                                    assert messagesTemp != null;
                                    for (Object message: messagesTemp) {
                                        Map<String, Object> messageMap = (Map<String, Object>) message;
                                        lastMessage = new MessageModel(
                                                (Timestamp) messageMap.get("datetime"),
                                                (String) messageMap.get("from"),
                                                (String) messageMap.get("message")
                                        );
                                    }

                                    ChatModel cm = new ChatModel(
                                            chatList.getId(),
                                            chatList.getWith(),
                                            lastMessage
                                    );

                                    chatLists.add(cm);
                                }
                            }

                            user = new UserModel(
                                    user.getId(),
                                    user.getUsername(),
                                    user.getPassword(),
                                    user.getFullname(),
                                    chatLists
                            );

                            loadingDialog.dismiss();

                            Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                            Gson gson = new Gson();
                            String userJson = gson.toJson(user);
                            intent.putExtra("user", userJson);
                            startActivity(intent);
                            finish();
                        }
                    });
        }
    }

    //                                    ArrayList<ChatModel> chatLists = new ArrayList<>();
//                                    ArrayList<Object> chatListTemp = (ArrayList<Object>) document.get("chats");
//                                    assert chatListTemp != null;
//                                    for (Object chatList: chatListTemp) {
//                                        Map<String, Object> chatListMap = (Map<String, Object>) chatList;
//                                        Map<String, Object> withMap = (Map<String, Object>) chatListMap.get("with");
//
//                                        assert withMap != null;
//                                        UserModel um = new UserModel(
//                                                (String) withMap.get("id"),
//                                                (String) withMap.get("username"),
//                                                (String) withMap.get("fullname")
//                                        );
//
//                                        ChatModel cm = new ChatModel(
//                                                (String) chatListMap.get("id"),
//                                                (UserModel) um,
//                                                null
//                                        );
//                                        chatLists.add(cm);
//                                    }
}