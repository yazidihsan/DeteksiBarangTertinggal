package ugm.sv.tugasakhir.reminder;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;

public class AlarmActivity extends Activity {
    Button stopAlarm;

    public Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        final MediaPlayer mPlayer = MediaPlayer.create(this,R.raw.alarm_alert);
        mPlayer.setLooping(true);
        mPlayer.start();

        long[] pattern = {300, 600};
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, 0);

        stopAlarm = (Button) findViewById(R.id.alarm_btn_off);
        stopAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mPlayer.stop();
                vibrator.cancel();
            }
        });
    }
}
