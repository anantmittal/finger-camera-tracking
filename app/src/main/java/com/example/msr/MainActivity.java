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
    private static final int MORPH_RECT = 0;
    private static final int MORPH_CROSS = 1;
    private static final int MORPH_ELLIPSE = 2;

    /** References to the UI widgets used in this demo app. */
    private TextView mLeftRightDirectionTextView;

    private DiffGradientCalculator mDetector;

    private CameraBridgeViewBase mOpenCvCameraView;

    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    Mat tempImg;
    Mat imgHSV;
    Mat imgThresholded;

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
        //Log.i(TAG, "called onCreate");
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
        // CHeck if open cv is initialized.
        if (!OpenCVLoader.initDebug()) {
            //Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            //Log.d(TAG, "OpenCV library found inside package. Using it!");
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
        imgThresholded = new Mat(width, width, CvType.CV_8UC4);
        mDetector = new DiffGradientCalculator();

        tempImg = new Mat(0, 0, 0);


    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //Log.d(TAG, "onCameraFrame ");
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, 1 );


        tempImg.setTo(new Scalar(0,0,0,255));
        Imgproc.cvtColor(mRgba, imgHSV, Imgproc.COLOR_RGB2HSV);

        Mat mask1 = new Mat();
        Mat mask2 = new Mat();

        /*Mat mDilatedMask1 = new Mat();
        Mat mErodedMask1 = new Mat();

        Mat mDilatedMask2 = new Mat();
        Mat mErodedMask2 = new Mat();

        Size kernelDilate = new Size(8, 8);
        Size kernelErode = new Size(3, 3);*/

        Core.inRange(imgHSV, new Scalar(0, 70, 50), new Scalar(10, 255, 255), mask1);
        //Imgproc.erode(mask1, mErodedMask1, getStructuringElement(MORPH_RECT, kernelErode));
        //Imgproc.dilate(mErodedMask1, mDilatedMask1, getStructuringElement(MORPH_RECT, kernelDilate));

        Core.inRange(imgHSV, new Scalar(170, 70, 50), new Scalar(180, 255, 255), mask2);
        //Imgproc.erode(mask2, mErodedMask2, getStructuringElement(MORPH_RECT, kernelErode));
        //Imgproc.dilate(mErodedMask2, mDilatedMask2, getStructuringElement(MORPH_RECT, kernelDilate));

        Mat mask_combined = new Mat();
        //Core.bitwise_or(mDilatedMask1, mDilatedMask2, mask_combined);
        Core.bitwise_or(mask1, mask2, mask_combined);

        // Calculate direction of gradient with mask_combined itself.
        mDetector.calculateDirectionGradient(mask_combined);
        Double sumDiffGrad = mDetector.getDirectionGradient();
        runOnUiThread(new Runnable() {
            public void run() {
                displayTrackingDirection(sumDiffGrad);
            }
        });

        //return mask_combined;

        return mRgba; // Return the original camera preview feed.

        /*
        Mat image_masked = new Mat();
        Core.bitwise_and(inputFrame.rgba(), inputFrame.rgba(), image_masked, mask_combined);

        mDetector.calculateDirectionGradient(image_masked);
        Double sumDiffGrad = mDetector.getDirectionGradient();
        runOnUiThread(new Runnable() {
            public void run() {
                displayTrackingDirection(sumDiffGrad);
            }
        });

        // We want to send the original frame to camera. For some reason it needs to be rotated again.
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, 1 );
        return mRgba;*/
    }

}
