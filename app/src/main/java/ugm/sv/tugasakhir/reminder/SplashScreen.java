package ugm.sv.tugasakhir.reminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        int SPLASH_TIME_OUT = 5000;
        new Handler().postDelayed(new Runnable(){
            public void run(){
                Intent defaultIntent = new Intent(SplashScreen.this, DeviceScanActivity.class);
                startActivity(defaultIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}