package com.example.securevault.home;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.securevault.R;
import com.example.securevault.model.FileMeta;
import com.example.securevault.util.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.UUID;

public class LocationFragment extends Fragment {

    private Button btnSelectFiles;

    private Button btnSelectFile;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private double seledctedLat = 0.0;

    private double selectedLng = 0.0;

    DatabaseReference fileDatabaseRef;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_location, container, false);

        SupportMapFragment supportMapFragment=(SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.id_map);

        btnSelectFiles = view.findViewById(R.id.btnSelectFiles);

        storage = FirebaseStorage.getInstance();

        storageReference = storage.getReference();

        fileDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Files");



        btnSelectFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        MarkerOptions markerOptions=new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(latLng.latitude+" : "+latLng.longitude);
                        googleMap.clear();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                        googleMap.addMarker(markerOptions);

                        CircleOptions circleOptions = new CircleOptions();
                        circleOptions.center(latLng);
                        circleOptions.radius(4000);
                        circleOptions.strokeColor(Color.BLACK);
                        circleOptions.fillColor(0x30ff0000);
                        circleOptions.strokeWidth(2);
                        googleMap.addCircle(circleOptions);

                        seledctedLat = latLng.latitude;
                        selectedLng = latLng.longitude;

                        btnSelectFiles.setVisibility(View.VISIBLE);
                    }
                });
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
        // If the selection didn't work
        super.onActivityResult(requestCode, resultCode, returnIntent);
        if (requestCode == 1 && resultCode == RESULT_OK && returnIntent.getData() != null) {
            Uri uri = returnIntent.getData();
            File file = new File(uri.toString());
            System.out.println("kaka");
            ContentResolver contentResolver = getActivity().getContentResolver();
            String mimeType = contentResolver.getType(uri);

            uploadPicture(file,uri,mimeType.split("/")[1]);
        }
    }

    private void insertData(double lat, double lng, String path, String fileName,String mimeType){

        FileMeta file = new FileMeta(path,fileName,mimeType,"location",lat,lng,null,null);

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

    private void uploadPicture(File file, Uri uri, String mimeType) {

        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setTitle("Uploading File...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();

        StorageReference filesRef = storageReference.child("files/"  + randomKey+ '.'+mimeType);
        //encryptFile
        Util.encrypt(file.getPath(),randomKey+ "-encrypted." + mimeType);

        filesRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Snackbar.make(getActivity().findViewById(android.R.id.content), "File Uploaded", Snackbar.LENGTH_LONG).show();
                insertData(seledctedLat,selectedLng,filesRef.getPath(),randomKey + file.getName(),mimeType);

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
