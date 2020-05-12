package edu.fandm.research.ideal.Application.Database;

import android.annotation.TargetApi;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import edu.fandm.research.ideal.Application.Helpers.PermissionsHelper;

@TargetApi(22)
public class UpdateLeakForegroundStatus extends AsyncTask<Long, Void, Void> {
    private Context context;

    public UpdateLeakForegroundStatus(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected Void doInBackground(Long... params) {
        // To run this task, build version must be valid and usage access permission must be granted.
        if (!PermissionsHelper.validBuildVersionForAppUsageAccess() ||
                !PermissionsHelper.hasUsageAccessPermission(context)) {
            return null;
        }

        long id = params[0];
        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(context);
        DataLeak leak = databaseHandler.getLeakById(id);

        long leakTime = leak.getTimestampDate().getTime();

        UsageStatsManager usageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = (new Date()).getTime();

        UsageEvents usageEvents = usageStatsManager.queryEvents(currentTime - TimeUnit.DAYS.toMillis(1), currentTime);

        UsageEvents.Event lastEvent = null;
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);
            if (event.getTimeStamp() > leakTime) break;

            if (event.getPackageName().equals(leak.getPackageName()) &&
                    (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                     event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND)) {
                lastEvent = event;
            }
        }

        if (lastEvent == null) {
            // Some applications will leak information without a user ever opening the application.
            // For example, some applications will listen for an internet connection and then
            // run a service in the background without the application ever being opened. In this case,
            // there will be no status events to classify this leak, and we classify it as background.
            databaseHandler.setDataLeakStatus(id, DatabaseHandler.BACKGROUND_STATUS);
        }
        else if (lastEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            databaseHandler.setDataLeakStatus(id, DatabaseHandler.FOREGROUND_STATUS);
        }
        else if (lastEvent.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
            databaseHandler.setDataLeakStatus(id, DatabaseHandler.BACKGROUND_STATUS);
        }
        else {
            throw new RuntimeException("A leak's status should always be classified by this task.");
        }

        return null;
    }
}
