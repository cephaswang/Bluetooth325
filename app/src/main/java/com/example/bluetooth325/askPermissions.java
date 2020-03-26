package com.example.bluetooth325;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.HashSet;
import java.util.Set;


public class askPermissions {

    private final static int REQ_PERMISSIONS = 0;

    void askPermissions(Context context) {

        String[] PERMISSION_S = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };


        Set<String> permissionsRequest = new HashSet<>();
        for (String permission : PERMISSION_S) {
            int result = ContextCompat.checkSelfPermission(context, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsRequest.add(permission);
            }
        }

        if (!permissionsRequest.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) context,
                    permissionsRequest.toArray(new String[permissionsRequest.size()]),
                    REQ_PERMISSIONS);
        }
        System.out.println("permissionsRequest:" + permissionsRequest.size());
    }

    @SuppressLint("Override")
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSIONS:
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        //String text = getString( R.string.text_ShouldGrant) + " : "+ result ;
                        //Toast.makeText(this, text, Toast.LENGTH_LONG).show();

                        //handler.postDelayed(GotoMenu, 4000);
                        return;
                    }
                }
                break;
        }
    }
}
