package edu.fandm.research.ideal.Application.Database;

import java.text.ParseException;
import java.util.Date;

public class PredictedLeak {

    private String timestamp;
    private Date timestampDate;
    //private String packageName;
    private String appName;
    //private String category;
    //private String type;
    //private int foregroundStatus;
    //private int awakeStatus;
    //private int lockedStatus;
    //private int screenStatus;

    /**
    public PredictedLeak(String timestamp, String packageName, String appName, String category, String type, int foregroundStatus, int awakeStatus, int lockedStatus, int screenStatus){
        this.packageName = packageName;
        this.appName = appName;
        this.category = category;
        this.type = type;
        this.timestamp = timestamp;
        this.foregroundStatus = foregroundStatus;
        this.awakeStatus = awakeStatus;
        this.lockedStatus = lockedStatus;
        this.screenStatus = screenStatus;

        try {
            this.timestampDate = DatabaseHandler.getDateFormat().parse(timestamp);
        }
        catch (ParseException ex) {
            throw new RuntimeException("Invalid timestamp for DataLeak, tried to parse: " + timestamp);
        }
    }
     */

    public PredictedLeak(String timestamp, String appName){
        this.appName = appName;
        this.timestamp = timestamp;

        try {
            this.timestampDate = DatabaseHandler.getDateFormat().parse(timestamp);
        }
        catch (ParseException ex) {
            throw new RuntimeException("Invalid timestamp for DataLeak, tried to parse: " + timestamp);
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Date getTimestampDate() {
        return timestampDate;
    }

    public String getAppName() {
        return appName;
    }

    /**
    public String getPackageName() {
        return packageName;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public int getForegroundStatus() {
        return foregroundStatus;
    }

    public int getAwakeStatus() {
        return awakeStatus;
    }

    public int getLockedStatus() {
        return lockedStatus;
    }

    public int getScreenStatus() {
        return screenStatus;
    }
     */
}
