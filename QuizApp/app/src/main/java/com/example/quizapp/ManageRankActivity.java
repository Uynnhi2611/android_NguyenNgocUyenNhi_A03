package com.example.quizapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.Adapters.RankAdapter;

public class ManageRankActivity extends AppCompatActivity {

    private TextView totalUsersTV, myImgTextTV, myScoreTV,myRankTV;
    private RecyclerView usersView;
    private RankAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_rank);

        initViews();

        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        usersView.setLayoutManager(layoutManager);
        adapter=new RankAdapter(DbQuery.g_usersList);
        usersView.setAdapter(adapter);

        DbQuery.getRank(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure() {
                Toast.makeText(ManageRankActivity.this,"Something went wrong! Please try again.",Toast.LENGTH_SHORT).show();
            }
        });

        totalUsersTV.setText("Total Users: "+ DbQuery.g_usersCount);
    }

    private void initViews(){
        totalUsersTV= findViewById(R.id.total_users);
        myImgTextTV=findViewById(R.id.img_text);
        myScoreTV=findViewById(R.id.total_score);
        myRankTV=findViewById(R.id.rank);
        usersView=findViewById(R.id.users_view);
    }
}
