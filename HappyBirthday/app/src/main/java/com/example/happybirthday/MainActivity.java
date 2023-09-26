package com.example.happybirthday;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView greetingText = findViewById(R.id.greeting_text);
        TextView signatureText = findViewById(R.id.signature_text);

        String happyBirthdayText = getResources().getString(R.string.happy_birthday_text);
        String signatureTextString = getResources().getString(R.string.signature_text);

        greetingText.setText(happyBirthdayText);
        signatureText.setText(signatureTextString);
    }
}

