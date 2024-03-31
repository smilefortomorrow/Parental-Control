package com.wsd.wappblocker;

import static android.content.Context.MODE_PRIVATE;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class SearchRunningApp extends BroadcastReceiver {

    Context ct;

    public ArrayList<String> LIST_BLOCK = new ArrayList<>();

    public SearchRunningApp(){
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
    public void onReceive(Context aContext, Intent anIntent) {
        ct = aContext;

        try {
            ct = aContext;
            ActivityManager am = (ActivityManager) aContext
                    .getSystemService(Context.ACTIVITY_SERVICE);

            int i = 0;
            String s;
            s = am.getRunningTasks(1).get(0).topActivity.flattenToShortString();

            for (String b : LIST_BLOCK) {
                if (s.toUpperCase().contains(b.toUpperCase()) == true){
                    blockApp();
                    break;
                }
            }
        } catch (Throwable t) {
        }
    }

    private void blockApp() {
        //ct.startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        ct.startActivity(new Intent(ct, BlockActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
