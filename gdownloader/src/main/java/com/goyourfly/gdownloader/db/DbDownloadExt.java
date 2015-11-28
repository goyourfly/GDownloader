package com.goyourfly.gdownloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by gaoyf on 15/6/9.
 */
public class DbDownloadExt {
    public static final int DOWNLOAD_STATE_NOT_DOWNLOAD = 0;
    public static final int DOWNLOAD_STATE_DOWNLOADED = 2;
    public static final int DOWNLOAD_STATE_DOWNLOADING = 3;
    public static final int DOWNLOAD_STATE_PAUSE = 4;
    public static final int DOWNLOAD_STATE_ERROR = 5;
    public static final int DOWNLOAD_STATE_WAITING = 6;
    public static final int DOWNLOAD_STATE_PREPARING = 7;

    public static final String TABLE_VIDEO_DOWNLOAD = "video_download";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DOWNLOAD_URL = "url";
    public static final String COLUMN_DOWNLOAD_STATE = "state";
    public static final String COLUMN_DOWNLOAD_PROGRESS = "progress";
    public static final String COLUMN_DOWNLOAD_FILE_SIZE = "fileSize";
    public static final String COLUMN_DOWNLOAD_LAST_UPDATE_TIME = "updateTime";

    protected String url;
    protected int downloadState;
    protected long downloadProgress, fileSize, lastUpdateTime;


    public long getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(long downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static class Helper {
        protected SQLiteDatabase mDb;
        protected static Helper mHelper;

        private Helper(Context context) {
            mDb = new DbVideoExtOpenHelper(context).getWritableDatabase();
        }

        public static Helper getInstance(Context context) {
            if (mHelper == null)
                mHelper = new Helper(context);
            return mHelper;
        }

        public String insertOrUpdate(final DbDownloadExt dbDownloadExt) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_DOWNLOAD_URL, dbDownloadExt.url);
            contentValues.put(COLUMN_DOWNLOAD_STATE, dbDownloadExt.downloadState);
            contentValues.put(COLUMN_DOWNLOAD_PROGRESS, dbDownloadExt.downloadProgress);
            contentValues.put(COLUMN_DOWNLOAD_FILE_SIZE, dbDownloadExt.fileSize);
            contentValues.put(COLUMN_DOWNLOAD_LAST_UPDATE_TIME, System.currentTimeMillis());
            Cursor cursor = mDb.query(TABLE_VIDEO_DOWNLOAD, null, COLUMN_DOWNLOAD_URL + " = ?", new String[]{dbDownloadExt.url}, null, null, null);
            if (cursor.moveToNext()) {
                cursor.close();
                mDb.update(TABLE_VIDEO_DOWNLOAD, contentValues, COLUMN_DOWNLOAD_URL + " = ?", new String[]{dbDownloadExt.url});
            } else {
                cursor.close();
                mDb.insert(TABLE_VIDEO_DOWNLOAD, null, contentValues);
            }
            return dbDownloadExt.url;
        }

        public String updateProgress(final String url, final long progress) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_DOWNLOAD_PROGRESS, progress);
            contentValues.put(COLUMN_DOWNLOAD_LAST_UPDATE_TIME, System.currentTimeMillis());
            mDb.update(TABLE_VIDEO_DOWNLOAD, contentValues, COLUMN_DOWNLOAD_URL + " = ?", new String[]{url});
            return url;
        }

        public String updateState(final String url, final int state) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_DOWNLOAD_STATE, state);
            contentValues.put(COLUMN_DOWNLOAD_LAST_UPDATE_TIME, System.currentTimeMillis());
            mDb.update(TABLE_VIDEO_DOWNLOAD, contentValues, COLUMN_DOWNLOAD_URL + " = ?", new String[]{url});
            return url;
        }

        public String updateFileLength(final String url, final long length) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_DOWNLOAD_FILE_SIZE, length);
            contentValues.put(COLUMN_DOWNLOAD_LAST_UPDATE_TIME, System.currentTimeMillis());
            mDb.update(TABLE_VIDEO_DOWNLOAD, contentValues, COLUMN_DOWNLOAD_URL + " = ?", new String[]{url});
            return url;
        }

        public void delete(String url) {
            mDb.delete(TABLE_VIDEO_DOWNLOAD, COLUMN_DOWNLOAD_URL + " = ?", new String[]{url});
        }

        public DbDownloadExt queryDownloadExt(final String url) {
            if (url == null)
                return null;
            Cursor cursor = mDb.query(TABLE_VIDEO_DOWNLOAD, null, COLUMN_DOWNLOAD_URL + " = ?", new String[]{url}, null, null, null);
            if (cursor.moveToNext()) {
                DbDownloadExt dbDownloadExt = cursorToDb(cursor);
                cursor.close();
                return dbDownloadExt;
            }
            cursor.close();
            return null;
        }

        private DbDownloadExt cursorToDb(Cursor cursor){
            DbDownloadExt dbDownloadExt = new DbDownloadExt();
            dbDownloadExt.url = cursor.getString(cursor.getColumnIndex(COLUMN_DOWNLOAD_URL));
            dbDownloadExt.downloadProgress = cursor.getLong(cursor.getColumnIndex(COLUMN_DOWNLOAD_PROGRESS));
            dbDownloadExt.fileSize = cursor.getLong(cursor.getColumnIndex(COLUMN_DOWNLOAD_FILE_SIZE));
            dbDownloadExt.downloadState = cursor.getInt(cursor.getColumnIndex(COLUMN_DOWNLOAD_STATE));
            dbDownloadExt.lastUpdateTime = cursor.getLong(cursor.getColumnIndex(COLUMN_DOWNLOAD_LAST_UPDATE_TIME));
            return dbDownloadExt;
        }

        public List<DbDownloadExt> queryAllDownloadExt() {
            List<DbDownloadExt> list = new ArrayList<>();
            Cursor cursor = mDb.query(TABLE_VIDEO_DOWNLOAD, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                DbDownloadExt dbDownloadExt = cursorToDb(cursor);
                list.add(dbDownloadExt);
            }
            cursor.close();
            return list;
        }

        public List<DbDownloadExt> queryAllNotDownloadedExt() {
            List<DbDownloadExt> list = new ArrayList<>();
            Cursor cursor = mDb.query(TABLE_VIDEO_DOWNLOAD, null, COLUMN_DOWNLOAD_STATE + " != ?",
                    new String[]{DOWNLOAD_STATE_DOWNLOADED + ""}, null, null, null);
            while (cursor.moveToNext()) {
                DbDownloadExt dbDownloadExt = cursorToDb(cursor);
                list.add(dbDownloadExt);
            }
            cursor.close();
            return list;
        }

        public List<DbDownloadExt> queryAllDownloadedExt() {
            List<DbDownloadExt> list = new ArrayList<>();
            Cursor cursor = mDb.query(TABLE_VIDEO_DOWNLOAD, null, COLUMN_DOWNLOAD_STATE + " = ?",
                    new String[]{DOWNLOAD_STATE_DOWNLOADED + ""}, null, null, null);
            while (cursor.moveToNext()) {
                DbDownloadExt dbDownloadExt = cursorToDb(cursor);
                list.add(dbDownloadExt);
            }
            cursor.close();
            return list;
        }

        public void clear() {
            mDb.delete(TABLE_VIDEO_DOWNLOAD, null, null);
        }
    }

}
