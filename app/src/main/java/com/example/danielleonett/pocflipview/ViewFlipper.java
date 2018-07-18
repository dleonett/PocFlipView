package com.example.danielleonett.pocflipview;

import android.view.View;
import android.view.animation.Animation;

public class ViewFlipper {

    private View baseView;
    private View viewBackTop;
    private View viewBackBottom;
    private View viewFrontTop;
    private View viewFrontBottom;

    private Animation animationToMiddle;
    private Animation animationFromMiddle;
    private int lastDigit;
    private int animateFrom;
    private int animateTo;

}
