package com.example.quizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.quizapp.Adapters.AnswersAdapter;
import com.example.quizapp.Adapters.BookmarksAdapter;

public class BookmarksActivity extends AppCompatActivity {

    private RecyclerView questionsView;
    private Dialog progressDialog;
    private TextView dialogText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        Toolbar toolbar =findViewById(R.id.bm_toolbar);
        questionsView=findViewById(R.id.bm_recycler_view);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Saves Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog=new Dialog(BookmarksActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogText=progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");
        progressDialog.show();

        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        questionsView.setLayoutManager(layoutManager);

        DbQuery.loadBookmarks(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                BookmarksAdapter adapter=new BookmarksAdapter(DbQuery.g_bookmarksList);
                questionsView.setAdapter(adapter);

                progressDialog.dismiss();
            }

            @Override
            public void onFailure() {
                progressDialog.dismiss();
            }
        });


    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId()== android.R.id.home){
            BookmarksActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
