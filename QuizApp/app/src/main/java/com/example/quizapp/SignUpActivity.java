package com.example.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText name,email,pass,confirmPass;
    private Button btnSignUp;
    private ImageView btnBack;
    private FirebaseAuth mAuth;
    private String emailStr,passStr,confirmPassStr,nameStr;
    String emailPattern = "(?=.*[a-z])(?=.*[0-9])[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    private Dialog progressDialog;
    private TextView dialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        name=findViewById(R.id.username);
        email=findViewById(R.id.emailID);
        pass=findViewById(R.id.password);
        confirmPass=findViewById(R.id.confirm_pass);
        btnSignUp=findViewById(R.id.btnSignUp);
        btnBack=findViewById(R.id.btnBback);

        progressDialog=new Dialog(SignUpActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText=progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Registering user...");
        mAuth= FirebaseAuth.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                if(validate()){
                    signupNewUser();
                }
            }
        });

    }

    private boolean validate(){
        nameStr=name.getText().toString().trim();
        passStr=pass.getText().toString().trim();
        emailStr=email.getText().toString().trim();
        confirmPassStr=confirmPass.getText().toString().trim();

        if(nameStr.isEmpty()){
            name.setError("Enter Your Name");
            return false;
        }

        if(emailStr.isEmpty()){
            email.setError("Enter Email ID");
            return false;
        }

        if(passStr.isEmpty()){
            pass.setError("Enter Password");
            return false;
        }
        if(confirmPassStr.isEmpty()){
            pass.setError("Enter Confirm Password");
            return false;
        }


        if(!emailStr.matches(emailPattern)) {
            email.setError("Email name must contain at least one character and one number");
            return false;
        }

        if(passStr.length()<6) {
            pass.setError("Password must contain 6 characters");
        }
        if(passStr.compareTo(confirmPassStr)!=0){
            Toast.makeText(SignUpActivity.this,"Password and confirm Password should be same!",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void signupNewUser(){

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(emailStr, passStr)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this,"Sign Up Successfull",Toast.LENGTH_SHORT).show();

                            DbQuery.createUserData(emailStr,nameStr,new MyCompleteListener(){

                                @Override
                                public void onSuccess() {
                                    DbQuery.loadData(new MyCompleteListener() {
                                        @Override
                                        public void onSuccess() {
                                            progressDialog.dismiss();
                                            Intent intent= new Intent(SignUpActivity.this,MainActivity.class);
                                            startActivity(intent);
                                            SignUpActivity.this.finish();
                                        }

                                        @Override
                                        public void onFailure() {
                                            Toast.makeText(SignUpActivity.this,"Something went wrong!Please Try Again Later!",Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    });


                                }
                                @Override
                                public void onFailure() {

                                    Toast.makeText(SignUpActivity.this,"Something went wrong!Please Try Again Later!",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}