package com.example.pdf_uploader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UploaderPageActivity extends AppCompatActivity {
    private static final int DOCUMENT_REQUEST_CODE = 2001;
    private ListView listViewLv;
    private MaterialButton uploadBt;
    private TextInputEditText filenameEt;
    private List<Map<String, String>> uploadPDFs;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private StorageReference stRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploader_page);
        initViews();
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("User");
        stRef = FirebaseStorage.getInstance().getReference("User");
        uploadPDFs = new ArrayList<>();
        viewAllFiles();

        uploadBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialogBox();
//                selectDocument();
            }
        });
    }

    private void showAlertDialogBox() {
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        @SuppressLint("InflateParams") View promptsView = li.inflate(R.layout.alert_dialog, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
        alertDialog.setView(promptsView);
        filenameEt = promptsView.findViewById(R.id.filename);
        alertDialog
                .setCancelable(true)
                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectDocument();
                    }
                });
        AlertDialog alertDialog1 = alertDialog.create();
        alertDialog1.show();
    }

    private void viewAllFiles() {
        DatabaseReference databaseReference = dbRef.child("documents");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                uploadPDFs.clear();
                Map<String, String> map = new HashMap<>();
                for(DataSnapshot postSnapshot:snapshot.getChildren()){
                    map.put("filename", postSnapshot.child("filename").getValue(String.class));
                    map.put("url", postSnapshot.child("url").getValue(String.class));
                    uploadPDFs.add(map);
                }
                String[] uploads = new String[uploadPDFs.size()];

                for(int i = 0; i < uploads.length; i++){
                    uploads[i] = uploadPDFs.get(i).get("filename");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, uploads);
                listViewLv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectDocument() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF file!"), DOCUMENT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == DOCUMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            upload(data.getData());
        }
    }

    private void upload(Uri data) {

        StorageReference storageReference = stRef.child("uploads/" +System.currentTimeMillis()+".pdf");
        storageReference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uri.isComplete());
                        Uri url = uri.getResult();

                        Map<String, String> map = new HashMap<>();
                        map.put("filename", Objects.requireNonNull(filenameEt.getText()).toString());
                        assert url != null;
                        map.put("url", url.toString());

                        dbRef.child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                                .child("documents")
                                .child(Objects.requireNonNull(dbRef.push().getKey()))
                                .setValue(map);

                        Toast.makeText(UploaderPageActivity.this, "File uploaded!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews() {
        listViewLv = findViewById(R.id.list_view);
        uploadBt = findViewById(R.id.upload_button);
    }
}