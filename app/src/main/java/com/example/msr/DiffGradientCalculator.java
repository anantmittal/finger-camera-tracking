package com.example.msr;

import android.util.Log;

import androidx.core.math.MathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class DiffGradientCalculator {

    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
    private List<Double> directionGradient = new ArrayList<Double>();


    public void calculateDirectionGradient(Mat rgbaImage) {
        //Log.d("channels", String.valueOf(rgbaImage.channels()));
        //Log.d("size", String.valueOf(rgbaImage.size()));

        Rect roiLeft = new Rect(0, 0, rgbaImage.width()/2, rgbaImage.height());
        Rect roiRight = new Rect(rgbaImage.width()/2, 0, rgbaImage.width()/2, rgbaImage.height());


        //List<Mat> labSrc = new ArrayList<Mat>(4);
        //Core.split(rgbaImage, labSrc);

        // LEFT CROPPED IMAGE
        Mat croppedLeft = new Mat(rgbaImage, roiLeft);
        MatOfDouble meanSrcLeft = new MatOfDouble();
        MatOfDouble stdSrcLeft = new MatOfDouble();
        Core.meanStdDev(croppedLeft, meanSrcLeft, stdSrcLeft);
        //Log.d("Left meansrc", meanSrcLeft.dump());
        //Log.d("Left meanval4", String.valueOf(meanSrcLeft.get(3,0)[0]));

        //Log.d("Left meanval1", String.valueOf(meanSrcLeft.get(0,0)[0]));
        //Log.d("Left meanval2", String.valueOf(meanSrcLeft.get(1,0)[0]));
        //Log.d("Left meanval3", String.valueOf(meanSrcLeft.get(2,0)[0]));

        // Right CROPPED IMAGE
        Mat croppedRight = new Mat(rgbaImage, roiRight);
        MatOfDouble meanSrcRight= new MatOfDouble();
        MatOfDouble stdSrcRight = new MatOfDouble();
        Core.meanStdDev(croppedRight, meanSrcRight, stdSrcRight);
        //Log.d("Right meansrc", meanSrcRight.dump());
        //Log.d("Right meanval4", String.valueOf(meanSrcRight.get(3,0)[0]));

        directionGradient.add(meanSrcLeft.get(3,0)[0] - meanSrcRight.get(3,0)[0]);

        //Log.d("Left meanval1", String.valueOf(meanSrcLeft.get(0,0)[0]));
        //Log.d("Left meanval2", String.valueOf(meanSrcLeft.get(1,0)[0]));
        //Log.d("Left meanval3", String.valueOf(meanSrcLeft.get(2,0)[0]));

    }

    public Double getDirectionGradient(){
        //return directionGradient;
        List<Double> tail = directionGradient.subList(Math.max(directionGradient.size() - 30, 0), directionGradient.size());
        //Log.d("directionGradient", String.valueOf(tail));

        List<Double> diffGrad = new ArrayList<Double>();

        // Iterating using for loop

        for (int i = 0; i < tail.size()-1; i++){

            // Printing and display the elements in ArrayList
            //System.out.print(tail.get(i) + " ");
            diffGrad.add(tail.get(i) - tail.get(i+1));
        }

        double sumDiffGrad = 0;
        for(int i = 0; i < diffGrad.size(); i++){
            sumDiffGrad += diffGrad.get(i);
        }
        //Log.d("SumDIFFGRAD", String.valueOf(sumDiffGrad));
        return sumDiffGrad;
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }
}
