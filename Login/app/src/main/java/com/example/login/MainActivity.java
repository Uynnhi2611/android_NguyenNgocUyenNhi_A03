package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    TextView createnewAccount;
    EditText inputEmail, inputPassword;
    Button btnLogin;
    String emailPattern="[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    ImageView btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createnewAccount=findViewById(R.id.createNewAccount);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        inputEmail= findViewById(R.id.inputEmail);
        inputPassword=findViewById(R.id.inputPassword);
        progressDialog= new ProgressDialog(this);
        btnLogin=findViewById(R.id.btnLogin);
        mAuth= FirebaseAuth.getInstance();
        mUser= mAuth.getCurrentUser();
        btnGoogle=findViewById(R.id.btnGoogle);
        createnewAccount.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,RegisterActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                perforLogin();
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,GoogleSignInActivity.class);
                startActivity(intent);
            }
        });
    }
    private void perforLogin(){
        String email=inputEmail.getText().toString();
        String password=inputPassword.getText().toString();

        if(!email.matches(emailPattern)) {
            inputEmail.setError("Enter Correct Email");
        } else if(password.isEmpty() || password.length()<6) {
            inputPassword.setError("Enter Proper Password");
        }  else {
            progressDialog.setMessage("Please wait While Login...");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success
                                progressDialog.dismiss();
                                sendUserToNextActivity();
                                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            }else {
                                // If sign in fails
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Authentication failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void sendUserToNextActivity(){
        Intent intent= new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}