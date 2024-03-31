package com.wsd.wappblocker;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;
import java.util.Calendar;

public class SearchRunningAppService extends AccessibilityService {
    String appName;
    ArrayList<String> strings;
    ArrayList<Integer> isPerm;
    public ArrayList<String> LIST_BLOCK = new ArrayList<>();

    public SearchRunningAppService(){
        LIST_BLOCK.add("Browser");
        LIST_BLOCK.add("Chrome");
        LIST_BLOCK.add("Hinge");
        LIST_BLOCK.add("OkCupid");
        LIST_BLOCK.add("Bumble");
        LIST_BLOCK.add("Tinder");
        LIST_BLOCK.add("Badoo");
        LIST_BLOCK.add("EliteSingles");
        LIST_BLOCK.add("SilverSingles");
        LIST_BLOCK.add("Parship");
        LIST_BLOCK.add("UK Dating");
        LIST_BLOCK.add("Boo");
        LIST_BLOCK.add("Feeld");
        LIST_BLOCK.add("Ashley Madison");
        LIST_BLOCK.add("Xmatch");
        LIST_BLOCK.add("be2");
        LIST_BLOCK.add("Kismia");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Log.i("TopAct", am.getRunningTasks(1).get(0).topActivity.flattenToShortString());
        boolean app_state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("app_off", false);
        if (!app_state) {
            String runningPackName = event.getPackageName().toString();
            for (String b : LIST_BLOCK) {
                if (runningPackName.toUpperCase().contains(b.toUpperCase()) == true) {
                    blockApp();
                    break;
                }
            }
        }
    }

    private void blockApp() {
//        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        startActivity(new Intent(getApplicationContext(), BlockActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onInterrupt() {

    }
}
