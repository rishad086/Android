package com.example.cholo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class driverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mgoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private Button mLogout,settingsDriverButton;
    private  Boolean isLoggingOut=false;
    private String customerId="";
    private TextView txtName,txtPhone;
    private CircleImageView profilePic;
    private LinearLayout relativelayout;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);







        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(driverMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);

        }
        settingsDriverButton=(Button)findViewById(R.id.driver_settings);
        settingsDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(driverMapActivity.this,InfoActivity.class);
                intent.putExtra("type","Drivers");
                startActivity(intent);
            }
        });
        relativelayout=(LinearLayout)findViewById(R.id.customerInfo);
        profilePic=(CircleImageView)findViewById(R.id.profileImage);
        txtName=(TextView)findViewById(R.id.customername) ;
        txtPhone=(TextView)findViewById(R.id.customerPhone) ;

        mLogout=(Button)findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut=true;
                logOutDriver();

                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(driverMapActivity.this,WelcomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        getAssignedCustomer();


    }

    private void getAssignedCustomer() {
        String driverId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("User").child("Drivers").child(driverId).child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {


                    customerId=snapshot.getValue().toString();
                    getAssignedCustomerPicupLocation();
                    relativelayout.setVisibility(View.VISIBLE);
                    getAssignedCustomerInformation();

                }
                else {
                    customerId="";
                    if(pickupMarker!=null)
                    {
                        pickupMarker.remove();
                    }
                    if(assignedCustomerPicupLocationRefListener !=null)
                    {
                        assignedCustomerPicupLocationRef.removeEventListener(assignedCustomerPicupLocationRefListener);

                    }
                    relativelayout.setVisibility(View.GONE);




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    Marker pickupMarker;
    private DatabaseReference assignedCustomerPicupLocationRef;
    private ValueEventListener assignedCustomerPicupLocationRefListener;


    private void getAssignedCustomerPicupLocation() {
        assignedCustomerPicupLocationRef=FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customerId).child("l");

        assignedCustomerPicupLocationRefListener= assignedCustomerPicupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && !customerId.equals(""))
                {
                    List<Object> map=(List<Object>) snapshot.getValue();
                    double locationLat=0;
                    double locationLng=0;

                    if(map.get(0)!=null)
                    {
                        locationLat =Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null)
                    {
                        locationLng =Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverlatlng=new LatLng(locationLat,locationLng);


                    pickupMarker=  mMap.addMarker(new MarkerOptions().position(driverlatlng).title("pickup location ").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_cus_foreground)));


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }
    protected synchronized void buildGoogleApiClient()
    {
        mgoogleApiClient=new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mgoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(driverMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(getApplicationContext()!=null){
            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("Driver Available");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriversWorking");

            GeoFire geoFireAvailable=new GeoFire(refAvailable);
            GeoFire geoFireWorking=new GeoFire(refWorking);



            switch (customerId)
            {
                case "":
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailable.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }}








    }
    final  int LOCATION_REQUEST_CODE=1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    mapFragment.getMapAsync(this);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"please provide the permission",Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private  void logOutDriver()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleApiClient,this);
        String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Driver Available");

        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(userId);

    }
    private void getAssignedCustomerInformation()
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("User").child("Customers").child(customerId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0)
                {
                    String name=snapshot.child("name").getValue().toString();
                    String phone=snapshot.child("phone").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);




                    if(snapshot.hasChild("image"))
                    {
                        String image=snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isLoggingOut)
        {
            logOutDriver();
        }


    }
}