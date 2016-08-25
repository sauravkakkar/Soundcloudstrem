package com.eldhose.soundcloudstrem;

import android.support.annotation.IntRange;

import java.util.HashMap;

/**
 * Created by ELDHOSE on 2016-08-19.
 */
public class Pager {
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";

    public static final int LIMIT_DEFAULT = 50;
    public static final int LIMIT_MAX = 200;

    private HashMap<String, String> queryMap;
    private int limit = LIMIT_DEFAULT;
    private int offset = 0;

    public Pager(Query query) {
        this.queryMap = query.createMap();

        updateLimit(limit);
        updateOffset(offset);
    }

    public Pager(Query query, @IntRange(from = 1, to = 200) int pageSize) {
        this.queryMap = query.createMap();

        this.limit = pageSize;

        updateLimit(limit);
        updateOffset(offset);
    }

    /**
     * Updates the query offset by subtracting the page size from the current offset.
     * @return The updated query map to get the previous result set.
     */
    public HashMap<String, String> previous() {
        updateOffset(offset - limit);

        return queryMap;
    }

    public HashMap<String, String> next() {
        updateOffset(offset + limit);

        return queryMap;
    }

    public void setPageSize(int pageSize) {
        updateLimit(pageSize);
    }

    public void setOffset(int offset) {
        updateOffset(offset);

        if(offset < 0) {
            returnToStart();
        }
    }

    public void reset() {
        updateOffset(0);
        updateLimit(LIMIT_DEFAULT);
    }

    public void returnToStart() {
        updateOffset(0);
    }

    private void updateLimit(@IntRange(from = 1, to = 200) int limit) {
        this.limit = limit;

        queryMap.put(LIMIT, String.valueOf(limit));
    }

    private void updateOffset(int offset) {
        this.offset = offset;

        queryMap.put(OFFSET, String.valueOf(offset));
    }
}
