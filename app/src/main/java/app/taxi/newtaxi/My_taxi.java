package app.taxi.newtaxi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
//TODO : 1번째 상세정보 클릭시, 게시판의 정보와 결제 버튼 구현(다이얼로그)
// 나가기 버튼(방장은 나갈때, 다이얼로그 표시)

public class My_taxi extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnCameraIdleListener,GoogleMap.OnCameraMoveListener {
    private DatabaseReference mDatabase;
    private TextView INDEXtext,TIMEtext,PRICEtext,DISTANCEtext,PERSONtext;
    private GoogleMap MAPview;
    GoogleApiClient googleApiClient;
    Marker marker;
    private static int DEFAULT_ZOOM = 14; //0~21 level
    int POINT=1000,MIDDLE_ZOOM = 14;
    private ListView LISTview,MYDIALOGlist;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private String userID,title,start,arrive,driver,taxinumber,phonenumber,ID;
    private int index,pay,person,point,PayPerPerson;
    long now = System.currentTimeMillis ();
    Date date = new Date(now);
    SimpleDateFormat sdfNow = new SimpleDateFormat("MM/DD");
    String Time = sdfNow.format(date);
    int MONTH = Integer.parseInt(Time.split("/")[0]);
    int DAY = Integer.parseInt(Time.split("/")[1]);
    private final int stuck = 10;
    LatLng STARTlatlng,ARRIVElatlng,MIDDLElatlng;
    AlertDialog.Builder PAYdialog, CHARGEdialog,OUTdialog;
    My_taxiAdapter adapter;
    Double MIDDLE_latitude,MIDDLE_longitude;
    Button INFObutton,OUTbutton,PAYbutton;
    Dialog dialog;

    void init() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        SharedPreferences positionDATA = getSharedPreferences("positionDATA", MODE_PRIVATE);
        SharedPreferences.Editor editor = positionDATA.edit();
        String start_lati = positionDATA.getString("출발", "").split(",")[0];
        String start_long = positionDATA.getString("출발", "").split(",")[1];
        STARTlatlng = new LatLng(Double.valueOf(start_lati), Double.valueOf(start_long));

        String arrive_lati = positionDATA.getString("도착", "").split(",")[0];
        String arrive_long = positionDATA.getString("도착", "").split(",")[1];
        ARRIVElatlng = new LatLng(Double.valueOf(arrive_lati), Double.valueOf(arrive_long));

        MIDDLE_latitude = Math.abs(STARTlatlng.latitude - ARRIVElatlng.latitude) / 2.0;
        MIDDLE_longitude = Math.abs(STARTlatlng.longitude - ARRIVElatlng.longitude) / 2.0;

        if (STARTlatlng.latitude > ARRIVElatlng.latitude)
            MIDDLE_latitude += ARRIVElatlng.latitude;
        else
            MIDDLE_latitude += STARTlatlng.latitude;
        if (STARTlatlng.longitude > ARRIVElatlng.longitude)
            MIDDLE_longitude += ARRIVElatlng.longitude;
        else
            MIDDLE_longitude += STARTlatlng.longitude;
        MIDDLElatlng = new LatLng(MIDDLE_latitude, MIDDLE_longitude);

        if (MIDDLE_latitude <= 0.0108 || MIDDLE_longitude <= 0.0108) {    //출발지와 도착지가 중간지점에서부터 1.2km 이상이면,
            MIDDLE_ZOOM = 14;
        } else if (MIDDLE_latitude <= 0.0216 || MIDDLE_longitude <= 0.0216) {
            MIDDLE_ZOOM = 13;
        } else if (MIDDLE_latitude <= 0.0432 || MIDDLE_longitude <= 0.0432) {
            MIDDLE_ZOOM = 12;
        } else if (MIDDLE_latitude <= 0.0864 || MIDDLE_longitude <= 0.0864) {
            MIDDLE_ZOOM = 11;
        } else if (MIDDLE_latitude <= 0.1728 || MIDDLE_longitude <= 0.1728) {
            MIDDLE_ZOOM = 10;
        } else if (MIDDLE_latitude <= 0.3456 || MIDDLE_longitude <= 0.3456) {
            MIDDLE_ZOOM = 9;
        }

        INFObutton = findViewById(R.id.INFObutton);
        INDEXtext = findViewById(R.id.indexView);
        LISTview = findViewById(R.id.LISTview);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.MY_MAP);
        mapFragment.getMapAsync(this);

        dialog = new Dialog(this);
    }
    void click(){
        INFObutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.my_taxi_listview);
                dialog.show();
                TIMEtext = dialog.findViewById(R.id.TIMEtext);
                PRICEtext = dialog.findViewById(R.id.PRICEtext);
                DISTANCEtext = dialog.findViewById(R.id.DISTANCEtext);
                PERSONtext = dialog.findViewById(R.id.PERSONtext);

                OUTbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        OUTDIALOG();
                    } //TODO : 초기화 순서 잘못됨.
                });
                PAYbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        PAYDIALOG(POINT);
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_taxi);

        init();
        click();

        SharedPreferences positionDATA = getSharedPreferences("positionDATA",MODE_PRIVATE);
        final SharedPreferences.Editor editor = positionDATA.edit();

        Intent intent = getIntent();
        index = Integer.valueOf(intent.getExtras().getString("INDEX"));
        title = positionDATA.getString("TITLE","");
        arrive = positionDATA.getString("ARRIVE","");
        start = positionDATA.getString("START","");
        person = Integer.valueOf(positionDATA.getString("PERSON","1"));
        pay = point = Integer.valueOf(positionDATA.getString("POINT","1000"));
        userID = positionDATA.getString("USERNAME","");
        ID = positionDATA.getString("ID","");

        final int Max = Integer.valueOf(positionDATA.getString("MAX","3"));

        PayPerPerson = point / Max;
        INDEXtext.setText(String.valueOf(index) + " 번");

        // Custom Adapter Instance 생성 및 ListView에 Adapter 지정
        adapter = new My_taxiAdapter();
        LISTview.setAdapter(adapter);

        /*LISTview.addHeaderView();*/
        Query query = mDatabase.child("post-members").orderByChild("index").equalTo(String.valueOf(index));
        final Query query1 = mDatabase.child("post").orderByChild("index").equalTo(String.valueOf(index));

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Data_Members data_members = dataSnapshot.getValue(Data_Members.class);
                    adapter.addItem(data_members.getPROFILEURL(),data_members.getUSER1(),data_members.getGENDER());
                    adapter.notifyDataSetChanged();
                    if(data_members.getJOIN()){
                        Intent intent1 = new Intent(getApplicationContext(),Post_Call.class);
                        intent1.putExtra("INDEX",index);
                        startActivity(intent1);
                        finish();
                    }
                    else{
                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                                    Data_Post data_post = appleSnapshot.getValue(Data_Post.class);
                                    Log.e("COUNT",adapter.getCount()+"");
                                    if (adapter.getCount() == data_post.getMaxPerson()){
                                        POINT = data_post.getPay()/data_post.getMaxPerson();
                                        PAYDIALOG(data_post.getPay()/data_post.getMaxPerson());
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                        PAYdialog.show();
                    }
                }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
    private void PAYDIALOG(final int Point) {
        PAYdialog = new AlertDialog.Builder(this);
        PAYdialog.setTitle("결제 확인");
        PAYdialog.setMessage(Point + "P : 결제 하시겠습니까?\n(결제 후, 택시가 호출됩니다.)");
        PAYdialog.setPositiveButton("결제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Query query = mDatabase.child("user").orderByChild("email").equalTo(ID);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            User user = snapshot.getValue(User.class);
                            String path = "/" + dataSnapshot.getKey() + "/" + snapshot.getKey();
                            Map<String,Object> POINTmap = new HashMap<String,Object>();
                            if(user.getPoint()-Point < 0){
                                CHARGEDIALOG(user.getPoint());
                                CHARGEdialog.show();
                            }
                            else {
                                POINTmap.put("point", user.getPoint() - Point);
                                mDatabase.child(path).updateChildren(POINTmap);
                                Query query1 = mDatabase.child("post-members").orderByChild("index").equalTo(INDEXtext.getText().toString().split(" ")[0]);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Data_Members data_members = snapshot.getValue(Data_Members.class);
                                            String path = "/" + dataSnapshot.getKey() + "/" + snapshot.getKey();
                                            Map<String, Object> JOINmap = new HashMap<String, Object>();
                                            JOINmap.put("join", true);
                                            mDatabase.child(path).updateChildren(JOINmap);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                                Intent intent = new Intent(getApplicationContext(),Post_Call.class);
                                intent.putExtra("INDEX",index);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        });
    }
    void CHARGEDIALOG(final int Point){
        CHARGEdialog = new AlertDialog.Builder(this);
        CHARGEdialog.setTitle("포인트 부족");
        CHARGEdialog.setMessage("포인트가 부족합니다.\n충전하러 가시겠습니까?");
        CHARGEdialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), Charge.class);
                intent.putExtra("POINT",Point+"");
                Log.e("POINT",Point+"");
                startActivity(intent);
            }
        });
    }
    void OUTDIALOG(){
        OUTdialog = new AlertDialog.Builder(this);
        OUTdialog.setTitle("퇴장");
        final Query query = mDatabase.child("post").orderByChild("index").equalTo(INDEXtext.getText().toString());
        final Query query1 = mDatabase.child("post-members").orderByChild("userid").equalTo(ID);
        if(INDEXtext.getText().toString() == ID)
            OUTdialog.setMessage("노선에서 나가시겠습니까?\n(팀원 전체 퇴장됩니다.)");
        else
            OUTdialog.setMessage("노선에서 나가시겠습니까");
        OUTdialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDestroy();
            }
        });
        OUTdialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences positionDATA = getSharedPreferences("positionDATA",MODE_PRIVATE);
                SharedPreferences.Editor editor = positionDATA.edit();
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Data_Post data_post = snapshot.getValue(Data_Post.class);
                            if (ID == data_post.getIndex()){
                                mDatabase.child("post").child(snapshot.getKey()).removeValue();
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                                            mDatabase.child("post-members").child(snapshot1.getKey()).removeValue();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }
                            else{
                                String path = "/" + dataSnapshot.getKey() + "/" + snapshot.getKey();
                                Map<String,Object> taskMap = new HashMap<String,Object>();
                                taskMap.put("person",data_post.getPerson()-1);
                                mDatabase.child(path).updateChildren(taskMap);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                                            mDatabase.child("post-members").child(snapshot1.getKey()).removeValue();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });     //post 갱신,post-members삭제
                //TODO : 퇴장 후, positionDATA 삭제해야됨.
                Intent intent = new Intent(getApplicationContext(),main.class);
                startActivity(intent);
                finish();
            }
        });
    }
    //TODO : 방장 퇴장시, 팀원 데이터베이스 삭제 및 액티비티전환(DB 상태받아와서, 메시지도 띄우기)
    @Override
    public void onConnected(@Nullable Bundle bundle) { }
    @Override
    public void onConnectionSuspended(int i) { }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
    @Override
    public void onCameraIdle() { }
    @Override
    public void onCameraMove() { }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MAPview = googleMap;

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MIDDLElatlng,MIDDLE_ZOOM));
        marker = googleMap.addMarker(new MarkerOptions().position(STARTlatlng).title("출발 위치").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        marker = googleMap.addMarker(new MarkerOptions().position(ARRIVElatlng).title("도착 위치").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

}
