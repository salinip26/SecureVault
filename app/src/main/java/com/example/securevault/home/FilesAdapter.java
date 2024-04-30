package com.example.securevault.home;


import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.securevault.model.FileMeta;
import com.example.securevault.R;
import com.example.securevault.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

    public class FilesAdapter extends ArrayAdapter<FileMeta> {

        private TextView textView;

        private FirebaseStorage storage;
        private StorageReference storageReference;

        Context ctx;

//        private TextView textView;


        public FilesAdapter(@NonNull Context context, ArrayList<FileMeta> files) {
            super(context, 0, files);

            ctx = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View listitemView = convertView;
            if (listitemView == null) {
                listitemView = LayoutInflater.from(getContext()).inflate(R.layout.file_card_view, parent, false);
            }

            storage = FirebaseStorage.getInstance();

            storageReference = storage.getReference();

            FileMeta fileModel = getItem(position);

            StorageReference ref = storageReference.child(fileModel.getFilePath());

            TextView courseTV = listitemView.findViewById(R.id.fileName);

            courseTV.setText(fileModel.getFileName() != null ? fileModel.getFileName().split("-")[1]+"-"+fileModel.getFileName().split("-")[2]+ fileModel.getExtension() : fileModel.getFilePath() + fileModel.getExtension());

            listitemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            downloadFile(
                                    ctx,
                                    fileModel.getFileName() != null ? fileModel.getFileName() : fileModel.getFilePath(),
                                    "."+ fileModel.getExtension(),
                                    DIRECTORY_DOWNLOADS,uri);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("ERROR");
                        }
                    });
                }
            });

            return listitemView;
        }

        private void downloadFile(Context ctx, String fileName, String fileExtension, String destinationDirectory, Uri uri){
            DownloadManager downloadManager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request req = new DownloadManager.Request(uri);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            String path = (fileName + fileExtension);
//            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS , (fileName + fileExtension));
            downloadManager.enqueue(req);
            File encryptedFile = new File(Environment.DIRECTORY_DOWNLOADS + (fileName + "-decrypted" + fileExtension));
            try {
                Util.decrypt(encryptedFile, Environment.DIRECTORY_DOWNLOADS + (fileName + "-decrypted" + fileExtension));
                System.out.println("done");

            }catch(FileNotFoundException e){
                System.out.println("error");

            }
        }
    }

