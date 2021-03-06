package app.taxi.newtaxi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Join extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraIdleListener,GoogleMap.OnCameraMoveListener {
    public static Activity Join;
    main_simple main = new main_simple();
    Button m1000button,m700button,m500button,m300button,m100button,LISTbutton,JOINbutton,CreateButton;
    private DatabaseReference mDatabase,rDatabase;
    GoogleMap map;
    GoogleApiClient googleApiClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location mLastKnownLocation;
    Task<Location> location;
    Marker marker;
    CircleOptions circle;
    LatLng latLng,selectlatLng, STARTLatlng,ARRIVELatlng;
    int DISTANCE = 500;
    int MARKERcount=0;
    int MyPoint;
    ArrayList<String> MARKERlist = new ArrayList<String>();
    String SELECT_latitude = "37.566643",SELECT_longitude = "126.978279";
    String USERNAME,ID,URL,INDEX,GENDER;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static int DEFAULT_ZOOM = 15; //0~21 level
    private final LatLng mDefaultLocation = new LatLng(37.566643, 126.978279);
    private CameraPosition mCameraPosition;
    private boolean mLocationPermissionGranted;
    Dialog dialog;
    MapView mMapView;
    TextView TIMEtext,PRICEtext,DISTANCEtext;

    void init(){
        Join = Join.this;
        Intent intent = getIntent();
        DISTANCE = intent.getExtras().getInt("METER");

        SharedPreferences positionDATA = getSharedPreferences("positionDATA",MODE_PRIVATE);
        SharedPreferences.Editor editor = positionDATA.edit();

        USERNAME = positionDATA.getString("USERNAME","");
        ID = positionDATA.getString("ID","");
        URL = positionDATA.getString("PROFILE","");
        GENDER = positionDATA.getString("GENDER","미정");
        STARTLatlng = new LatLng(Double.valueOf(positionDATA.getString("출발","").split(",")[0]),
                Double.valueOf(positionDATA.getString("출발","").split(",")[1]));
        ARRIVELatlng = new LatLng(Double.valueOf(positionDATA.getString("도착","").split(",")[0]),
                Double.valueOf(positionDATA.getString("도착","").split(",")[1]));

        m1000button= findViewById(R.id.m1000button);
        m700button= findViewById(R.id.m700button);
        m500button= findViewById(R.id.m500button);
        m300button= findViewById(R.id.m300button);
        m100button= findViewById(R.id.m100button);
        LISTbutton= findViewById(R.id.LISTbutton);
        CreateButton = findViewById(R.id.CreateButton);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        rDatabase = FirebaseDatabase.getInstance("https://taxitogether.firebaseio.com/").getReference();

        mDatabase.child("user").child(ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                MyPoint = user.getPoint();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void click(){
        m1000button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DISTANCE=1000;
                DrawCircle(DISTANCE);
                m1000button.setTextColor(Color.parseColor("#FF6600"));
                m700button.setTextColor(Color.parseColor("#000000"));
                m500button.setTextColor(Color.parseColor("#000000"));
                m300button.setTextColor(Color.parseColor("#000000"));
                m100button.setTextColor(Color.parseColor("#000000"));
            }
        });
        m700button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DISTANCE=700;
                DrawCircle(DISTANCE);
                m1000button.setTextColor(Color.parseColor("#000000"));
                m700button.setTextColor(Color.parseColor("#FF6600"));
                m500button.setTextColor(Color.parseColor("#000000"));
                m300button.setTextColor(Color.parseColor("#000000"));
                m100button.setTextColor(Color.parseColor("#000000"));
            }
        });
        m500button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DISTANCE=500;
                DrawCircle(DISTANCE);
                m1000button.setTextColor(Color.parseColor("#000000"));
                m700button.setTextColor(Color.parseColor("#000000"));
                m500button.setTextColor(Color.parseColor("#FF6600"));
                m300button.setTextColor(Color.parseColor("#000000"));
                m100button.setTextColor(Color.parseColor("#000000"));
            }
        });
        m300button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DISTANCE=300;
                DrawCircle(DISTANCE);
                m1000button.setTextColor(Color.parseColor("#000000"));
                m700button.setTextColor(Color.parseColor("#000000"));
                m500button.setTextColor(Color.parseColor("#000000"));
                m300button.setTextColor(Color.parseColor("#FF6600"));
                m100button.setTextColor(Color.parseColor("#000000"));
            }
        });
        m100button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DISTANCE=100;
                DrawCircle(DISTANCE);
                m1000button.setTextColor(Color.parseColor("#000000"));
                m700button.setTextColor(Color.parseColor("#000000"));
                m500button.setTextColor(Color.parseColor("#000000"));
                m300button.setTextColor(Color.parseColor("#000000"));
                m100button.setTextColor(Color.parseColor("#FF6600"));
            }
        });
        LISTbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Join_list.class);
                intent.putExtra("MARKER",MARKERlist);
                intent.putExtra("MARKER_COUNT",MARKERcount);
                startActivity(intent);
            }
        });
        CreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),Posting_simple.class);
                startActivity(intent);
            }
        });
    }
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_join);
        getLocationPermission();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.DISTANCEMAP);
        mapFragment.getMapAsync(this);
        init();
        click();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = fusedLocationProviderClient.getLastLocation();
            DrawCircle(DISTANCE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        getLocationPermission();
    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMove() {    }
    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected())
            googleApiClient.disconnect();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);
        map.setOnMarkerClickListener(this);
        if (mLocationPermissionGranted){
            DrawCircle(DISTANCE);
        }
        else
            setDefaultLocation();
    }
    @SuppressLint("MissingPermission")
    void DrawCircle(final int D) {
        if (location != null && map != null) {
            Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
            locationTask.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    LatLng gpsLatLng = new LatLng(location.getResult().getLatitude(), location.getResult().getLongitude());
                    CameraPosition position = new CameraPosition.Builder().target(STARTLatlng).zoom(DEFAULT_ZOOM).build();
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                    map.setMyLocationEnabled(mLocationPermissionGranted);
                    selectlatLng = gpsLatLng;
                    Log.v("DISTANCE",D+"");
                    if(circle!=null) {
                        map.clear();
                        circle.radius(D);
                        CIRCLE_MARKER(STARTLatlng,ARRIVELatlng,D);
                    }
                    else {
                        circle = new CircleOptions().center(STARTLatlng) //원점
                                .radius(D)      //반지름 단위 : m
                                .strokeWidth(3f);  //선너비 0f : 선없음;
                        CIRCLE_MARKER(STARTLatlng,ARRIVELatlng,D);
                    }
                    map.addCircle(circle);
                }
            });
        }else{

        }
    }
    void CIRCLE_MARKER(final LatLng STARTLatlng, final LatLng ARRIVELatlng, int DISTANCE){
        Double METER = 0.0045;
        switch (DISTANCE){
            case 1000:METER=0.009; break;
            case 700:METER=0.0063; break;
            case 500:METER=0.0045; break;
            case 300:METER=0.0027; break;
            case 100:METER=0.0009; break;
        }
        final Double M = METER;

        MARKERcount=0;
        MARKERlist.clear();
        LISTbutton.setText("리스트로 보기");
        mDatabase.child("post").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Data_Post data_post = snapshot.getValue(Data_Post.class);
                    Double DB_START_latitude = Double.valueOf(data_post.getStart_Latitude());
                    Double DB_START_longitude = Double.valueOf(data_post.getStart_Longitude());
                    Double DB_ARRIVE_latitude = Double.valueOf(data_post.getArrive_Latitude());
                    Double DB_ARRIVE_longitude = Double.valueOf(data_post.getArrive_Longitude());

                    if(Math.abs(DB_START_latitude-STARTLatlng.latitude) <= M && Math.abs(DB_START_longitude-STARTLatlng.longitude) <= M
                    && Math.abs(DB_ARRIVE_latitude-ARRIVELatlng.latitude) <= M && Math.abs(DB_ARRIVE_longitude-ARRIVELatlng.longitude) <= M) {//출발지와 도착지가 설정한 범위 안에 있을 때,
                        map.addMarker(new MarkerOptions().position(new LatLng(DB_START_latitude, DB_START_longitude)).title(data_post.getIndex())
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        MARKERcount++;
                        MARKERlist.add(data_post.getIndex());

                    }
                }
                LISTbutton.setText(LISTbutton.getText().toString() + "(" + MARKERcount + "건)");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    void setDefaultLocation(){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.566643, 126.978279),14));
        DrawCircle(DISTANCE);
    }
    @Override
    public boolean onMarkerClick(final Marker marker) {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.map_dialog);
        dialog.show();
        TextView OUTtext;
        mMapView = dialog.findViewById(R.id.MAP_Dialog);
        TIMEtext = dialog.findViewById(R.id.TIMEtext);
        PRICEtext = dialog.findViewById(R.id.PRICEtext);
        DISTANCEtext = dialog.findViewById(R.id.DISTANCEtext);
        OUTtext = dialog.findViewById(R.id.OUTtext);
        OUTtext.setVisibility(View.INVISIBLE);
        JOINbutton = dialog.findViewById(R.id.JOINbutton);
        JOINbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Query query = mDatabase.child("post").orderByChild("index").equalTo(marker.getTitle());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                            Data_Post data_post = appleSnapshot.getValue(Data_Post.class);
                            SharedPreferences positionDATA = getSharedPreferences("positionDATA",MODE_PRIVATE);
                            SharedPreferences.Editor editor = positionDATA.edit();
                            if(data_post.getPerson() < data_post.getMaxPerson()) {      //  인원 체크
                                if(MyPoint > 1000) { // 서비스 이용료 체크
                                    String path = "/" + dataSnapshot.getKey() + "/" + appleSnapshot.getKey();
                                    Map<String, Object> taskMap = new HashMap<>();
                                    taskMap.put("person", data_post.getPerson() + 1);
                                    mDatabase.child(path).updateChildren(taskMap);

                                    Data_Members data_members = new Data_Members(USERNAME, marker.getTitle(), URL, GENDER, ID, true, false);
                                    taskMap.clear();
                                    taskMap.put(ID, data_members);
                                    mDatabase.child("post-members").updateChildren(taskMap);
                                    rDatabase.child("post-members").push().setValue(taskMap);

                                    editor.putString("INDEX", marker.getTitle());
                                    editor.putString("??", INDEX);
                                    editor.putString("출발", data_post.getStart_Latitude() + "," + data_post.getStart_Longitude());
                                    editor.putString("도착", data_post.getArrive_Latitude() + "," + data_post.getArrive_Longitude());
                                    editor.putString("MAX", data_post.getMaxPerson() + "");
                                    editor.putString("TIME", TIMEtext.getText().toString());
                                    editor.putString("DISTANCE", DISTANCEtext.getText().toString());
                                    editor.putString("PAY", PRICEtext.getText().toString() + " P");
                                    editor.putString("PRICE", data_post.getPrice());
                                    editor.putInt("PERSON",data_post.getMaxPerson());
                                    editor.apply();
                                    Toast.makeText(getApplicationContext(), "참가 신청 완료!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), My_taxi_simple.class);
                                    intent.putExtra("INDEX", marker.getTitle());
                                    startActivity(intent);

                                    main.finish(); //메인 액티비티 종료
                                    finish(); //현 액티비티 종료
                                } else{
                                    Toast.makeText(getApplicationContext(),"노선에 참가하기 위한 포인트가 부족합니다.",Toast.LENGTH_SHORT).show();
                                }
                            } //  인원 체크
                            else{
                                Toast.makeText(getApplicationContext(),"인원이 초과되었습니다.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        Query query1 = mDatabase.child("post").orderByChild("index").equalTo(marker.getTitle());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    Data_Post data_post = appleSnapshot.getValue(Data_Post.class);
                    TIMEtext.setText(data_post.getTime());
                    PRICEtext.setText(data_post.getPay()+"");
                    DISTANCEtext.setText(data_post.getDistance());
                    SELECT_latitude = data_post.getArrive_Latitude();
                    SELECT_longitude = data_post.getArrive_Longitude();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        try{
            Thread.sleep(500);     // DB에서 받아오는 시간 지연 -> 로딩(원돌아가는거)로 변경하기
        }catch (Exception e){
            e.printStackTrace();
        }
        MapsInitializer.initialize(this);

        mMapView.onCreate(dialog.onSaveInstanceState());
        mMapView.onResume();// needed to get the map to display immediately
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
               LatLng latLng = new LatLng(Double.valueOf(SELECT_latitude),Double.valueOf(SELECT_longitude));
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("도착 위치"));
                CameraPosition position = new CameraPosition.Builder().target(latLng).zoom(DEFAULT_ZOOM).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                googleMap.getUiSettings().setZoomControlsEnabled(true);
            }
        });
        return true;
    }



}
