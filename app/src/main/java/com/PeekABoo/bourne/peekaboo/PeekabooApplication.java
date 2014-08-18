package com.PeekABoo.bourne.peekaboo;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by bournelipardo on 8/13/14.
 */
public class PeekabooApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "hQ6hf1pAArPxvodg46bzcxgdn1ZQIY5ocQotHHmr",
                "6fhkCvaHmB2L5Vi8hM0x3eoIPrjdy8VHyuE7XsDd");

    }
}
