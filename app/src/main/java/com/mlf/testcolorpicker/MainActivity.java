package com.mlf.testcolorpicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    private ColorPickerView colorPicker;
    private AppCompatButton butSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorPicker = findViewById(R.id.colorPicker);
        butSelect = findViewById(R.id.buSelect);

        butSelect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int color = colorPicker.getSelectedColor();
                butSelect.setBackgroundColor(color);
            }
        });
    }
}