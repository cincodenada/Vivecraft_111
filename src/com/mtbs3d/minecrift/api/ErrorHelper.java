package com.mtbs3d.minecrift.api;

/**
 * Created by StellaArtois on 2/7/2016.
 */
public class ErrorHelper {
    public long createdTime;
    public long endTime;
    public String title;
    public String message;
    public String resolution;

    public ErrorHelper(String title, String message, String resolution, long displayTimeSecs)
    {
        this.title = "\u00a7e\u00a7l" + title;
        this.message = message;
        this.resolution = resolution;
        this.createdTime = System.currentTimeMillis();
        this.endTime = this.createdTime + (displayTimeSecs * 1000);
    }
}
