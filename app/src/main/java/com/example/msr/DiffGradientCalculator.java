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

    private List<Double> directionGradient = new ArrayList<Double>();

    public void calculateDirectionGradient(Mat combinedMask) {

        // Create rectangle to crop left part of the image.
        Rect roiLeft = new Rect(0, 0, combinedMask.width()/2, combinedMask.height());

        // Create rectangle to crop right part of the image.
        Rect roiRight = new Rect(combinedMask.width()/2, 0, combinedMask.width()/2, combinedMask.height());

        // LEFT CROPPED IMAGE
        Mat croppedLeft = new Mat(combinedMask, roiLeft);
        MatOfDouble meanSrcLeft = new MatOfDouble();
        MatOfDouble stdSrcLeft = new MatOfDouble();
        // Calculate mean and standard deviation of the values in cropped left image and store them.
        Core.meanStdDev(croppedLeft, meanSrcLeft, stdSrcLeft);

        // Right CROPPED IMAGE
        Mat croppedRight = new Mat(combinedMask, roiRight);
        MatOfDouble meanSrcRight= new MatOfDouble();
        MatOfDouble stdSrcRight = new MatOfDouble();
        // Calculate mean and standard deviation of the values in cropped right image and store them.
        Core.meanStdDev(croppedRight, meanSrcRight, stdSrcRight);

        // Only 1 channel because rgbaImage is a mask.
        directionGradient.add(meanSrcLeft.get(0,0)[0] - meanSrcRight.get(0,0)[0]);
    }

    public Double getDirectionGradient(){
        // Get last 30 frames
        List<Double> tail = directionGradient.subList(Math.max(directionGradient.size() - 30, 0), directionGradient.size());

        List<Double> diffGrad = new ArrayList<Double>();

        for (int i = 0; i < tail.size()-1; i++){
            // Store difference of consecutive values in tail into diffGrad
            diffGrad.add(tail.get(i) - tail.get(i+1));
        }

        // Sum all the values in diffGrad and return it.
        double sumDiffGrad = 0;
        for(int i = 0; i < diffGrad.size(); i++){
            sumDiffGrad += diffGrad.get(i);
        }
        return sumDiffGrad;
    }
}
