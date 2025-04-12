package com.example.weathernow.firebase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    private final FirebaseAuth auth;

    public AuthManager() {
        auth = FirebaseAuth.getInstance();
    }

    public void signInAnonymously(OnCompleteListener<AuthResult> callback) {
        auth.signInAnonymously().addOnCompleteListener(callback);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}
