//
// Created by Ascarre on 28-07-2022.
//

package ashope.tech.objectdetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    int Request_Permission = 0;//Flag for Permission

    //BackPressed required Variables
    int Time_Delay = 2000;//2000 = 2 Sec
    long Back_Pressed;

    Button Start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check for Permissions if not given then ask for it
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Request_Permission);
        }

        setContentView(R.layout.activity_main);//Set the view

        Start = findViewById(R.id.Start_Capture);//Get the View from its Assigned Id
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Check if OpenCv Loaded Successfully else show a toast
        if(!OpenCVLoader.initDebug()){
            //Show a floating message at bottom
            Toast.makeText(MainActivity.this, "Error Occured! Please restart Application", Toast.LENGTH_SHORT).show();
        }

        //Not using onclickListener as this way its cool and fast to code
        Start.setOnClickListener(V->{
            startActivity(new Intent(MainActivity.this, CameraActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));//Clear previous activities from history
        });
    }

    //When user presses back button
    @Override
    public void onBackPressed() {
        if (Back_Pressed + Time_Delay > System.currentTimeMillis()) {
            super.onBackPressed();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());//Close the app when double back pressed 2 times within 2 sec
        } else {
            //Message to show when pressed back once
            Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
        }
        Back_Pressed = System.currentTimeMillis();
    }

}