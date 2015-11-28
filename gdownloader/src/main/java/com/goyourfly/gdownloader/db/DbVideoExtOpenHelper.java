package com.goyourfly.gdownloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by gaoyf on 15/6/9.
 */
public class DbVideoExtOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "GDOWNLOADER";
    public static final int DATABASE_VERSION = 1;

    public DbVideoExtOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer stringBufferVideoDownload = new StringBuffer();
        stringBufferVideoDownload
                .append("CREATE TABLE ")
                .append(DbDownloadExt.TABLE_VIDEO_DOWNLOAD).append("(")
                .append(DbDownloadExt.COLUMN_ID).append(" integer primary key autoincrement ").append(",")
                .append(DbDownloadExt.COLUMN_DOWNLOAD_URL).append(" string unique").append(",")
                .append(DbDownloadExt.COLUMN_DOWNLOAD_STATE).append(" integer").append(",")
                .append(DbDownloadExt.COLUMN_DOWNLOAD_PROGRESS).append(" long").append(",")
                .append(DbDownloadExt.COLUMN_DOWNLOAD_FILE_SIZE).append(" long").append(",")
                .append(DbDownloadExt.COLUMN_DOWNLOAD_LAST_UPDATE_TIME).append(" long")
                .append(")").append(";");
        db.execSQL(stringBufferVideoDownload.toString());


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
