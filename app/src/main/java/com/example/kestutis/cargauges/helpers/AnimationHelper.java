package com.example.kestutis.cargauges.helpers;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class AnimationHelper {

    public static void rotateAround(View view, int repeatCount) {
        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration((long) 1000);
        rotateAnimation.setRepeatCount(repeatCount);
        view.startAnimation(rotateAnimation);
    }

    public static void rotate(View view,  float fromDegrees, float toDegrees){
        RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF,  0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(100);
        rotate.setFillAfter(true);
        view.startAnimation(rotate);
    }
}
