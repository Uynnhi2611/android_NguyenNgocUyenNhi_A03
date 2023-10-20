package com.example.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.quizapp.Models.QuestionModel;

import java.util.concurrent.TimeUnit;

public class ScoreActivity extends AppCompatActivity {
    private TextView scoreTV, timeTV,totalQTV,correctQTV,wrongQTV,unattemptQTV;
    private Button btnLeader, btnReAttempt,btnViewAns;
    private long timeTaken;
    private  Dialog progressDialog;
    private TextView dialogText;
    private int finalScore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Result");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog=new Dialog(ScoreActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText=progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");
        progressDialog.show();

        init();
        loadData();
        setBookMarks();
        btnViewAns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ScoreActivity.this,AnswersActivity.class);
                startActivity(intent);
            }
        });

        btnReAttempt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reAttempt();
            }
        });

        saveResult();
    }

    private void init(){
        scoreTV=findViewById(R.id.score);
        timeTV=findViewById(R.id.time);
        totalQTV=findViewById(R.id.totalQ);
        correctQTV=findViewById(R.id.correctQ);
        wrongQTV=findViewById(R.id.wrongQ);
        unattemptQTV=findViewById(R.id.unAttemptedQ);
        btnLeader=findViewById(R.id.btnLeader);
        btnReAttempt=findViewById(R.id.btnReAttempt);
        btnViewAns=findViewById(R.id.btnViewAns);
    }
    private void loadData(){
        int correctQ=0, wrongQ=0, unattemptQ=0;
        for(int i=0;i<DbQuery.g_quesList.size();i++){
            if(DbQuery.g_quesList.get(i).getSelectedAns()==-1){
                unattemptQ++;
            }else {
                if(DbQuery.g_quesList.get(i).getSelectedAns()==DbQuery.g_quesList.get(i).getCorrectAns()){
                    correctQ++;
                }else {
                    wrongQ++;
                }
            }
        }
        correctQTV.setText(String.valueOf(correctQ));
        wrongQTV.setText(String.valueOf(wrongQ));
        unattemptQTV.setText(String.valueOf(unattemptQ));

        totalQTV.setText(String.valueOf(DbQuery.g_quesList.size()));
        finalScore=(correctQ*100)/DbQuery.g_quesList.size();//set diem cho moi cau
        scoreTV.setText(String.valueOf(finalScore));

        timeTaken= getIntent().getLongExtra("TIME_TAKEN",0);

        String time=String.format("%02d:%02d min",
                TimeUnit.MILLISECONDS.toMinutes(timeTaken),
                TimeUnit.MILLISECONDS.toSeconds(timeTaken)-
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeTaken))
        );

        timeTV.setText(time);
    }
    private void reAttempt(){
        for(int i=0;i<DbQuery.g_quesList.size();i++){
            DbQuery.g_quesList.get(i).setSelectedAns(-1);
            DbQuery.g_quesList.get(i).setStatus(DbQuery.NOT_VISITED);
        }
        Intent intent=new Intent(ScoreActivity.this,StartTestActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveResult(){
        DbQuery.saveResult(finalScore, new MyCompleteListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
            }

            @Override
            public void onFailure() {
                Toast.makeText(ScoreActivity.this,"Something went wrong! Please try again later!",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }
    private void setBookMarks(){
        for(int i=0; i<DbQuery.g_quesList.size();i++){
            QuestionModel question=DbQuery.g_quesList.get(i);
            if(question.isBookmarked())
            {
                if(! DbQuery.g_bmIdList.contains(question.getqID())){
                    DbQuery.g_bmIdList.add(question.getqID());
                    DbQuery.myProfile.setBookmarksCount(DbQuery.g_bmIdList.size());
                }
            }else {
                if(DbQuery.g_bmIdList.contains(question.getqID())){
                    DbQuery.g_bmIdList.remove(question.getqID());
                    DbQuery.myProfile.setBookmarksCount(DbQuery.g_bmIdList.size());
                }
            }
        }
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId()== android.R.id.home){
            ScoreActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }



}