package com.example.quizapp;

import static com.example.quizapp.DbQuery.createUserData;
import static com.example.quizapp.DbQuery.g_catList;
import static com.example.quizapp.DbQuery.loadquestions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.ViewTransitionController;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizapp.Models.QuestionModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartTestActivity extends AppCompatActivity {

    private TextView catName,testNo,totalQ,bestScore,time;
    private Button btnStart;
    private ImageView btnBack,btnAdd,btnReset;
    private Dialog progressDialog;
    private TextView dialogText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_test);

        init();

        progressDialog=new Dialog(StartTestActivity.this);
        progressDialog.setContentView(R.layout.dialog_layout);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText=progressDialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading...");

        loadquestions(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                setData();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure() {
                progressDialog.dismiss();
                Toast.makeText(StartTestActivity.this, "Something went wrong!Please try again.",
                        Toast.LENGTH_SHORT).show();

            }
        });

    }
    private void init(){
        catName=findViewById(R.id.st_cat_name);
        testNo=findViewById(R.id.st_test_no);
        totalQ=findViewById(R.id.st_total_ques);
        bestScore=findViewById(R.id.st_best_score);
        time=findViewById(R.id.st_time);
        btnStart=findViewById(R.id.btnStart_test);
        btnBack=findViewById(R.id.btnBack);
        btnAdd=findViewById(R.id.btnAdd);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartTestActivity.this.finish();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DbQuery.g_quesList.isEmpty()) {
                    // No questions were loaded, show a message to the user
                    Toast.makeText(StartTestActivity.this, "There are no questions yet, please click the Add Question button to add more", Toast.LENGTH_LONG).show();
                } else {
                    // Questions were loaded successfully, navigate to QuestionsActivity
                    Intent intent = new Intent(StartTestActivity.this, QuestionsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuestion();
            }
        });

        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadquestions(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        setData();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure() {
                        progressDialog.dismiss();
                        Toast.makeText(StartTestActivity.this, "Something went wrong!Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    private void setData(){
        catName.setText(g_catList.get(DbQuery.g_selected_cat_index).getName());
        testNo.setText("Test No. " + String.valueOf(DbQuery.g_selected_test_index+1));
        totalQ.setText(String.valueOf(DbQuery.g_quesList.size()));
        bestScore.setText(String.valueOf(DbQuery.g_testList.get(DbQuery.g_selected_test_index).getTopScore()));
        time.setText(String.valueOf(DbQuery.g_testList.get(DbQuery.g_selected_test_index).getTime()));
    }

    private void addQuestion () {
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setCancelable (true);

        View view = getLayoutInflater ().inflate (R.layout.add_ques_dialog_layout, null);
        Button btnUpload = view.findViewById (R.id.btnUpload);
        EditText inputQuestion = view.findViewById (R.id.inputQues);
        EditText inputAnswer = view.findViewById (R.id.inputAns);
        EditText inputA = view.findViewById (R.id.inputOptionA);
        EditText inputB = view.findViewById (R.id.inputOptionB);
        EditText inputC = view.findViewById (R.id.inputOptionC);
        EditText inputD = view.findViewById (R.id.inputOptionD);

        builder.setView (view);
        AlertDialog alertDialog = builder.create ();

        btnUpload.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                String question = inputQuestion.getText ().toString ();
                String answer = inputAnswer.getText ().toString ();
                String a = inputA.getText ().toString ();
                String b = inputB.getText ().toString ();
                String c = inputC.getText ().toString ();
                String d = inputD.getText ().toString ();
                if (!question.isEmpty () && !answer.isEmpty () && !a.isEmpty () && !b.isEmpty () && !c.isEmpty () && !d.isEmpty ()) {
                    int answerNum = Integer.parseInt (answer); // Chuyển đổi chuỗi thành số nguyên
                    // Tạo một Map để lưu trữ dữ liệu của câu hỏi mới
                    Map<String, Object> questionData = new HashMap<>();
                    questionData.put ("QUESTION", question);
                    questionData.put ("ANSWER", answerNum);
                    questionData.put ("A", a);
                    questionData.put ("B", b);
                    questionData.put ("C", c);
                    questionData.put ("D", d);
                    questionData.put ("CATEGORY", DbQuery.g_current_cat_id);

                    // Lấy giá trị của field TEST[DbQuery.g_selected_test_index ]_ID từ document TESTS_INFO
                    DocumentReference docRef = DbQuery.g_firestore.collection ("QUIZ").document (DbQuery.g_current_cat_id).collection ("TESTS_LIST").document ("TESTS_INFO");
                    docRef.get ()
                            .addOnCompleteListener (new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete (@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful ()) {
                                        DocumentSnapshot document = task.getResult ();
                                        if (document.exists ()) {
                                            // Lấy giá trị của field và gán cho TEST trong questionData
                                            String testValue = document.getString ("TEST" + (DbQuery.g_selected_test_index+1) + "_ID");
                                            questionData.put ("TEST", testValue);

                                            // Tạo một document mới với id ngẫu nhiên trong collection "Questions" của Firestore
                                            DocumentReference newDoc = DbQuery.g_firestore.collection ("Questions").document ();

                                            // Thêm câu hỏi mới vào Firestore
                                            newDoc.set (questionData)
                                                    .addOnSuccessListener (new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess (Void aVoid) {
                                                            Toast.makeText(view.getContext(), "Thêm câu hỏi thành công!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener (new OnFailureListener() {
                                                        @Override
                                                        public void onFailure (@NonNull Exception e) {
                                                            Toast.makeText (view.getContext (), "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show ();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText (view.getContext (), "Không tìm thấy document TESTS_INFO", Toast.LENGTH_SHORT).show ();
                                        }
                                    } else {
                                        Toast.makeText (view.getContext (), "Không thể truy vấn dữ liệu từ Firestore", Toast.LENGTH_SHORT).show ();
                                    }
                                }
                            });
                } else {
                    Toast.makeText (view.getContext (), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show ();
                }
                alertDialog.dismiss ();
            }
        });

        alertDialog.show ();
    }

}