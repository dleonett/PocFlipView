package com.example.danielleonett.pocflipview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView viewBackTop;
    TextView viewBackBottom;
    TextView viewFrontTop;
    TextView viewFrontBottom;
    Button btnFlip;
    FlipView flipView;

    Animation animationToMiddle;
    Animation animationFromMiddle;
    int lastDigit;
    int animateFrom;
    int animateTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewBackTop = findViewById(R.id.viewBackTop);
        viewBackBottom = findViewById(R.id.viewBackBottom);
        viewFrontTop = findViewById(R.id.viewFrontTop);
        viewFrontBottom = findViewById(R.id.viewFrontBottom);

        btnFlip = findViewById(R.id.btnFlip);
        btnFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnimationToMiddle();
            }
        });

        animationToMiddle = AnimationUtils.loadAnimation(this, R.anim.flip_point_to_middle);
        animationToMiddle.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                viewFrontTop.setVisibility(View.VISIBLE);
                viewFrontBottom.setVisibility(View.INVISIBLE);

                setDigitImage(getDigitToShow(), viewFrontBottom);
                setDigitImage(getDigitToShow(), viewBackTop);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewFrontTop.setVisibility(View.INVISIBLE);
                startAnimationFromMiddle();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationFromMiddle = AnimationUtils.loadAnimation(this, R.anim.flip_point_from_middle);
        animationFromMiddle.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                viewFrontBottom.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewFrontTop.setVisibility(View.VISIBLE);

                incrementFromCode();

                setDigitImage(animateFrom, viewFrontTop);
                setDigitImage(animateFrom, viewBackBottom);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        flipView = findViewById(R.id.flipView);
        flipView.flipToView(View.inflate(this, R.layout.view_digit, null));
    }

    private void startAnimationToMiddle() {
        viewFrontTop.clearAnimation();
        viewFrontTop.setAnimation(animationToMiddle);
        viewFrontTop.startAnimation(animationToMiddle);
    }

    private void startAnimationFromMiddle() {
        viewFrontBottom.clearAnimation();
        viewFrontBottom.setAnimation(animationFromMiddle);
        viewFrontBottom.startAnimation(animationFromMiddle);
    }

    public void setDigit(int digit) {
        if (digit < 0) {
            digit = 0;
        }
        if (digit > 9) {
            digit = 9;
        }

        animateTo = digit;

        animateDigit();
    }

    private void animateDigit() {
        animateFrom = lastDigit;

        startAnimationToMiddle();
    }

    private void setDigitImage(int digitToShow, TextView textView) {
        textView.setText(String.valueOf(digitToShow));
    }

    private int getDigitToShow() {
        if (animateFrom + 1 > 9)
            return 0;
        else
            return animateFrom + 1;
    }

    private void incrementFromCode() {
        animateFrom++;
        if (animateFrom < 0)
            animateFrom = 9;

        if (animateFrom > 9)
            animateFrom = 0;

    }

}
