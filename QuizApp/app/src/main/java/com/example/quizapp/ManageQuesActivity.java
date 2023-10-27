package com.example.quizapp;

import static com.example.quizapp.DbQuery.deleteCategory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quizapp.Adapters.CategoryAdapter;
import com.example.quizapp.Models.CategoryModel;

public class ManageQuesActivity extends AppCompatActivity {

    private GridView catView;
    private ImageView btnBack;

    private ImageView btnAdd,btnDel,btnReset;
    private Button btnUpload;
    EditText inputCategoryName;
    private CategoryAdapter adapter = new CategoryAdapter(DbQuery.g_catList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ques);

        catView=findViewById(R.id.cat_Grid);

        adapter=new CategoryAdapter(DbQuery.g_catList);
        catView.setAdapter(adapter);

        btnBack=findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageQuesActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnAdd=findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });

        btnDel=findViewById(R.id.btnDelete);
        btnReset = findViewById(R.id.btnReset);
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
                        Toast.makeText(ManageQuesActivity.this, "Something went wrong! Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnDel=findViewById(R.id.btnDelete);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCatgory();
            }
        });
    }

    private void addCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ManageQuesActivity.this);
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
            Toast.makeText(ManageQuesActivity.this, "There are no categories! Please add a category.", Toast.LENGTH_SHORT).show();
        } else {
            // Tạo một hộp thoại mới
            AlertDialog.Builder builder = new AlertDialog.Builder(ManageQuesActivity.this);
            builder.setTitle("Select a category to delete.");

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
                            Toast.makeText(ManageQuesActivity.this, "Delete Successful!", Toast.LENGTH_SHORT).show();
                            // Cập nhật UI sau khi xóa thành công
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(ManageQuesActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            // Hiển thị hộp thoại
            builder.show();
        }
    }
}