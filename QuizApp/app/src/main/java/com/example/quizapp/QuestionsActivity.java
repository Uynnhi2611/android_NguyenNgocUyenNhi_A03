package com.example.quizapp;

import static com.example.quizapp.DbQuery.ANSWERED;
import static com.example.quizapp.DbQuery.NOT_VISITED;
import static com.example.quizapp.DbQuery.REVIEW;
import static com.example.quizapp.DbQuery.UNANSWERED;
import static com.example.quizapp.DbQuery.g_catList;
import static com.example.quizapp.DbQuery.g_firestore;
import static com.example.quizapp.DbQuery.g_quesList;
import static com.example.quizapp.DbQuery.g_selected_cat_index;
import static com.example.quizapp.DbQuery.g_selected_test_index;
import static com.example.quizapp.DbQuery.g_testList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizapp.Adapters.QuestionGridAdapter;
import com.example.quizapp.Adapters.QuestionsAdapter;
import com.example.quizapp.Models.QuestionModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class QuestionsActivity extends AppCompatActivity {

    private RecyclerView questionsView;
    private TextView tvQuesID, timerTV,catNameTV;
    private Button btnSubmit,btnMark,btnClearSel;
    private ImageButton btnPrevQues,btnNextQues,btnDrawerClose;
    private ImageView btnQuesList;
    private int quesID;
    QuestionsAdapter quesAdapter;
    private DrawerLayout drawer;
    private GridView quesListGV;
    private ImageView markImage;
    private QuestionGridAdapter gridAdapter;
    private CountDownTimer timer;
    private long timeLeft;
    private ImageView btnBookmark,btnVoice,btnQues_Del;
    TextToSpeech textToSpeech;
    LanguageIdentification languageIdentifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questions_list_layout);

        init();
        quesAdapter=new QuestionsAdapter(g_quesList);
        questionsView.setAdapter(quesAdapter);

        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        questionsView.setLayoutManager(layoutManager);

        gridAdapter=new QuestionGridAdapter(this,g_quesList.size());
        quesListGV.setAdapter(gridAdapter);
        setSnapHelper();

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("vi"));
                }
            }
        });

        setClickListeners();
        startTimer();
    }

    private void init(){
        questionsView=findViewById(R.id.questions_view);
        tvQuesID=findViewById(R.id.tv_quesID);
        timerTV=findViewById(R.id.tv_timer);
        catNameTV=findViewById(R.id.qa_catName);
        btnSubmit=findViewById(R.id.btnSubmit);
        btnMark=findViewById(R.id.btnMark);
        btnClearSel=findViewById(R.id.btnClear_sel);
        btnPrevQues=findViewById(R.id.btnPrev_ques);
        btnNextQues=findViewById(R.id.btnNext_ques);
        btnQuesList=findViewById(R.id.btnQues_list_grid);
        quesID=0;
        tvQuesID.setText("1/"+String.valueOf(g_quesList.size()));
        catNameTV.setText(g_catList.get(g_selected_cat_index).getName());
        drawer = findViewById(R.id.drawer_layout);
        btnDrawerClose=findViewById(R.id.btnDrawerClose);
        markImage=findViewById(R.id.mark_image);
        quesListGV=findViewById(R.id.ques_list_gv);
        btnBookmark=findViewById(R.id.btnQua_bookmark);
        if (!g_quesList.isEmpty()) {
            g_quesList.get(0).setStatus(UNANSWERED);
            if(g_quesList.get(0).isBookmarked()){
                btnBookmark.setImageResource(R.drawable.ic_bookmark_selected);
            } else {
                btnBookmark.setImageResource(R.drawable.ic_bookmark);
            }
        }
        btnVoice=findViewById(R.id.btnVoice);
        btnQues_Del=findViewById(R.id.btnQues_Del);


    }
    private void setSnapHelper(){
        SnapHelper snapHelper= new PagerSnapHelper();
        snapHelper.attachToRecyclerView(questionsView);

        questionsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                View view  = snapHelper.findSnapView(recyclerView.getLayoutManager());
                quesID= recyclerView.getLayoutManager().getPosition(view);
                if(g_quesList.get(quesID).getStatus()==NOT_VISITED)
                    g_quesList.get(quesID).setStatus(UNANSWERED);
                if(g_quesList.get(quesID).getStatus()==REVIEW){
                    markImage.setVisibility(View.VISIBLE);
                }else {
                    markImage.setVisibility(View.GONE);
                }

                tvQuesID.setText(String.valueOf(quesID+1)+"/"+String.valueOf(g_quesList.size()));

                if(g_quesList.get(quesID).isBookmarked()){
                    btnBookmark.setImageResource(R.drawable.ic_bookmark_selected);
                }else {
                    btnBookmark.setImageResource(R.drawable.ic_bookmark);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }
    private void setClickListeners(){
        btnPrevQues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(quesID>0) {
                    questionsView.smoothScrollToPosition(quesID -1);
                }
            }
        });
        btnNextQues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( quesID< g_quesList.size()-1)
                {
                    questionsView.smoothScrollToPosition(quesID+1);
                }

            }
        });

        btnClearSel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                g_quesList.get(quesID).setSelectedAns(-1);
                g_quesList.get(quesID).setStatus(UNANSWERED);
                markImage.setVisibility(View.GONE);
                quesAdapter.notifyDataSetChanged();

            }
        });

        btnQuesList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! drawer.isDrawerOpen(GravityCompat.END)){

                    gridAdapter.notifyDataSetChanged();
                    drawer.openDrawer(GravityCompat.END);
                }
            }
        });

        btnDrawerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawer.isDrawerOpen(GravityCompat.END)){
                    drawer.closeDrawer(GravityCompat.END);
                }
            }
        });

        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(markImage.getVisibility()!=View.VISIBLE){
                    markImage.setVisibility(View.VISIBLE);
                    g_quesList.get(quesID).setStatus(REVIEW);
                }else {
                    markImage.setVisibility(View.GONE);
                    if(g_quesList.get(quesID).getSelectedAns()!=-1){
                        g_quesList.get(quesID).setStatus(ANSWERED);
                    }else {
                        g_quesList.get(quesID).setStatus(UNANSWERED);
                    }
                }
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Kiểm tra xem danh sách câu hỏi còn câu hỏi nào không
                if (g_quesList.isEmpty()) {
                    // Hiển thị thông báo "Không còn câu hỏi nào"
                    Toast.makeText(QuestionsActivity.this, "There are no more questions.", Toast.LENGTH_SHORT).show();

                    // Chuyển về StartTestActivity
                    Intent intent = new Intent(QuestionsActivity.this, StartTestActivity.class);
                    startActivity(intent);
                    finish();
                } else{
                    submitTest();
                }
            }
        });
        btnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToBookmarks();
            }
        });


        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy câu hỏi và các lựa chọn từ danh sách câu hỏi
                QuestionModel currentQuestion = g_quesList.get(quesID);
                String questionText = currentQuestion.getQuestion();
                String optionA = currentQuestion.getOptionA();
                String optionB = currentQuestion.getOptionB();
                String optionC = currentQuestion.getOptionC();
                String optionD = currentQuestion.getOptionD();

                // Tạo một chuỗi bao gồm cả câu hỏi và các lựa chọn
                String textToSpeak = questionText + ". Option A: " + optionA + ". Option B: " + optionB
                        + ". Option C: " + optionC + ". Option D: " + optionD;

                // Khởi tạo ngôn ngữ nhận dạng
                LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();

                // Xác định ngôn ngữ của một chuỗi văn bản
                languageIdentifier.identifyLanguage(textToSpeak)
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (!languageCode.equals("und")) {
                                    if ("en".equals(languageCode)) {
                                        textToSpeech.setLanguage(Locale.ENGLISH);
                                    } else if ("vi".equals(languageCode)) {
                                        textToSpeech.setLanguage(new Locale("vi"));
                                    } else {
                                        // Mặc định là tiếng Anh nếu ngôn ngữ không được hỗ trợ
                                        textToSpeech.setLanguage(Locale.ENGLISH);
                                    }
                                } else {
                                    // Model không thể xác định ngôn ngữ
                                }
                                // Đọc chuỗi sau khi đã xác định và thiết lập ngôn ngữ
                                textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model không thể xác định ngôn ngữ hoặc xảy ra lỗi khác
                            }
                        });

            }
        });
        btnQues_Del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra xem danh sách câu hỏi còn câu hỏi nào không
                if (g_quesList.isEmpty()) {
                    // Hiển thị thông báo "Không còn câu hỏi nào"
                    Toast.makeText(QuestionsActivity.this, "There are no more questions.", Toast.LENGTH_SHORT).show();
                } else {
                    // Lấy ID của câu hỏi hiện tại
                    String questionId = g_quesList.get(quesID).getqID();

                    // Xóa câu hỏi từ Firestore
                    g_firestore.collection("Questions").document(questionId)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Xóa câu hỏi khỏi danh sách câu hỏi
                                    g_quesList.remove(quesID);

                                    // Thông báo cho RecyclerView.Adapter về sự thay đổi
                                    quesAdapter.notifyDataSetChanged();

                                    // Hiển thị thông báo "Câu hỏi đã bị xóa"
                                    Toast.makeText(QuestionsActivity.this, "Question has been deleted.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý lỗi, ví dụ: hiển thị thông báo lỗi
                                }
                            });
                }
            }
        });

    }

    private void submitTest(){
        AlertDialog.Builder builder=new AlertDialog.Builder(QuestionsActivity.this);
        builder.setCancelable(true);

        View view=getLayoutInflater().inflate(R.layout.alert_dialog_layout,null);
        Button btnCancel= view.findViewById(R.id.btnCancel);
        Button btnConfirm=view.findViewById(R.id.btnConfirm);

        builder.setView(view);
        AlertDialog alertDialog=builder.create();
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                alertDialog.dismiss();
                Intent intent=new Intent(QuestionsActivity.this, ScoreActivity.class);
                long totalTime=g_testList.get(g_selected_test_index).getTime()*60*1000;
                intent.putExtra("TIME_TAKEN",totalTime - timeLeft);
                startActivity(intent);
                QuestionsActivity.this.finish();
            }
        });

        alertDialog.show();

    }
    public void goToQuestion(int position){

        questionsView.smoothScrollToPosition(position);

        if(drawer.isDrawerOpen(GravityCompat.END)){
            drawer.closeDrawer(GravityCompat.END);
        }
    }
    private void startTimer(){
        long totalTime= g_testList.get(g_selected_test_index).getTime()*60*1000;
        timer=new CountDownTimer(totalTime+1000,1000) {
            @Override
            public void onTick(long remainingTime) {

                timeLeft=remainingTime;
                String time=String.format("%02d:%02d min",
                        TimeUnit.MILLISECONDS.toMinutes(remainingTime),
                        TimeUnit.MILLISECONDS.toSeconds(remainingTime)-
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingTime))
                        );
                timerTV.setText(time);
            }

            @Override
            public void onFinish() {
                Intent intent=new Intent(QuestionsActivity.this, ScoreActivity.class);
                long totalTime=g_testList.get(g_selected_test_index).getTime()*60*1000;
                intent.putExtra("TIME_TAKEN",totalTime-timeLeft);
                startActivity(intent);
                QuestionsActivity.this.finish();
            }
        };
        timer.start();
    }
    private void addToBookmarks(){
        if(g_quesList.get(quesID).isBookmarked()){
            g_quesList.get(quesID).setBookmarked(false);
            btnBookmark.setImageResource(R.drawable.ic_bookmark);
        }else {
            g_quesList.get(quesID).setBookmarked(true);
            btnBookmark.setImageResource(R.drawable.ic_bookmark_selected);
        }
    }
}