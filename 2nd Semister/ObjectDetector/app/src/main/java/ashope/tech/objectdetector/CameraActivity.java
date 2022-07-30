//
// Created by Ascarre on 28-07-2022.
//

package ashope.tech.objectdetector;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public CameraActivity() {}

    private Mat Color, Color2;
    private CameraBridgeViewBase CameraView;
    private ObjectDetector Detect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//Keep the screen on

        setContentView(R.layout.activity_camera);//Set the view

        CameraView = (CameraBridgeViewBase) findViewById(R.id.frame_Surface);//Get view from id
        CameraView.setVisibility(SurfaceView.VISIBLE);//Set the view fragment Visible
        //Just a Task Listener function
        CameraView.setCvCameraViewListener(this);

        try {
            //Load the Labels and Objects File from Assets
            Detect = new ObjectDetector(getAssets(), "Models.tflite", "Labels.txt", 300);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //When user decides to hop into the app from recents
    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
    }

    //When user keeps the app in recents
    @Override
    protected void onPause() {
        super.onPause();
        if (CameraView != null) {
            CameraView.disableView();
        }
    }

    //When user closes the app from recents
    public void onDestroy() {
        super.onDestroy();
        if (CameraView != null) {
            CameraView.disableView();
        }
    }

    /******************** CameraBridgeViewBase Functions Start ********************/
    @Override
    public void onCameraViewStarted(int width, int height) {
        Color = new Mat(height, width, CvType.CV_8UC4);
        Color2 = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        Color.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Color = inputFrame.rgba();
        Color2 = inputFrame.gray();
        Mat out = new Mat();
        out = Detect.ScanEnvironment(Color);
        return out;
    }

    /******************** CameraBridgeViewBase Functions Ends ********************/

    //A callback Function
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    CameraView.enableView();
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
}