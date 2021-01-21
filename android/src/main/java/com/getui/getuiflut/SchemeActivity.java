package com.getui.getuiflut;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

public class SchemeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }
}
