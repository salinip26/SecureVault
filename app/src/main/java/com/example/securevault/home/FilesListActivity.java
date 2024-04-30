package com.example.securevault.home;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.securevault.model.FileMeta;
import com.example.securevault.R;
import com.example.securevault.util.LocationUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FilesListActivity extends AppCompatActivity{

    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

    private ArrayList<FileMeta> files = new ArrayList<>();

    private ArrayList<FileMeta> locationFiles = new ArrayList<>();

    private ArrayList<FileMeta> timeFiles = new ArrayList<>();


    private GridView timeGridView;

    private GridView locationGridView;

    private FilesAdapter timeAdapter;

    private FilesAdapter locationAdapter;

    LocationManager locationManager;
    DatabaseReference usersRef = rootRef.child("Files");

    Double currentLat;

    Double currentLong;

    private LocationRequest locationRequest;
    private static final int REQUEST_CHECK_SETTINGS = 10001;

    private FirebaseAuth mAuth;

    ImageView emptyImage;

    TextView errorTitle;

    TextView errorDesc;

    TextView txtFileByTime;

    TextView txtLocationByTime;

    ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<FileMeta> list = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();

            try {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    map = (Map<String, Object>) ds.getValue();
                    String filePath = map.get("filePath") != null ? map.get("filePath").toString() : null;
                    String fileName = map.get("fileName") != null ? map.get("fileName").toString() : null;
                    String extension = map.get("extension") != null ? map.get("extension").toString() : null;

                    String type = map.get("type") != null ? map.get("type").toString() : null;
                    Double lat = map.get("latVal") != null ? Double.parseDouble(map.get("latVal").toString()) : null;
                    Double lng = map.get("longVal") != null ? Double.parseDouble(map.get("longVal").toString()) : null;
                    String from = map.get("fromTime") != null ? map.get("fromTime").toString() : null;
                    String to = map.get("toTime") != null ? map.get("toTime").toString() : null;

                    if (filePath != null) {
                        FileMeta meta = new FileMeta(filePath, fileName,extension, type, lat, lng, from, to);
                        list.add(meta);
                    }
                }

                files = list;

                System.out.println("SUSUUSUS");

            } catch (Exception e) {
                System.out.println(e.toString());
            }


            System.out.println();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_icon) {
            mAuth.signOut();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_files_list);
        usersRef.addListenerForSingleValueEvent(eventListener);
        mAuth = FirebaseAuth.getInstance();

        emptyImage = findViewById(R.id.empty_error_image);
        errorTitle = findViewById(R.id.empty_error_title);
        errorDesc = findViewById(R.id.empty_error_desc);

        txtFileByTime = findViewById(R.id.txtfileByTime);
        txtLocationByTime = findViewById(R.id.txtfileByLocation);


        timeGridView = findViewById(R.id.timeGridView);
        locationGridView = findViewById(R.id.locationGridView);


        timeAdapter = new FilesAdapter(FilesListActivity.this, files);
        timeGridView.setAdapter(timeAdapter);

        locationAdapter = new FilesAdapter(FilesListActivity.this, files);
        locationGridView.setAdapter(locationAdapter);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(FilesListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if(isGpsEnabled()){
                    LocationServices.getFusedLocationProviderClient(FilesListActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(FilesListActivity.this)
                                            .removeLocationUpdates(this);

                                    if(!locationResult.getLocations().isEmpty()){
                                        int index = locationResult.getLocations().size() -1;

                                        currentLat = locationResult.getLastLocation().getLatitude();
                                        currentLong = locationResult.getLastLocation().getLongitude();

                                        filterArray();

                                    }
                                }
                            }, Looper.getMainLooper());
                }else{
                    turnOnGPS();
                }

            }else{
                ActivityCompat.requestPermissions(FilesListActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 100);
            }
        }
        if (ContextCompat.checkSelfPermission(FilesListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FilesListActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(FilesListActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                                resolvableApiException.startResolutionForResult(FilesListActivity.this,REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    private boolean isGpsEnabled(){
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if(locationManager == null){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return isEnabled;
    }

    private void filterArray(){
        ArrayList<FileMeta> filteredTimeArr = new ArrayList<>();
        ArrayList<FileMeta> filteredLocationArr = new ArrayList<>();

        for(int i=0; i< files.size(); i++){
            if(files.get(i).getType().equals("location")){

                boolean isWithin =  LocationUtil.isWithinRadius(currentLat,currentLong,files.get(i).getLatVal(),files.get(i).getLongVal(),4000);

                if(isWithin){
                    filteredLocationArr.add(files.get(i));
                }else{
                    System.out.println("not within");
                }
            }else if(files.get(i).getType().equals("time")){
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                     LocalTime currentTime = LocalTime.now();

                    LocalTime fromTime = LocalTime.of(Integer.parseInt(files.get(i).getFromTime().split(":")[0]), Integer.parseInt(files.get(i).getFromTime().split(":")[1]));
                    LocalTime toTime = LocalTime.of(Integer.parseInt(files.get(i).getToTime().split(":")[0]), Integer.parseInt(files.get(i).getToTime().split(":")[1]));

                    if (currentTime.isAfter(fromTime) && currentTime.isBefore(toTime)) {
                        System.out.println("Current time is between " + fromTime + " and " + toTime);
                        filteredTimeArr.add(files.get(i));
                    } else {
                        System.out.println("Current time is not between " + fromTime + " and " + toTime);
                    }
                }
            }
        }
        timeFiles = filteredTimeArr;
        locationFiles = filteredLocationArr;

        if(files.isEmpty()){
            timeGridView.setVisibility(View.GONE);
            txtFileByTime.setVisibility(View.GONE);
            locationGridView.setVisibility(View.GONE);
            txtLocationByTime.setVisibility(View.GONE);

            emptyImage.setVisibility(View.VISIBLE);
            errorTitle.setVisibility(View.VISIBLE);
            errorDesc.setVisibility(View.VISIBLE);
        }

        if(timeFiles.isEmpty()){
            timeGridView.setVisibility(View.GONE);
            txtFileByTime.setVisibility(View.GONE);
        }

        if(locationFiles.isEmpty()){
            locationGridView.setVisibility(View.GONE);
            txtLocationByTime.setVisibility(View.GONE);
        }

        timeAdapter.notifyDataSetChanged();
        timeAdapter = new FilesAdapter(FilesListActivity.this, timeFiles);
        timeGridView.setAdapter(timeAdapter);


        locationAdapter.notifyDataSetChanged();
        locationAdapter = new FilesAdapter(FilesListActivity.this, locationFiles);
        locationGridView.setAdapter(locationAdapter);
    }

}