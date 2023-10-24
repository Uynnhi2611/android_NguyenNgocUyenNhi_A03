package com.example.quizapp;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.graphics.BitmapFactory;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.quizapp.Models.CategoryModel;
import com.example.quizapp.Models.ProfileModel;
import com.example.quizapp.Models.QuestionModel;
import com.example.quizapp.Models.RankModel;
import com.example.quizapp.Models.TestModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbQuery {
    public static FirebaseFirestore g_firestore;
    public static List<CategoryModel> g_catList=new ArrayList<>();
    public static int g_selected_cat_index=0;
    public static List<TestModel> g_testList=new ArrayList<>();
    public static int g_selected_test_index=0;
    public static List<String> g_bmIdList=new ArrayList<>();
    public static List<QuestionModel> g_bookmarksList=new ArrayList<>();
    public static List<QuestionModel> g_quesList=new ArrayList<>();
    public static List<RankModel> g_usersList = new ArrayList<>();
    public static int g_usersCount=0;
    public static boolean isMeOnTopList=false;
    public static ProfileModel myProfile=new ProfileModel("NA",null,null,0);
    public static RankModel myPerformance= new RankModel("NULL",0,-1);
    public static final int NOT_VISITED=0;
    public static final int UNANSWERED=1;
    public static final int ANSWERED=2;
    public static final int REVIEW=3;
    static int tmp;
    public static String g_current_cat_id;

    public  static void createUserData(String email,String name,MyCompleteListener completeListener){
        Map<String,Object> userData=new ArrayMap<>();
        userData.put("EMAIL_ID",email);
        userData.put("NAME",name);
        userData.put("TOTAL_SCORE",0);
        userData.put("BOOKMARKS",0);

        DocumentReference userDoc=g_firestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        WriteBatch batch=g_firestore.batch();
        batch.set(userDoc,userData);

        DocumentReference countDoc=g_firestore.collection("USERS").document("TOTAL_USERS");
        batch.update(countDoc,"COUNT", FieldValue.increment(1));

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        completeListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });
    }

    public static void saveProfileDate(String name,String phone,MyCompleteListener completeListener){
        Map<String,Object> profileData=new ArrayMap<>();

        profileData.put("NAME",name);
        if(phone != null)
            profileData.put("PHONE",phone);
        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .update(profileData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        myProfile.setName(name);
                        if(phone != null)
                            myProfile.setPhone(phone);

                        completeListener.onSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });
    }
    public static void getUserData(MyCompleteListener completeListener){
        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        myProfile.setName(documentSnapshot.getString("NAME"));
                        myProfile.setEmail(documentSnapshot.getString("EMAIL_ID"));
                        if(documentSnapshot.getString("PHONE")!= null)
                            myProfile.setPhone(documentSnapshot.getString("PHONE"));
                        if(documentSnapshot.get("BOOKMARKS")!= null)
                            myProfile.setBookmarksCount(documentSnapshot.getLong("BOOKMARKS").intValue());
                        myPerformance.setScore(documentSnapshot.getLong("TOTAL_SCORE").intValue());
                        myPerformance.setName(documentSnapshot.getString("NAME"));
                        completeListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });
    }
    public static void  loadMyScores(MyCompleteListener completeListener){
        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA").document("MY_SCORES")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        for(int i=0; i<g_testList.size();i++){
                            int top=0;
                            if(documentSnapshot.get(g_testList.get(i).getTestID()) != null ){
                                top=documentSnapshot.getLong(g_testList.get(i).getTestID()).intValue();
                            }
                            g_testList.get(i).setTopScore(top);
                        }
                        completeListener.onSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();

                    }
                });
    }

    public static void loadBmIds(MyCompleteListener completeListener){
        g_bmIdList.clear();
        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA").document("BOOKMARKS")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        int count=myProfile.getBookmarksCount();

                        for(int i=0;i< count;i++){
                            String bmID=documentSnapshot.getString("BM"+ String.valueOf(i+1)+"_ID");
                            g_bmIdList.add(bmID);
                        }
                        completeListener.onSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });

    }
    public static void getTopUsers(MyCompleteListener completeListener){
        g_usersList.clear();
        String myUID=FirebaseAuth.getInstance().getUid();
        g_firestore.collection("USERS")
                .whereGreaterThan("TOTAL_SCORE",0)
                .orderBy("TOTAL_SCORE", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        int rank=1;
                        for(QueryDocumentSnapshot doc: queryDocumentSnapshots){

                            g_usersList.add(new RankModel(
                                    doc.getString("NAME"),
                                    doc.getLong("TOTAL_SCORE").intValue(),
                                    rank
                            ));

                            if(myUID.compareTo(doc.getId())==0){
                                isMeOnTopList=true;
                                myPerformance.setRank(rank);
                            }
                            rank++;
                        }

                        completeListener.onSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });
    }
    public static void loadBookmarks(MyCompleteListener completeListener){
        g_bookmarksList.clear();
        tmp=0;

        if(g_bmIdList.size()==0){
            completeListener.onSuccess();
        }

        for(int i=0;i<g_bmIdList.size();i++){
            String docID = g_bmIdList.get(i);

            g_firestore.collection("Questions").document(docID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){
                                g_bookmarksList.add(new QuestionModel(
                                        documentSnapshot.getId(),
                                        documentSnapshot.getString("QUESTION"),
                                        documentSnapshot.getString("A"),
                                        documentSnapshot.getString("B"),
                                        documentSnapshot.getString("C"),
                                        documentSnapshot.getString("D"),
                                        documentSnapshot.getLong("ANSWER").intValue(),
                                        0,
                                            -1,
                                            false
                                ));
                            }
                            tmp++;
                            if(tmp==g_bmIdList.size()){
                                completeListener.onSuccess();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            completeListener.onFailure();
                        }
                    });
        }
    }
    public static void getUsersCount(MyCompleteListener completeListener){
        g_firestore.collection("USERS").document("TOTAL_USERS")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        g_usersCount=documentSnapshot.getLong("COUNT").intValue();

                        completeListener.onSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });

    }
    public static void saveResult(int score,MyCompleteListener completeListener){
        WriteBatch batch=g_firestore.batch();
        //Bookmarks
        Map<String,Object> bmData=new ArrayMap<>();
        for(int i=0; i<g_bmIdList.size();i++){
            bmData.put("BM"+String.valueOf(i+1)+"_ID",g_bmIdList.get(i));
        }
        DocumentReference bmDoc=g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA").document("BOOKMARKS");

        batch.set(bmDoc,bmData);

        DocumentReference userDoc=g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid());
        Map<String,Object> userData=new ArrayMap<>();
        userData.put("TOTAL_SCORE",score);
        userData.put("BOOKMARKS",g_bmIdList.size());

        batch.update(userDoc,userData);


        if(score>g_testList.get(g_selected_test_index).getTopScore()){
            DocumentReference scoreDoc=userDoc.collection("USER_DATA").document("MY_SCORES");
            Map<String,Object> testData=new ArrayMap<>();
            testData.put(g_testList.get(g_selected_test_index).getTestID(),score);
            batch.set(scoreDoc,testData, SetOptions.merge());
        }
        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if(score> g_testList.get(g_selected_test_index).getTopScore())
                            g_testList.get(g_selected_test_index).setTopScore(score);

                        myPerformance.setScore(score);
                        completeListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });

    }
    public static void loadCategories(MyCompleteListener completeListener){
        g_catList.clear();
        g_firestore.collection("QUIZ").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, QueryDocumentSnapshot> docList= new ArrayMap<>();
                        for(QueryDocumentSnapshot doc:queryDocumentSnapshots){
                            docList.put(doc.getId(),doc);
                        }
                        QueryDocumentSnapshot catListDooc= docList.get("Categories");

                        long catCount=catListDooc.getLong("COUNT");


                        for(int i=1;i<= catCount;i++){
                            String catID=catListDooc.getString("CAT"+ String.valueOf(i)+"_ID");
                            QueryDocumentSnapshot catDoc= docList.get(catID);
                            /*Long noOfTestsLong = catDoc.getLong("NO_OF_TESTS");
                            int noOfTest = noOfTestsLong != null ? noOfTestsLong.intValue() : 0;*/
                            Long noOfTestsLong = catDoc.getLong("NO_OF_TESTS");
                            int noOfTest = 0;
                            if (noOfTestsLong != null) {
                                noOfTest = noOfTestsLong.intValue();
                            }
                            String catName=catDoc.getString("NAME");
                            g_catList.add(new CategoryModel(catID,catName,noOfTest));
                        }
                        completeListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        completeListener.onFailure();
                    }
                });
    }
    public static void  loadquestions(MyCompleteListener completeListener)
    {
        g_quesList.clear();
        g_firestore.collection("Questions")
                .whereEqualTo("CATEGORY",g_catList.get(g_selected_cat_index).getDocID())
                .whereEqualTo("TEST",g_testList.get(g_selected_test_index).getTestID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots){
                            boolean isBookmarked=false;
                            if(g_bmIdList.contains(doc.getId()))
                                isBookmarked=true;
                            g_quesList.add(new QuestionModel(
                                    doc.getId(),
                                    doc.getString("QUESTION"),
                                    doc.getString("A"),
                                    doc.getString("B"),
                                    doc.getString("C"),
                                    doc.getString("D"),
                                    doc.getLong("ANSWER").intValue(),
                                    -1,
                                    NOT_VISITED,
                                    isBookmarked
                            ));
                        }
                        completeListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });

    }
    public static void loadTestData(MyCompleteListener completeListener)
    {
        g_testList.clear();
        g_firestore.collection("QUIZ").document(g_catList.get(g_selected_cat_index).getDocID())
                .collection("TESTS_LIST").document("TESTS_INFO")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        int noOfTests=g_catList.get(g_selected_cat_index).getNoOfTests();
                        for(int i=1;i<=noOfTests;i++){
                            g_testList.add(new TestModel(
                                documentSnapshot.getString("TEST"+String.valueOf(i)+"_ID"),0,
                                    documentSnapshot.getLong("TEST"+String.valueOf(i)+"_TIME").intValue()
                            ));
                        }
                        completeListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });

    }
    public static void loadData(MyCompleteListener completeListener){
        loadCategories(new MyCompleteListener() {
            @Override
            public void onSuccess() {
                getUserData(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {

                        getUsersCount(new MyCompleteListener() {
                            @Override
                            public void onSuccess() {
                                loadBmIds(completeListener);
                            }

                            @Override
                            public void onFailure() {
                                completeListener.onFailure();
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        completeListener.onFailure();
                    }
                });
            }

            @Override
            public void onFailure() {
                completeListener.onFailure();
            }
        });
    }
    public static void createCategory(CategoryModel newCategory, MyCompleteListener completeListener) {
      // Tạo một Map để lưu trữ dữ liệu của danh mục mới
      Map<String, Object> categoryData = new HashMap<>();
      categoryData.put("NAME", newCategory.getName());
      categoryData.put("NO_OF_TESTS", newCategory.getNoOfTests());

      // Tạo một document mới với ID ngẫu nhiên
      DocumentReference newDoc = g_firestore.collection("QUIZ").document();

      // Sử dụng ID của document mới làm CAT_ID
      categoryData.put("CAT_ID", newDoc.getId());

      // Thêm danh mục mới vào Firestore
      newDoc.set(categoryData)
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void aVoid) {
                      // Cập nhật ID của danh mục mới
                      newCategory.setDocID(newDoc.getId());

                      // Cập nhật số lượng category trong mục "Categories"
                      DocumentReference categoriesDoc = g_firestore.collection("QUIZ").document("Categories");
                      categoriesDoc.update("COUNT", FieldValue.increment(1));

                      // Lấy số lượng category hiện tại
                      categoriesDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                          @Override
                          public void onSuccess(DocumentSnapshot documentSnapshot) {
                              Long count = documentSnapshot.getLong("COUNT");
                              // Cập nhật CAT[i]_ID trong "Categories"
                              categoriesDoc.update("CAT" + count + "_ID", newDoc.getId());
                          }
                      });

                      completeListener.onSuccess();
                  }
              })
              .addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {
                      completeListener.onFailure();
                  }
              });
  }
   public static void deleteCategory(String catId, MyCompleteListener completeListener) {
       // Lấy tham chiếu đến document chứa thông tin danh mục
       DocumentReference catDocRef = g_firestore.collection("QUIZ").document(catId);

       // Xóa document từ Firestore
       catDocRef.delete()
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       // Giảm số lượng danh mục trong "Categories"
                       DocumentReference categoriesDoc = g_firestore.collection("QUIZ").document("Categories");
                       categoriesDoc.update("COUNT", FieldValue.increment(-1));

                       // Lấy số lượng danh mục hiện tại và cập nhật lại chỉ số
                       categoriesDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                           @Override
                           public void onSuccess(DocumentSnapshot documentSnapshot) {
                               Long count = documentSnapshot.getLong("COUNT");
                               WriteBatch batch = g_firestore.batch();

                               for (int i = 1; i <= count; i++) {
                                   String nextCatId = documentSnapshot.getString("CAT" + (i + 1) + "_ID");
                                   if (nextCatId != null) {
                                       batch.update(categoriesDoc, "CAT" + i + "_ID", nextCatId);
                                       batch.update(categoriesDoc, "CAT" + (i + 1) + "_ID", FieldValue.delete());
                                   }
                               }

                               batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful()) {
                                           // Xóa danh mục khỏi g_catList
                                           for (int i = 0; i < g_catList.size(); i++) {
                                               if (g_catList.get(i).getDocID().equals(catId)) {
                                                   g_catList.remove(i);
                                                   break;
                                               }
                                           }
                                           completeListener.onSuccess();
                                       } else {
                                           completeListener.onFailure();
                                       }
                                   }
                               });
                           }
                       });
                   }
               })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       completeListener.onFailure();
                   }
               });
   }
    public static void createTest(String categoryId, TestModel newTest, MyCompleteListener completeListener) {
        // Cập nhật số lượng test trong category
        DocumentReference categoryDoc = g_firestore.collection("QUIZ").document(categoryId);
        categoryDoc.update("NO_OF_TESTS", FieldValue.increment(1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Lấy số lượng test hiện tại
                        categoryDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Long count = documentSnapshot.getLong("NO_OF_TESTS");
                                // Cập nhật TEST[i]_ID và TEST[i]_TIME trong TESTS_INFO
                                DocumentReference testsInfoDoc = categoryDoc.collection("TESTS_LIST").document("TESTS_INFO");
                                testsInfoDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                // Document exists, update it
                                                testsInfoDoc.update("TEST" + count + "_ID", newTest.getTestID());
                                                testsInfoDoc.update("TEST" + count + "_TIME", newTest.getTime());
                                            } else {
                                                // Document does not exist, create it
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("TEST" + count + "_ID", newTest.getTestID());
                                                data.put("TEST" + count + "_TIME", newTest.getTime());
                                                testsInfoDoc.set(data);
                                            }
                                        } else {
                                            Log.d(TAG, "Failed with: ", task.getException());
                                        }
                                    }
                                });
                            }
                        });

                        completeListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });
    }

    public static void deleteTest(int testIndex, MyCompleteListener completeListener) {
        // Tạo một Map để lưu trữ các trường cần xóa
        Map<String, Object> testData = new HashMap<>();
        testData.put("TEST" + testIndex + "_ID", FieldValue.delete());
        testData.put("TEST" + testIndex + "_TIME", FieldValue.delete());

        // Lấy tham chiếu đến document chứa thông tin bài kiểm tra
        DocumentReference docRef = g_firestore.collection("QUIZ").document(DbQuery.g_current_cat_id).collection("TESTS_LIST").document("TESTS_INFO");

        // Xóa các trường từ Firestore
        docRef.update(testData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Giảm số lượng test trong category hiện tại
                        g_firestore.collection("QUIZ").document(DbQuery.g_current_cat_id)
                                .update("NO_OF_TESTS", FieldValue.increment(-1));

                        // Xóa bài kiểm tra khỏi g_testList
                        g_testList.remove(testIndex - 1);

                        completeListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        completeListener.onFailure();
                    }
                });
    }
  public static void updateTestIndices(int deletedTestIndex, MyCompleteListener completeListener) {
        // Lấy tham chiếu đến document chứa thông tin bài kiểm tra
        DocumentReference docRef = g_firestore.collection("QUIZ").document(DbQuery.g_current_cat_id).collection("TESTS_LIST").document("TESTS_INFO");

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Lấy danh sách tất cả các trường trong document
                        Map<String, Object> fields = document.getData();

                        WriteBatch batch = g_firestore.batch();

                        for (String field : fields.keySet()) {
                            // Xác định các trường nào có chỉ số lớn hơn chỉ số của bài test đã xóa
                            String prefix = field.substring(0, 4);
                            int index = Integer.parseInt(field.substring(4, field.indexOf('_')));

                            if (prefix.equals("TEST") && index > deletedTestIndex) {
                                // Xóa các trường này
                                batch.update(docRef, field, FieldValue.delete());
                                // Tạo lại các trường này với chỉ số giảm đi 1
                                String newField = "TEST" + (index - 1) + field.substring(field.indexOf('_'));
                                batch.update(docRef, newField, fields.get(field));
                            }
                        }

                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    completeListener.onSuccess();
                                } else {
                                    completeListener.onFailure();
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "No such document");
                        completeListener.onFailure();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    completeListener.onFailure();
                }
            }
        });
    }



}
