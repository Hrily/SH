package com.hrily.sh;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by hrishi on 9/6/16.
 */
public class Utils {
    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {

        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

}