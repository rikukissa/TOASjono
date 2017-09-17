package fi.naf.toasjono;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.facebook.stetho.Stetho;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);

        setContentView(R.layout.activity_main);

        TextView time = (TextView) findViewById(R.id.textView3);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        time.setText(prefs.getString("lastsaved", "?"));

        getQueue();
        scheduleAlarm();
    }

    public void scheduleAlarm() {
        Intent intent = new Intent(this, AlarmReceiver.class);

        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }

    public void getQueue() {
        DBHandler db = new DBHandler(getApplicationContext());

        TableLayout ll = (TableLayout) findViewById(R.id.toasTable);
        ll.removeAllViews();

        List<TOASPosition> toasList = db.getAllToas();

        Collections.sort(toasList, new Comparator<TOASPosition>() {
            @Override
            public int compare(TOASPosition lhs, TOASPosition rhs) {
                return lhs.getPos() - rhs.getPos();
            }
        });

        if(toasList.isEmpty()) {
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);

            TextView tv = new TextView(this);
            tv.setTextSize(22);
            tv.setText("TOASjono on tyhjä. Odota sen päivittymistä tai tarkista kirjautumistietosi.");

            row.addView(tv);
            ll.addView(row);
        } else {
            int i = 0;
            for (TOASPosition apartment : toasList) {
                TableRow row = new TableRow(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(lp);

                TextView tv = new TextView(this);
                tv.setTextSize(20);
                tv.setText(apartment.toString());

                TextView tvi = new TextView(this);
                tvi.setTextSize(22);
                tvi.setText(Integer.toString(apartment.getPos()));
                tvi.setPadding(16, 16, 0, 0);

                row.addView(tv);
                row.addView(tvi);

                ll.addView(row, i);

                i++;
            }
        }
    }
}
