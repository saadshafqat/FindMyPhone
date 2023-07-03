package com.saarimtech.findmyphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NotificationHelper {
    private Context context;
    private Double Lattitude, Longitude;
    List<AddressModel> addressList = new ArrayList<>();

    public NotificationHelper(Context context, Double lattitude, Double longitude) {
        this.context = context;
        Lattitude = lattitude;
        Longitude = longitude;
    }


    public void showNotification() {
        Intent intent = new Intent(context, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent);
        PendingIntent pendingintent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder notificationBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(context, App.Channel_ID)
                    .setContentTitle("Fetched Your Location: ")
                    .setContentText(addressList.get(0).getAddress())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(false)
                    .setContentIntent(pendingintent);
        }
        getNotificationManager().notify(0, notificationBuilder.build());
    }

    private NotificationManager getNotificationManager() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    public void decodeLocations(double latitude, double longitude) {
        Geocoder geocoder;

        List<Address> addresses;
        geocoder = new Geocoder(context.getApplicationContext(), Locale.getDefault());

        try {
            addressList.clear();
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            addressList.add(new AddressModel(address, city, state, country, postalCode, knownName));
            saveUserLocation();
            showNotification();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void saveUserLocation() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Phones").child(FirebaseAuth.getInstance().getUid()).child(getEmi());
        HashMap<Object, String> map = new HashMap<>();
        map.put("key", FirebaseAuth.getInstance().getUid());
        map.put("devicename", getDeviceName());
        map.put("lattitude", String.valueOf(this.Lattitude));
        map.put("longitude", String.valueOf(this.Longitude));
        ref.setValue(map);

    }

    public String getEmi() {
       return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
    public  String getDeviceName() {
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

}


