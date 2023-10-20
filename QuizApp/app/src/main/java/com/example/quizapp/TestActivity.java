package com.example.quizapp;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizapp.Adapters.TestAdapter;
import com.example.quizapp.Models.CategoryModel;
import com.example.quizapp.Models.TestModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.checkerframework.checker.nullness.qual.Nullable;

public class TestActivity extends AppCompatActivity {

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

        adapter = new TestAdapter(DbQuery.g_testList);


        //loadTestData();
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

        // Khởi tạo adapter và docRef
        adapter = new TestAdapter(DbQuery.g_testList);
        docRef = DbQuery.g_firestore.collection("QUIZ").document(DbQuery.g_current_cat_id).collection("TESTS_LIST").document("TESTS_INFO");

        // Thêm lắng nghe viên cho Firestore
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // Cập nhật danh sách bài kiểm tra với dữ liệu mới từ snapshot
                    // TODO: Thêm mã để phân tích dữ liệu từ snapshot và cập nhật DbQuery.g_testList
                    // Thông báo cho adapter về sự thay đổi dữ liệu
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTest();
            }
        });
    }

      /*  btnDel = findViewById(R.id.btnDel);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị hộp thoại cho phép người dùng chọn bài kiểm tra để xóa
                AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                builder.setTitle("Select a test to delete");

                // Tạo một mảng chứa tên của tất cả các bài kiểm tra
                String[] testNames = new String[DbQuery.g_testList.size()];
                for (int i = 0; i < DbQuery.g_testList.size(); i++) {
                    testNames[i] = DbQuery.g_testList.get(i).getTestID();
                }

                builder.setItems(testNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 'which' là chỉ số của bài kiểm tra được chọn
                        DbQuery.g_testList.remove(which);

                        // Cập nhật dữ liệu trên Firebase
                        DbQuery.deleteTest(which, new MyCompleteListener() {
                            @Override
                            public void onSuccess() {
                                // Cập nhật adapter để phản ánh sự thay đổi trong danh sách bài kiểm tra
                                adapter.notifyItemRemoved(which);

                                Toast.makeText(TestActivity.this, "Test deleted successfully.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(TestActivity.this, "Something went wrong! Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                // Hiển thị hộp thoại
                builder.show();
            }
        });
    }*/

    private void addTest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        View view = getLayoutInflater().inflate(R.layout.add_test_dialog_layout, null);
        Button btnUpload = view.findViewById(R.id.btnUpload);
        EditText inputTestID = view.findViewById(R.id.inputTest_ID);
        EditText inputTestTime = view.findViewById(R.id.inputTest_Time);

        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String testID = inputTestID.getText().toString();
                String testTimeStr = inputTestTime.getText().toString();
                if (!testID.isEmpty() && !testTimeStr.isEmpty()) {
                    int testTime = Integer.parseInt(testTimeStr); // Chuyển đổi chuỗi thành số nguyên
                    // Tạo một đối tượng TestModel mới
                    TestModel newTest = new TestModel(testID, 0, testTime);
                    // Thêm đối tượng này vào danh sách test của category hiện tại
                    DbQuery.g_testList.add(newTest);
                    // Cập nhật dữ liệu trên Firebase
                    DbQuery.createTest(newTest, DbQuery.g_testList.size(), new MyCompleteListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(view.getContext(), "Add Successful!", Toast.LENGTH_SHORT).show();
                            DocumentReference docRef = db.collection("QUIZ").document(DbQuery.g_current_cat_id);
                            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                    @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen failed.", e);
                                        return;
                                    }

                                    if (snapshot != null && snapshot.exists()) {
                                        Log.d(TAG, "Current data: " + snapshot.getData());
                                        // Cập nhật UI tại đây
                                        CategoryModel category = DbQuery.g_catList.get(DbQuery.g_selected_cat_index);
                                        category.setNoOfTests(category.getNoOfTests() + 1);
                                        adapter.notifyDataSetChanged();
                                    }
                                    else {
                                        Log.d(TAG, "Current data: null");
                                    }
                                }
                            });

                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(view.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(view.getContext(), "Enter test ID and time!", Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId()== android.R.id.home){
            TestActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}