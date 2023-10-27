package com.example.quizapp.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.quizapp.ManageQuesActivity;
import com.example.quizapp.ManageTestActivity;
import com.example.quizapp.Models.CategoryModel;
import com.example.quizapp.DbQuery;
import com.example.quizapp.R;
import com.example.quizapp.TestActivity;

import java.util.List;

public class CategoryAdapter extends BaseAdapter {

    private List<CategoryModel> cat_list;

    public CategoryAdapter(List<CategoryModel> cat_list){
        this.cat_list=cat_list;
    }

    @Override
    public int getCount() {
        return cat_list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View myView;
        if(view== null){
            myView= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cat_item_layout,viewGroup,false);
        }else{
            myView=view;
        }

       /* myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DbQuery.g_selected_cat_index = i;
                DbQuery.g_current_cat_id = cat_list.get(i).getDocID(); // Cập nhật ID category hiện tại
                Intent intent = new Intent(view.getContext(), TestActivity.class);
                view.getContext().startActivity(intent);
            }
        });*/
        myView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DbQuery.g_selected_cat_index = i;
                DbQuery.g_current_cat_id = cat_list.get(i).getDocID(); // Cập nhật ID category hiện tại

                Intent intent;
                if (view.getContext() instanceof ManageQuesActivity) {
                    intent = new Intent(view.getContext(), ManageTestActivity.class);
                } else {
                    intent = new Intent(view.getContext(), TestActivity.class);
                }
                view.getContext().startActivity(intent);
            }
        });




        TextView catName= myView.findViewById(R.id.catName);
        TextView noOfTests=myView.findViewById(R.id.no_of_tests);

        catName.setText(cat_list.get(i).getName());
        noOfTests.setText(String.valueOf(cat_list.get(i).getNoOfTests())+"Tests");

        return myView;
    }
}
