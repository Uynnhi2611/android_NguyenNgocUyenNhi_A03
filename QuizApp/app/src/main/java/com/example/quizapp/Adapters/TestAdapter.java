package com.example.quizapp.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.DbQuery;
import com.example.quizapp.ManageQuesActivity;
import com.example.quizapp.ManageStartTestActivity;
import com.example.quizapp.ManageTestActivity;
import com.example.quizapp.R;
import com.example.quizapp.StartTestActivity;
import com.example.quizapp.Models.TestModel;

import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {
    private List<TestModel> testList;

    public TestAdapter(List<TestModel> testList) {
        this.testList = testList;
    }

    @NonNull
    @Override
    public TestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestAdapter.ViewHolder holder, int position) {
        int progress=testList.get(position).getTopScore();
        holder.setData(position,progress);
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView testNo,topScore;
        private ProgressBar progressBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            testNo= itemView.findViewById(R.id.testNo);
            topScore=itemView.findViewById(R.id.scoretext);
            progressBar=itemView.findViewById(R.id.testProgressBar);


        }
        private void setData(int pos,int progress ){
            testNo.setText("Test No: "+ String.valueOf(pos+1));
            topScore.setText(String.valueOf(progress)+"%");
            progressBar.setProgress(progress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DbQuery.g_selected_test_index = pos;

                    Intent intent;
                    if (view.getContext() instanceof ManageTestActivity) {
                        intent = new Intent(view.getContext(), ManageStartTestActivity.class);
                    } else {
                        intent = new Intent(view.getContext(), StartTestActivity.class);
                    }
                    view.getContext().startActivity(intent);
                }
            });


        }
    }
}
