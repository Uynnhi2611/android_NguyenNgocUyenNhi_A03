package com.example.quizapp;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import static com.example.quizapp.DbQuery.createTest;
import static com.example.quizapp.DbQuery.g_firestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizapp.Adapters.CategoryAdapter;
import com.example.quizapp.Adapters.TestAdapter;
import com.example.quizapp.Models.CategoryModel;
import com.example.quizapp.Models.TestModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.checkerframework.checker.nullness.qual.Nullable;

public class TestActivity extends AppCompatActivity  {

    private RecyclerView testView;
    private Toolbar toolbar;
    private TestAdapter adapter;
    private Dialog progressDialog;
    private TextView dialogText;
    private ImageView btnAdd,btnDel;
    private DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        getSupportActionBar().setTitle(DbQuery.g_catList.get(DbQuery.g_selected_cat_index).getName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        testView = findViewById(R.id.test_recycler_view);


        progressDialog = new Dialog(TestActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");

        progressDialog.show();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        testView.setLayoutManager(layoutManager);


        DbQuery.loadTestData(new MyCompleteListener() {
            @Override
            public void onSuccess() {

                DbQuery.loadMyScores(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        adapter = new TestAdapter(DbQuery.g_testList);
                        testView.setAdapter(adapter);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure() {
                        progressDialog.dismiss();
                        Toast.makeText(TestActivity.this, "Something went wrong!Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure() {
                progressDialog.dismiss();
                Toast.makeText(TestActivity.this, "Something went wrong!Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTest();
            }
        });
        // Tìm nút xóa trong layout
        btnDel = findViewById(R.id.btnDelete);

        // Đặt sự kiện click cho nút xóa
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTest();
            }
        });

    }
    private void addTest(){
        AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        // Sử dụng LayoutInflater để chuyển đổi add_test_dialog_layout thành View
        View dialogLayout = inflater.inflate(R.layout.add_test_dialog_layout, null);
        // Đặt dialogLayout làm layout cho AlertDialog
        builder.setView(dialogLayout);
        // Tạo AlertDialog
        AlertDialog alertDialog = builder.create();
        // Tìm btnUpload trong dialogLayout
        Button btnUpload = dialogLayout.findViewById(R.id.btnUpload);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy dữ liệu từ EditTexts
                String testId = ((EditText) dialogLayout.findViewById(R.id.inputTest_ID)).getText().toString();
                int testTime = Integer.parseInt(((EditText) dialogLayout.findViewById(R.id.inputTest_Time)).getText().toString());

                // Tạo một TestModel mới với dữ liệu đã lấy
                TestModel newTest = new TestModel(testId, 0, testTime);
                DbQuery.g_testList.add(newTest);
                // Gọi hàm createTest để tải dữ liệu lên Firebase
                createTest(DbQuery.g_current_cat_id, newTest, new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(TestActivity.this, "Test added successfully!", Toast.LENGTH_SHORT).show();
                        // Cập nhật noOfTests trong g_catList
                        CategoryModel category = DbQuery.g_catList.get(DbQuery.g_selected_cat_index);
                        category.setNoOfTests(category.getNoOfTests() + 1);

                        // Gọi notifyDataSetChanged() trên adapter của g_catList

                        DocumentReference categoryDoc = g_firestore.collection("QUIZ").document(DbQuery.g_current_cat_id);
                        categoryDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "Listen failed.", e);
                                    return;
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    Long noOfTests = snapshot.getLong("NO_OF_TESTS");
                                    // Cập nhật g_testList và giao diện người dùng của bạn ở đây

                                    adapter.notifyDataSetChanged();

                                } else {
                                    Log.d(TAG, "Current data: null");
                                }
                            }
                        });

                    }
                    @Override
                    public void onFailure() {
                        Toast.makeText(TestActivity.this, "Failed to add test.", Toast.LENGTH_SHORT).show();
                    }
                });
                // Đóng AlertDialog sau khi tải dữ liệu lên Firebase
                alertDialog.dismiss();
            }
        });

        // Hiển thị AlertDialog
        alertDialog.show();
    }
    private void deleteTest(){
        // Kiểm tra xem danh sách có rỗng hay không
        if (DbQuery.g_testList.isEmpty()) {
            Toast.makeText(TestActivity.this, "There are no tests! Please add another post.", Toast.LENGTH_SHORT).show();
        } else {
            // Tạo một hộp thoại mới
            AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
            builder.setTitle("Select a Test to Delete");

            // Tạo một danh sách các TEST_ID
            String[] testIds = new String[DbQuery.g_testList.size()];
            for (int i = 0; i < DbQuery.g_testList.size(); i++) {
                testIds[i] = DbQuery.g_testList.get(i).getTestID();
            }

            builder.setItems(testIds, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Xóa bài kiểm tra khi một TEST_ID được chọn
                    DbQuery.deleteTest(which + 1, new MyCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(TestActivity.this, "Delete Successful!", Toast.LENGTH_SHORT).show();
                            DbQuery.updateTestIndices(which + 1, new MyCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    // Cập nhật UI sau khi xóa thành công
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(TestActivity.this, "Update Successful!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure() {
                                    Toast.makeText(TestActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(TestActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            // Hiển thị hộp thoại
            builder.show();
        }
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId()== android.R.id.home){
            TestActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}