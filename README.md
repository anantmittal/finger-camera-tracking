# Finger Camera Tracking

An android app which detects a finger moving left to right or right to left while touching the back camera, and prints that direction on the screen.

# Limitations (to fix)

* A direction label is always displayed once initialized, even when there is no motion
* Sometimes, the direction label might be inaccurate when capturing extremely slow or extremely fast motion

# Method

```
Capture live camera frame
Convert frame from RGBA to HSV
Apply two masks to capture red color band in the HSV frame, and do bitwise_or operation to combine them.
Divide the combined mask into 2 halves, left and right. Compute difference (left half - right half) of means of pixel intensity values and store them in memory.
For a sequence of frames, if the sequence of above computed difference decreases from positive to negative, the direction is left-to-right. Else it's right-to-left.
```

# References

https://stackoverflow.com/questions/35111559/capture-video-frames-with-opencv-in-android
https://docs.opencv.org/3.4/db/df6/tutorial_erosion_dilatation.html
https://stackoverflow.com/questions/32522989/opencv-better-detection-of-red-color
https://docs.opencv.org/3.4/da/d97/tutorial_threshold_inRange.html
https://blog.codeonion.com/2016/04/09/show-camera-on-android-app-using-opencv-for-android/
https://stackoverflow.com/questions/10660598/android-camera-preview-orientation-in-portrait-mode
https://stackoverflow.com/questions/46015299/how-to-apply-mask-to-live-camera-in-android-opencv
https://stackoverflow.com/questions/44413952/gradle-implementation-vs-api-configuration
https://stackoverflow.com/questions/17165777/open-the-android-native-camera-using-opencv
https://stackoverflow.com/questions/35753393/how-to-add-image-as-mask-in-camera-frame-android-opencv
