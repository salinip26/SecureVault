package com.example.securevault.home;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.securevault.model.FileMeta;
import com.example.securevault.R;
import com.example.securevault.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimeFragment extends Fragment {

    private Button buttonSelectFiles;
    private Button buttonSelectToTime;
    private Button buttonSelectFromTime;
    private TextView fromText;
    private TextView toText;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private double seledctedLat = 0.0;
    private double selectedLng = 0.0;

    DatabaseReference fileDatabaseRef;

    public TimeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_time, container, false);;
        buttonSelectFiles = view.findViewById(R.id.btnSelectFiles);
        buttonSelectFromTime = view.findViewById(R.id.btnPickFromTime);
        buttonSelectToTime = view.findViewById(R.id.btnPickToTime);

        buttonSelectFiles = view.findViewById(R.id.btnSelectFiles);

        fromText = view.findViewById(R.id.txtFromTime);
        toText = view.findViewById(R.id.txtToTime);

        storage = FirebaseStorage.getInstance();

        storageReference = storage.getReference();

        fileDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Files");


        buttonSelectFromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                fromText.setText((hourOfDay < 10 ? ('0'+Integer.toString(hourOfDay)) : hourOfDay) + ":" + (minute < 10 ? ('0'+Integer.toString(minute)) : minute));
                                fromText.setVisibility(View.VISIBLE);

                                if(fromText.getText() != "" && toText.getText() != ""){
                                    buttonSelectFiles.setVisibility(View.VISIBLE);
                                }
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        buttonSelectToTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                toText.setText((hourOfDay < 10 ? '0'+Integer.toString(hourOfDay) : hourOfDay) + ":" + (minute < 10 ? '0'+Integer.toString(minute) : minute));
                                toText.setVisibility(View.VISIBLE);
                                if(fromText.getText() != "" && toText.getText() != ""){
                                    buttonSelectFiles.setVisibility(View.VISIBLE);
                                }
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        buttonSelectFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });
        return view;

    }

    private void choosePicture(){
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, 1);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent returnIntent) {
        super.onActivityResult(requestCode, resultCode, returnIntent);
        if (requestCode == 1 && resultCode == RESULT_OK && returnIntent.getData() != null ) {
            Uri uri = returnIntent.getData();
            ContentResolver contentResolver = getActivity().getContentResolver();
            String mimeType = contentResolver.getType(uri);
            File file = new File(uri.toString());
            System.out.println("kaka");
            uploadPicture(file,uri,mimeType.split("/")[1]);
        } else {

        }
    }

    private void insertData(double lat, double lng, String path,String fileName,String mimeType){

        FileMeta file = new FileMeta(path,fileName,mimeType,"time",0.0,0,fromText.getText().toString(),toText.getText().toString());

        fileDatabaseRef.push().setValue(file).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                System.out.println("asasasas");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("klakak");
            }
        });
    }

    private void uploadPicture(File file, Uri uri,String mimeType) {

        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setTitle("Uploading File...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        // Create a reference to "mountains.jpg"
        StorageReference filesRef = storageReference.child("files/" + randomKey+ '.' + mimeType);

        Util.encrypt(file.getPath(),randomKey+ "-encrypted." + mimeType);

        filesRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Snackbar.make(getActivity().findViewById(android.R.id.content), "File Uploaded", Snackbar.LENGTH_LONG).show();
                insertData(seledctedLat,selectedLng,filesRef.getPath(), randomKey + file.getName(),mimeType);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Failed Upload", Snackbar.LENGTH_LONG).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = 100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount();
                pd.setMessage("Percentage: " + (int) progressPercent + "%");
            }
        });


    }
}