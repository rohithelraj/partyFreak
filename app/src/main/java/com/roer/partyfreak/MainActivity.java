package com.roer.partyfreak;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class MainActivity extends AppCompatActivity implements ColorPicker.OnColorChangedListener {
    public static int currentColorValue = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        ColorPicker picker = (ColorPicker) findViewById(R.id.picker);
        SVBar svBar = (SVBar) findViewById(R.id.svbar);
        ValueBar valueBar = (ValueBar) findViewById(R.id.valuebar);
        picker.addSVBar(svBar);
        picker.addValueBar(valueBar);
        //To get the color
        picker.getColor();
        //To set the old selected color u can do it like this
        picker.setOldCenterColor(picker.getColor());
        // adds listener to the colorpicker which is implemented
        //in the activity
        picker.setOnColorChangedListener(this);
        //to turn of showing the old color
        picker.setShowOldCenterColor(false);
        TextView colorValue = findViewById(R.id.colorValue);
        ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
        colorValue.setText(""+picker.getColor());
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorValue.setText(""+picker.getColor());
            }
        });
    }

    @Override
    public void onColorChanged(int color) {
        currentColorValue = color;
    }
}