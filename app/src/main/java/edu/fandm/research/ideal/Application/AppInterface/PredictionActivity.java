/**
 * Created by Phyo Thuta Aung
 */

package edu.fandm.research.ideal.Application.AppInterface;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import edu.fandm.research.ideal.Application.Database.DatabaseHandler;
import edu.fandm.research.ideal.Application.Database.PredictedLeak;
import edu.fandm.research.ideal.MachineLearning.TensorFlowClassifier;
import edu.fandm.research.ideal.R;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public class PredictionActivity extends AppCompatActivity {

    private static final int N_SAMPLES = 150;
    private static List<Float> timestamp;
    private static List<Float> appName;
    private static List<Float> leakCategory;
    private static List<Float> leakType;
    private static List<Float> foregroundStatus;
    private static List<Float> awakeStatus;
    private static List<Float> lockedStatus;
    private static List<Float> screenStatus;

    String newTimestamp;
    private ListView list;
    private PredictionListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        timestamp = new ArrayList<>();
        appName = new ArrayList<>();
        leakCategory = new ArrayList<>();
        leakType = new ArrayList<>();
        foregroundStatus = new ArrayList<>();
        awakeStatus = new ArrayList<>();
        lockedStatus = new ArrayList<>();
        screenStatus = new ArrayList<>();

        SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
        String formattedTimestamp = s.format(Calendar.getInstance().getTime());
        newTimestamp = formattedTimestamp.replaceAll(":",".");

        list = (ListView) findViewById(R.id.prediction_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<PredictedLeak> predictions = db.getPredictedLeaks(newTimestamp);

        if (predictions == null) {
            return;
        }

        if (adapter == null) {
            adapter = new PredictionListViewAdapter(this, predictions);

            View header = getLayoutInflater().inflate(R.layout.listview_prediction, null);
            ((TextView) header.findViewById(R.id.predicted_time)).setText(R.string.ptime_label);
            ((TextView) header.findViewById(R.id.predicted_app)).setText(R.string.papp_label);
            /**((TextView) header.findViewById(R.id.predicted_category)).setText(R.string.pcategory_label);
            //((TextView) header.findViewById(R.id.predicted_type)).setText(R.string.ptype_label);*/

            list.addHeaderView(header);
            list.setAdapter(adapter);
        }
        else {
            adapter.updateData(predictions);
        }
    }

    public static void activityPrediction(String aTimestamp, Context context, ArrayList<String> apps) {
        timestamp.add(Float.parseFloat(aTimestamp));

        String[] mapPackageNameToNo = new String[apps.size()]; /// PHYO: Mapped all apps to particular numbers for the purpose of machine learning

        int counter = 0;
        for(String app : apps) {
            mapPackageNameToNo[counter] = app;
            for(int i = 1; i < 4; i++) { /// PHYO: USER = 1, DEVICE = 2, LOCATION = 3
                for(int j = 1; j < 6; j++) { ///PHYO: GPS Coordinates = 1, ZIP Code = 2, IMEI/IMSI/Advertiser ID = 3, MAC Address = 4, Phone = 5
                    appName.add((float) counter);
                    leakCategory.add((float) i);
                    leakType.add((float) j);
                    if(isAppInForeground(context)) {
                        foregroundStatus.add((float) 1);
                    }
                    else {
                        foregroundStatus.add((float) 0);
                    }
                    if(isDeviceAwake(context)) {
                        awakeStatus.add((float) 1);
                    }
                    else {
                        awakeStatus.add((float) 0);
                    }
                    if(isDeviceLocked(context)) {
                        lockedStatus.add((float) 1);
                    }
                    else {
                        lockedStatus.add((float) 0);
                    }
                    if(isScreenOn(context)) {
                        screenStatus.add((float) 1);
                    }
                    else {
                        screenStatus.add((float) 0);
                    }
                }
            }
            counter++;
        }

        if (timestamp.size() == N_SAMPLES && appName.size() == N_SAMPLES && leakCategory.size() == N_SAMPLES && leakType.size() == N_SAMPLES && foregroundStatus.size() == N_SAMPLES && awakeStatus.size() == N_SAMPLES && lockedStatus.size() == N_SAMPLES && screenStatus.size() == N_SAMPLES) {
            List<Float> data = new ArrayList<>();
            data.addAll(timestamp);
            data.addAll(appName);
            data.addAll(leakCategory);
            data.addAll(leakType);
            data.addAll(foregroundStatus);
            data.addAll(awakeStatus);
            data.addAll(lockedStatus);
            data.addAll(screenStatus);

            TensorFlowClassifier classifier = new TensorFlowClassifier(context);
            classifier.predictProbabilities(toFloatArray(data), context, mapPackageNameToNo);

            timestamp.clear();
            appName.clear();
            leakCategory.clear();
            leakType.clear();
            foregroundStatus.clear();
            awakeStatus.clear();
            lockedStatus.clear();
            screenStatus.clear();
        }
    }

    private static float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }


    private static boolean isAppInForeground(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();

            return foregroundTaskPackageName.toLowerCase().equals(context.getPackageName().toLowerCase());
        }
        else
        {
            ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(appProcessInfo);
            if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE)
            {
                return true;
            }

            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            // App is foreground, but screen is locked, so show notification
            return km.inKeyguardRestrictedInputMode();
        }
    }

    private static boolean isDeviceAwake(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenAwake = (Build.VERSION.SDK_INT < 20? powerManager.isScreenOn():powerManager.isInteractive());
        return isScreenAwake;
    }

    private static boolean isDeviceLocked(Context context) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isPhoneLocked = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && myKM.isKeyguardLocked()) || (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && myKM.inKeyguardRestrictedInputMode());
        return isPhoneLocked;
    }

    private static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = powerManager.isInteractive();
        return isScreenOn;
    }

    /**
    public static void installedApps()
    {
        List<PackageInfo> packList = getPackageManager().getInstalledPackages(0);
        for (int i=0; i < packList.size(); i++)
        {
            PackageInfo packInfo = packList.get(i);
            if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
            {
                String appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                Log.e("App № " + Integer.toString(i), appName);
            }
        }
    }
     */
}
