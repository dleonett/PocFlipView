package com.example.danielleonett.pocflipview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private FlipTickerView flipView1;
    private FlipTickerView flipView2;
    private FlipTickerView flipView3;

    private int position = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flipView1 = findViewById(R.id.flipView1);
        flipView2 = findViewById(R.id.flipView2);
        flipView3 = findViewById(R.id.flipView3);

        flipView1.setFlipDuration(5000);
        flipView1.setFlipListener(new FlipTickerView.FlipListener() {
            @Override
            public void onFlipStarted() {
                Toast.makeText(MainActivity.this, "onFlipStarted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFlipEnded() {
                Toast.makeText(MainActivity.this, "onFlipEnded", Toast.LENGTH_SHORT).show();
            }
        });

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

                flipView1.smoothFlipToView(view);

                view = inflater.inflate(R.layout.view_digit, null);
                tv = view.findViewById(R.id.position_in_line_digit);
                tv.setText(String.valueOf(position));

                flipView2.smoothFlipToView(view);

                view = inflater.inflate(R.layout.view_digit, null);
                tv = view.findViewById(R.id.position_in_line_digit);
                tv.setText(String.valueOf(position));

                flipView3.smoothFlipToView(view);
            }
        });
    }

}
