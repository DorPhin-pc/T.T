package app.taxi.newtaxi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class Map extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener {
    main main = new main();
    GoogleMap map;
    GoogleApiClient googleApiClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location mLastKnownLocation, Location;
    Task<Location> location;
    Marker marker;
    LatLng latLng, selectlatLng;
    double resultLat, resultLng;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static int DEFAULT_ZOOM = 15; //0~21 level
    private final LatLng mDefaultLocation = new LatLng(37.566643, 126.978279);
    private CameraPosition mCameraPosition;
    private boolean mLocationPermissionGranted;
    String resultAddress, selector;
    TextView addressView;
    Button settingBUTTON;

    void init() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this); //마지막 위치
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // 현재 GPS 위치

        addressView = findViewById(R.id.addressView);
        settingBUTTON = findViewById(R.id.settingBUTTON);
        Intent intent = getIntent();
        selector = intent.getExtras().getString("POSITION");
        settingBUTTON.setText(selector + "지로 설정");
    }

    void click() {
        settingBUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //버튼 클릭시, 마커 선택 위치의 좌표를 주소로 변환한 뒤, Intent
                String address;
                address = addressView.getText().toString();
                SharedPreferences positionDATA = getSharedPreferences("positionDATA", MODE_PRIVATE);
                SharedPreferences.Editor editor = positionDATA.edit();
                if(address.equals("") || address.equals("TextView")){
                    Toast.makeText(getApplicationContext(),"다른 지역을 선택해주세요.",Toast.LENGTH_SHORT).show();
                }else {
                    editor.putString(selector + "지", address);
                    editor.apply();
                    Intent intent1 = new Intent(getApplicationContext(), main_simple.class);
                    startActivity(intent1);
                    finish();
                }
            }
        });
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
        setContentView(R.layout.activity_map);
        getLocationPermission();
        init();
        click();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.MAPview);
        mapFragment.getMapAsync(this);*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);
        if (mLocationPermissionGranted) {
            moveMap();
        } else
            setmDefaultLocation();
    }

    @SuppressLint("MissingPermission")
    void moveMap() {
        if (location != null && map != null) {
            final Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
            locationTask.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    LatLng gpsLatLng = new LatLng(location.getResult().getLatitude(), location.getResult().getLongitude());
                    CameraPosition position = new CameraPosition.Builder().target(gpsLatLng).zoom(DEFAULT_ZOOM).build();
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                    map.setMyLocationEnabled(mLocationPermissionGranted);
                    if (marker != null) marker.remove();
                    marker = map.addMarker(new MarkerOptions().position(gpsLatLng).title("선택 위치").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    selectlatLng = gpsLatLng;
                    MyGeocoderThread thread = new MyGeocoderThread(gpsLatLng);
                    thread.start();
                }
            });
        } else {
            // TODO : GPS 현재위치 받아오기(지금은 GPS 사용전적이 있어야만 실행됨)
        }
    }

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
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = fusedLocationProviderClient.getLastLocation();
            moveMap();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    location = fusedLocationProviderClient.getLastLocation();
                    moveMap();
                }
            } else {
                setmDefaultLocation();
            }
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
        MyGeocoderThread thread = new MyGeocoderThread(map.getCameraPosition().target);
        thread.start();
        resultLat = map.getCameraPosition().target.latitude;
        resultLng = map.getCameraPosition().target.longitude;
    }

    @Override
    public void onCameraMove() {
        if (mLocationPermissionGranted)
            marker.setPosition(map.getCameraPosition().target);
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastKnownLocation = task.getResult();
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                            if (marker != null) marker.remove();
                            marker = map.addMarker(new MarkerOptions().position(latLng).title("선택 위치").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        } else {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false); //현재 위치 버튼
                        }
                    }
                });
            }
        } catch (SecurityException e) {

        }
    }

    void setmDefaultLocation() {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
    }

    class MyGeocoderThread extends Thread {
        LatLng latLng;

        public MyGeocoderThread(LatLng latLng) {
            this.latLng = latLng;
        }

        @Override
        public void run() {
            SharedPreferences positionDATA = getSharedPreferences("positionDATA", MODE_PRIVATE);
            SharedPreferences.Editor editor = positionDATA.edit();

            Geocoder geocoder = new Geocoder(getApplicationContext());
            List<Address> addressList = null;
            String addressText = "";
            try {
                addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                editor.putString(selector, latLng.latitude + "," + latLng.longitude);
                editor.apply();
                Thread.sleep(100);
                if (addressList != null && addressList.size() > 0) {
                    Address address = addressList.get(0);
                    addressText = address.getAdminArea() + " " + (address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : address.getLocality()) + " ";
                    addressText.replace("", "");

                    String txt = address.getSubLocality();
                    if (txt != null) {
                        addressText += txt + " ";
                    }
                    addressText += address.getThoroughfare() + " " + address.getSubThoroughfare();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Message message = new Message();
            message.what = 100;
            message.obj = addressText;
            handler.sendMessage(message);       //TODO : 출발지와 도착지가 null이거나 공백으로 표시될 때, 알림 창 표시

            resultAddress = addressText;
            editor.putString(selector + "지", resultAddress);
            editor.apply();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100: {
                    addressView.setText((String) msg.obj);
                }
            }
        }
    };
}

