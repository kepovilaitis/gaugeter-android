package com.example.kestutis.cargauges.helpers;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class AnimationHelper {

    public static RotateAnimation getRotateInfinitely() {
        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration((long) 1000);
        return rotateAnimation;
    }

    public static RotateAnimation getRotate(int repeatCount) {
        RotateAnimation rotateAnimation = getRotateInfinitely();
        rotateAnimation.setRepeatCount(repeatCount);
        return rotateAnimation;
    }

    public static RotateAnimation getRotateToAngle(float fromDegrees, float toDegrees) {
        RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF,  0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(100);
        rotate.setFillAfter(true);
        return rotate;
    }
}
