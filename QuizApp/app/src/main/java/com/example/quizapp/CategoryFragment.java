package com.example.quizapp;

import static com.example.quizapp.DbQuery.deleteCategory;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

        adapter=new CategoryAdapter(DbQuery.g_catList);
        catView.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });

        btnDel=view.findViewById(R.id.btnDelete);
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

        btnDel=view.findViewById(R.id.btnDelete);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCatgory();
            }
        });

        return  view;

    }
    private Button btnUpload;
    EditText inputCategoryName;

    public CategoryFragment() {
        // Required empty public constructor
    }
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
                            adapter.notifyDataSetChanged(); // Thông báo cho adapter về sự thay đổi
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
    private void deleteCatgory(){
        // Kiểm tra xem danh sách có rỗng hay không
        if (DbQuery.g_catList.isEmpty()) {
            Toast.makeText(getContext(), "Không có danh mục nào! Vui lòng thêm một danh mục.", Toast.LENGTH_SHORT).show();
        } else {
            // Tạo một hộp thoại mới
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Chọn một danh mục để xóa");

            // Tạo một danh sách các CAT_NAME
            String[] catNames = new String[DbQuery.g_catList.size()];
            for (int i = 0; i < DbQuery.g_catList.size(); i++) {
                catNames[i] = DbQuery.g_catList.get(i).getName();
            }

            builder.setItems(catNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Xóa danh mục khi một CAT_NAME được chọn
                    deleteCategory(DbQuery.g_catList.get(which).getDocID(), new MyCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
                            // Cập nhật UI sau khi xóa thành công
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(getContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            // Hiển thị hộp thoại
            builder.show();
        }
    }
    
}