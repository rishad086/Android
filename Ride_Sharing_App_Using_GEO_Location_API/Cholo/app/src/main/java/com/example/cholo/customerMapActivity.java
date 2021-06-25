
package com.example.cholo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.NotificationManager;
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

import com.example.cholo.R;
import com.example.cholo.WelcomeActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class customerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private Button msettings;
    GoogleApiClient mgoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private SupportMapFragment mapFragment;
    private Button mLogout,mRequest;
    private String destination;
    private LatLng pickUpLocation;
    private  Boolean isLoggingOut=false;
    private Boolean requestBol =false;
    private Marker pickupMarker;

    private TextView txtName,txtPhone,txtCarname;
    private CircleImageView profilePic;
    private LinearLayout relativelayout;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);







        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(customerMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);

        }
        txtName=(TextView)findViewById(R.id.drivername) ;
        txtPhone=(TextView)findViewById(R.id.driverPhone) ;
        txtCarname=(TextView)findViewById(R.id.driverCar) ;
        profilePic=(CircleImageView)findViewById(R.id.profileImage) ;
        relativelayout=(LinearLayout)findViewById(R.id.driverInfo) ;


        msettings=(Button)findViewById(R.id.settings) ;
        msettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(customerMapActivity.this,InfoActivity.class);
                intent.putExtra("type","Customers");
                startActivity(intent);
            }
        });
        String apikey=getString(R.string.api_key);
        if(!Places.isInitialized())
        {
            Places.initialize(getApplicationContext(),apikey);
        }
        PlacesClient placesClient=Places.createClient(this);


        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(24.92, 89.96), new LatLng(24.92, 89.96)
        ));
        autocompleteFragment.setCountry("BAN");

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG, Place.Field.NAME));
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG, Place.Field.NAME);

        // Start the autocomplete intent.




        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                destination=place.getName().toString();

            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.

            }
        });

        mLogout=(Button)findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut=true;
                logOutDriver();

                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(customerMapActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mRequest=(Button)findViewById(R.id.request) ;
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestBol)
                {
                    requestBol=false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverlocationRefListener);

                    if(driverFoundId !=null)
                    {
                        DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("User").child("Drivers").child(driverFoundId);
                        driverRef.setValue(true);
                        driverFoundId=null;
                    }
                    driverfound=false;
                    radius=1;
                    if(pickupMarker !=null)
                    {
                        pickupMarker.remove();
                    }
                    if(mDriverMarker!=null)
                    {
                        mDriverMarker.remove();
                    }
                    mRequest.setText("call ride");
                    relativelayout.setVisibility(View.GONE);

                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequest");
                    GeoFire geoFire=  new GeoFire(ref);
                    geoFire.removeLocation(userId);
                }
                else {
                    requestBol=true;
                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequest");
                    GeoFire geoFire=  new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                    pickUpLocation= new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    pickupMarker= mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("pick Up here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_cus_foreground)));
                    mRequest.setText("getting ur driver");

                    nearestDriver();
                }


            }
        });


    }
    private int radius = 1;
    private Boolean driverfound=false;
    private String driverFoundId;

    GeoQuery geoQuery;

    private void nearestDriver() {
        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("Driver Available");
        GeoFire geoFire=new GeoFire(driverLocation);
        geoQuery=geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude,pickUpLocation.longitude),radius);
        geoQuery.removeAllListeners();


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverfound && requestBol){
                    driverfound=true;
                    driverFoundId=key;

                    DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("User").child("Drivers").child(driverFoundId);
                    String customerId=FirebaseAuth.getInstance().getCurrentUser().getUid();

                    HashMap map=new HashMap();
                    map.put("customerRideId",customerId);
                    driverRef.updateChildren(map);
                    getDriverLocation();
                    mRequest.setText("looking for driver location");


                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverfound)
                {
                    radius=radius+1;
                    nearestDriver();
                }


            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }
    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverlocationRefListener;

    private void getDriverLocation()
    {
        driverLocationRef=FirebaseDatabase.getInstance().getReference().child("DriversWorking").child(driverFoundId).child("l");
        driverlocationRefListener= driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && requestBol)
                {
                    List<Object> map=(List<Object>)snapshot.getValue();
                    double locationLat=0;
                    double locationLng=0;
                    mRequest.setText("Driver Found");
                    relativelayout.setVisibility(View.VISIBLE);
                    getAssignedDriverInformation();
                    if(map.get(0)!=null)
                    {
                        locationLat =Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null)
                    {
                        locationLng =Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverlatlng=new LatLng(locationLat,locationLng);
                    if(mDriverMarker!=null)
                    {
                        mDriverMarker.remove();
                    }
                    Location loc1=new Location("");
                    loc1.setLatitude(pickUpLocation.latitude);
                    loc1.setLongitude(pickUpLocation.longitude);

                    Location loc2=new Location("");
                    loc2.setLatitude(driverlatlng.latitude);
                    loc2.setLongitude(driverlatlng.longitude);

                    float distance=loc1.distanceTo(loc2);
                    String s=Float. toString(distance);
                    mRequest.setText("DriverFound"+s);
                    if(distance<1000)
                    {
                        mRequest.setText("Driver is Here");
                        NotificationCompat.Builder mbuilder=( NotificationCompat.Builder) new NotificationCompat.Builder(customerMapActivity.this).setSmallIcon(R.drawable.c3).setContentTitle("Notification").setContentText("Driver is here");
                        NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(0,mbuilder.build());
                    }
                    else {
                        mRequest.setText("Driver Found"+(distance));
                    }



                    mDriverMarker=mMap.addMarker(new MarkerOptions().position(driverlatlng).title("your Driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.car1_foreground)));



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

            ActivityCompat.requestPermissions(customerMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
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
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));



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

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("CustomerRequest ");

        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(userId);

    }
    private void getAssignedDriverInformation()
    {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("User").child("Drivers").child(driverFoundId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0)
                {
                    String name=snapshot.child("name").getValue().toString();
                    String phone=snapshot.child("phone").getValue().toString();
                    String car=snapshot.child("car").getValue().toString();
                    txtName.setText(name);
                    txtPhone.setText(phone);
                    txtCarname.setText(car);



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