package com.example.quizapp;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.quizapp.Adapters.CategoryAdapter;
import com.example.quizapp.Models.CategoryModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CategoryFragment extends Fragment {

    public CategoryFragment() {
        // Required empty public constructor
    }

    private GridView catView;
    private ImageView btnAdd,btnDel,btnReset;
    private CategoryAdapter adapter = new CategoryAdapter(DbQuery.g_catList);

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_category, container, false);
        Toolbar toolbar =getActivity().findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Categories");
        catView=view.findViewById(R.id.cat_Grid);

        btnAdd=view.findViewById(R.id.btnAdd);
      //  btnDel= view.findViewById(R.id.btnDel);
        // loadCategories();
        CategoryAdapter adapter=new CategoryAdapter(DbQuery.g_catList);
        catView.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });

        btnReset = view.findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbQuery.loadCategories(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        // Update your adapter and notify it about the data change
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getActivity(), "Something went wrong! Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        return  view;


    }
    private Button btnUpload;
    EditText inputCategoryName;
    private void addCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        View view = getLayoutInflater().inflate(R.layout.add_category_dialog_layout, null);
        btnUpload = view.findViewById(R.id.btnUpload);
        inputCategoryName = view.findViewById(R.id.inputCategoryName);

        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = inputCategoryName.getText().toString();
                if (!categoryName.isEmpty()) {
                    int noOfTest = 0; // Mặc định là 0
                    // Tạo một đối tượng CategoryModel mới
                    CategoryModel newCategory = new CategoryModel("", categoryName, noOfTest);
                    // Thêm đối tượng này vào danh sách danh mục
                    DbQuery.g_catList.add(newCategory);
                    // Cập nhật dữ liệu trên Firebase
                    DbQuery.createCategory(newCategory, new MyCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(view.getContext(), "Add Successful!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(view.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(view.getContext(), "Enter category name!", Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}