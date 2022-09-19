package com.rui.annotiondemo;

import android.app.Activity;

public class MyButterKnife{

    public static void bind(Activity activity) {
        String name = activity.getClass().getName() + "_ViewBinding";

        try {
            Class<?> clzss = Class.forName(name);
            IBinder iBinder = (IBinder) clzss.newInstance();
            iBinder.bind(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
