package com.saarimtech.findmyphone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.suke.widget.SwitchButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    CircularImageView profileimage;
    ImageView myLocationicon;
    SwitchButton switchButton;
    TextView deviceName;
    TextView userName;
    String username;
    RecyclerView recyclerView;
    List<DeviceListModel> devicelist=new ArrayList<>();
    DeviceListAdapter deviceListAdapter;
    private InterstitialAd mInterstitialAd;
    public static final int PERMISSION_REQUEST_CODE = 9001;

    public static final int PLAY_SERVICES_ERROR_CODE = 9002;
    public static final int RESULT_CODE = 9003;
    private AdView mAdView;


    FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileimage = findViewById(R.id.circularImageView);
        myLocationicon = findViewById(R.id.imageView2);
        switchButton = findViewById(R.id.switch_button);
        deviceName=findViewById(R.id.txtDeviceName);
        userName=findViewById(R.id.textView2);
        recyclerView=findViewById(R.id.recyclerView);
        mAdView = findViewById(R.id.adView);



        AdRequest adRequest = new AdRequest.Builder().build();






        mAdView.loadAd(adRequest);

        checklocationpermissions();
        setlisteners();
        setConstants();

        deviceListAdapter=new DeviceListAdapter(MainActivity.this,devicelist);
        recyclerView.setAdapter(deviceListAdapter);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {


            }
        });


        adListeners();

        AdRequest intadRequest=new  AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-2912838182609237/5677017565",intadRequest ,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("MainActivity", "onAdLoaded");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d("MainActivity", "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d("MainActivity", "Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e("MainActivity", "Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.

                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d("MainActivity", "Ad showed fullscreen content.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d("MainActivity", loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });

    }

    private void adListeners() {
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.

            }

            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        });




    }

    private void setConstants() {
        deviceName.setText(getDeviceName());
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
          getuserName();
            getUserDevices();
        }else{
            if(deviceListAdapter!=null){
                devicelist.clear();
                deviceListAdapter.notifyDataSetChanged();
                switchButton.setChecked(false);
                putSwitchStatus(false);
            }
        }

    }

    private void getUserDevices() {
        devicelist.clear();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Phones").child(FirebaseAuth.getInstance().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                devicelist.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                    String devicename = dataSnapshot.child("devicename").getValue(String.class);
                    if(!devicename.equalsIgnoreCase(getDeviceName())) {
                        String key = dataSnapshot.child("key").getValue(String.class);
                        String lattitude = dataSnapshot.child("lattitude").getValue(String.class);
                        String longitude = dataSnapshot.child("longitude").getValue(String.class);
                        devicelist.add(new DeviceListModel(devicename, key, lattitude, longitude));
                   }

                }
                if(devicelist.isEmpty()){
                    deviceListAdapter.notifyDataSetChanged();

                }else{
                    deviceListAdapter.notifyItemRangeInserted(devicelist.size(),devicelist.size());
                    recyclerView.smoothScrollToPosition(devicelist.size() - 1);
                }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setlisteners() {
        switchButton.setChecked(getSwitchStatus());
        if (getSwitchStatus()) {
            if(!LocationPermissions()) {
                if(isUserLogin()) {
                    Intent intent = new Intent(MainActivity.this, BackgroundLocationService.class);
                    ContextCompat.startForegroundService(MainActivity.this, intent);
                }
                else{
                    Toast.makeText(MainActivity.this, "Login first to save device locations", Toast.LENGTH_SHORT).show();
                }
            }
            else
                requestpermission();
        }
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                putSwitchStatus(isChecked);
                if (getSwitchStatus()) {
                    if(!LocationPermissions()) {
                        if(isUserLogin()) {
                            //getLocationUpdates();
                            Intent intent = new Intent(MainActivity.this, BackgroundLocationService.class);
                            ContextCompat.startForegroundService(MainActivity.this, intent);
                        }
                        else{

                            Toast.makeText(MainActivity.this, "Login first to save device locations", Toast.LENGTH_SHORT).show();
                            putSwitchStatus(false);
                            Toast.makeText(MainActivity.this, "Unable to start, restart switch button", Toast.LENGTH_SHORT).show();

                        }

                    }
                    else {
                        requestpermission();
                    }

                } else {
                    if(isMyServiceRunning(BackgroundLocationService.class)){
                        Intent intent=new Intent(MainActivity.this,BackgroundLocationService.class);
                        stopService(intent);
                    }



                }
            }
        });

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                    Intent intent=new Intent(MainActivity.this,EditProfile.class);
                    startActivity(intent);

                }else {
                    Intent intent = new Intent(MainActivity.this, SignUp.class);
                    startActivity(intent);
                }
            }
        });

        myLocationicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isServicesOk()) {
                    if (isGPSEnabled()) {
                        if (!LocationPermissions()) {
                            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                            startActivity(intent);
                        } else {
                            requestpermission();
                        }
                    }
                }

            }
        });

    }

    private boolean isUserLogin() {
        auth=FirebaseAuth.getInstance();
        FirebaseUser user=auth.getCurrentUser();
        if(user==null) {
            return false;
        }
        else {
            return true;
        }
    }



    private void ShowLocation(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            Toast.makeText(MainActivity.this, "City: " + city + " Country: " + country, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void checklocationpermissions() {
        if (isServicesOk()) {
            if (isGPSEnabled()) {
                if (!LocationPermissions()) {

                } else {
                    requestpermission();
                }
            }
        }
    }

    private boolean LocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED;
    }

    private boolean isServicesOk() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int result = googleApiAvailability.isGooglePlayServicesAvailable(MainActivity.this);

        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, PLAY_SERVICES_ERROR_CODE, task -> Toast.makeText(MainActivity.this, "Dialog cancelled by user!", Toast.LENGTH_SHORT).show());
            dialog.show();
        } else {
            Toast.makeText(MainActivity.this, "Restricted Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void requestpermission() {
        String permList[] = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        requestPermissions(permList, PERMISSION_REQUEST_CODE);

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (providerEnabled) {
            return true;
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Location Service")
                    .setMessage("Location Service is neccessary for app, please enable Location Service")
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, RESULT_CODE);
                        }
                    }).setCancelable(false).show();
            return false;

        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


        } else {
            Toast.makeText(this, "Permission not granted, Turn it on from settings", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CODE) {
            if (isGPSEnabled()) {
                Toast.makeText(MainActivity.this, "GPS Enabled", Toast.LENGTH_SHORT).show();
            } else {
                switchButton.setChecked(false);
                putSwitchStatus(false);
                Toast.makeText(MainActivity.this, "GPS not Enabled", Toast.LENGTH_SHORT).show();
            }
        }


    }

    void putSwitchStatus(Boolean status) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        prefs.edit().putBoolean("locked", status).apply();

    }


    private boolean getSwitchStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        return prefs.getBoolean("locked", false);

    }



    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }
    private String getuserName() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 username=snapshot.child("username").getValue(String.class);
                 userName.setText(username);
                 String profilepic=snapshot.child("profilepic").getValue(String.class);
                 Glide.with(getApplicationContext()).load(profilepic).into(profileimage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return username;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
       setConstants();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setConstants();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}