package com.example.securevault.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.securevault.model.FileMeta;
import com.example.securevault.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilesFragment extends Fragment {

    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

    private ArrayList<FileMeta> files = new ArrayList<>();

    private ArrayList<FileMeta> locationFiles = new ArrayList<>();

    private ArrayList<FileMeta> timeFiles = new ArrayList<>();



    private GridView timeGridView;

    private GridView locationGridView;

    private FilesAdapter timeAdapter;

    private FilesAdapter locationAdapter;

    ImageView emptyImage;

    TextView errorTitle;

    TextView errorDesc;

    TextView txtFileByTime;

    TextView txtLocationByTime;

    LocationManager locationManager;
    DatabaseReference usersRef = rootRef.child("Files");

    Double currentLat;

    Double currentLong;
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
                locationFiles = new ArrayList<>();
                timeFiles = new ArrayList<>();

                for(int i=0;i<files.size() ;i++){
                    if(files.get(i).getType().equals("location")){
                        locationFiles.add(files.get(i));
                    }else{
                        timeFiles.add(files.get(i));
                    }
                }


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
                timeAdapter = new FilesAdapter(getActivity(), timeFiles);
                timeGridView.setAdapter(timeAdapter);


                locationAdapter.notifyDataSetChanged();
                locationAdapter = new FilesAdapter(getActivity(), locationFiles);
                locationGridView.setAdapter(locationAdapter);
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

    public static Map<String, Object> convert(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String name = field.getName();
            Object val = field.get(obj);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

    public FilesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FilesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FilesFragment newInstance(String param1, String param2) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usersRef.addListenerForSingleValueEvent(eventListener);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        txtFileByTime = view.findViewById(R.id.txtfileByTime);
        txtLocationByTime = view.findViewById(R.id.txtfileByLocation);


        timeGridView = view.findViewById(R.id.timeGridView);
        locationGridView = view.findViewById(R.id.locationGridView);

        emptyImage = view.findViewById(R.id.empty_error_image);
        errorTitle = view.findViewById(R.id.empty_error_title);
        errorDesc = view.findViewById(R.id.empty_error_desc);

        timeAdapter = new FilesAdapter(getActivity(), files);
        timeGridView.setAdapter(timeAdapter);

        locationAdapter = new FilesAdapter(getActivity(), files);
        locationGridView.setAdapter(locationAdapter);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
        return view;
    }


}