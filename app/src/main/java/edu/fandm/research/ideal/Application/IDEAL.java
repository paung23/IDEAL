package edu.fandm.research.ideal.Application;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.logging.Level;

import edu.fandm.research.ideal.Utilities.AndroidLoggingHandler;

public class IDEAL extends Application {
    public final static String EXTRA_DATA = "IDEAL.DATA";
    public final static String EXTRA_ID = "IDEAL.id";
    public final static String EXTRA_APP_NAME = "IDEAL.appName";
    public final static String EXTRA_PACKAGE_NAME = "IDEAL.packageName";
    public final static String EXTRA_CATEGORY = "IDEAL.category";
    public final static String EXTRA_IGNORE = "IDEAL.ignore";
    public final static String EXTRA_SIZE = "IDEAL.SIZE";
    public final static String EXTRA_DATE_FORMAT = "IDEAL.DATE";
    public static boolean doFilter = true;
    public static boolean asynchronous = true;
    public static int tcpForwarderWorkerRead = 0;
    public static int tcpForwarderWorkerWrite = 0;
    public static int socketForwarderWrite = 0;
    public static int socketForwarderRead = 0;

    private static Application sApplication;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getAppContext() {
        return getApplication().getApplicationContext();
    }//TODO:Nullable?

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        // make sure that java logging messages end up in logcat
        AndroidLoggingHandler loggingHandler = new AndroidLoggingHandler();
        AndroidLoggingHandler.reset(loggingHandler);
        loggingHandler.setLevel(Level.FINEST);
    }
}
