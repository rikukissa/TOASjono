package fi.naf.toasjono;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "fi.naf.toasjono.alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, ToasService.class);
        context.startService(i);
    }
}