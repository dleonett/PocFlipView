package com.example.danielleonett.pocflipview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private FlipView flipView1;
    private FlipView flipView2;
    private FlipView flipView3;

    private int position = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flipView1 = findViewById(R.id.flipView1);
        flipView2 = findViewById(R.id.flipView2);
        flipView3 = findViewById(R.id.flipView3);

        findViewById(R.id.btnFlip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position--;
                if (position < 0) {
                    position = 9;
                }

                View view;
                TextView tv;
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

                view = inflater.inflate(R.layout.view_digit, null);
                tv = view.findViewById(R.id.position_in_line_digit);
                tv.setText(String.valueOf(position));

                flipView1.flipToView(view);

                view = inflater.inflate(R.layout.view_digit, null);
                tv = view.findViewById(R.id.position_in_line_digit);
                tv.setText(String.valueOf(position));

                flipView2.flipToView(view);

                view = inflater.inflate(R.layout.view_digit, null);
                tv = view.findViewById(R.id.position_in_line_digit);
                tv.setText(String.valueOf(position));

                flipView3.flipToView(view);
            }
        });
    }

}
