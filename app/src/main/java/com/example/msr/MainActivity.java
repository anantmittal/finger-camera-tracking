package com.example.msr;

import static org.opencv.imgproc.Imgproc.getStructuringElement;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String TAG = "Finger Camera Tracking";

    /** References to the UI widgets used in this demo app. */
    private TextView mLeftRightDirectionTextView;

    private DiffGradientCalculator mDetector;
    private CameraBridgeViewBase mOpenCvCameraView;

    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    Mat imgHSV;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        mLeftRightDirectionTextView = findViewById(R.id.left_right_direction_value);

        // mOpenCvCameraView is the camera view which we set on layout id.
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(480, 640);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    /** Displays the left to right direction. */
    @SuppressLint("DefaultLocale")
    public void displayTrackingDirection(double getDirectionGradient) {
        Log.d(TAG, String.valueOf(getDirectionGradient));
        if (getDirectionGradient > 45) {
            mLeftRightDirectionTextView.setText(String.format("%s", "Left -> Right"));
        }
        else if (getDirectionGradient < -45){
            mLeftRightDirectionTextView.setText(String.format("%s", "Right -> Left"));
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // Check if open cv is initialized.
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        // Disable the camera view base if it's not null.
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        // This is called before onCameraFrame.
        // This is the channel of the image.
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
        imgHSV = new Mat(width, width, CvType.CV_8UC4);
        mDetector = new DiffGradientCalculator();
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mRgbaF.release();
        mRgbaT.release();
        imgHSV.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees clockwise.
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, 1 );

        // RGBA to HSV
        Imgproc.cvtColor(mRgba, imgHSV, Imgproc.COLOR_RGB2HSV);

        // RED Mask 1
        Mat mask1 = new Mat();
        Core.inRange(imgHSV, new Scalar(0, 70, 50), new Scalar(10, 255, 255), mask1);

        // RED Mask 2
        Mat mask2 = new Mat();
        Core.inRange(imgHSV, new Scalar(170, 70, 50), new Scalar(180, 255, 255), mask2);

        // Combine red mask 1 and 2
        Mat mask_combined = new Mat();
        Core.bitwise_or(mask1, mask2, mask_combined);

        // Calculate direction of gradient with mask_combined itself.
        mDetector.calculateDirectionGradient(mask_combined);
        Double sumDiffGrad = mDetector.getDirectionGradient();

        // This is required because displayTrackingDirection updates the UI.
        runOnUiThread(new Runnable() {
            public void run() {
                displayTrackingDirection(sumDiffGrad);
            }
        });
        return mRgba; // Return the original camera preview feed.
    }
}
