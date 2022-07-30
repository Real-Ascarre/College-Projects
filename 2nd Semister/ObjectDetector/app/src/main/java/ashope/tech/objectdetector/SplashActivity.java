//
// Created by Ascarre on 28-07-2022.
//

package ashope.tech.objectdetector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends Activity {

    //Load the Shared Library in static so every Activity in this package name can use it using JNI (Java Native Interface)
    static {
        System.loadLibrary("Detector");
    }

    //Lottie Animation Variable
    LottieAnimationView Animation;

    native String GetURL();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the Layout
        setContentView(R.layout.splash_activity);

        //Getting the View from its assigned ID and Loading everything while Activity is being created
        Animation = findViewById(R.id.animation);
        Animation.setAnimationFromUrl(GetURL());
        Animation.loop(false);
    }

    //Using onStart to prevent the Animation from starting itself before the layout is visible
    @Override
    protected void onStart() {
        super.onStart();

        //PLay animation automatically view & activity is ready for user to see
        Animation.playAnimation();

        //Delay to do the task. We using this to display the animation and load the other activity when time's up.
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();//So Users can't get back to Splash Again, not even if a bug occurs
        }, 3000);//Delay for 5 Seconds
    }
}
