package com.scatl.uestcbbs;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.just.agentweb.AgentWebConfig;
import com.scatl.uestcbbs.api.ApiConstant;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.SharePrefUtil;
import com.tencent.bugly.crashreport.CrashReport;


import org.litepal.LitePal;

import es.dmoral.toasty.Toasty;

/**
 * author: sca_tl
 * description:
 * date: 2020/1/24 14:24
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        CrashReport.initCrashReport(getApplicationContext(), Constant.BUGLY_ID, false);
        LitePal.initialize(this);
        setUiMode();

        Toasty.Config
                .getInstance()
                .setToastTypeface(Typeface.DEFAULT)
                .setTextSize(14)
                .supportDarkTheme(true)
                .apply();

        if (SharePrefUtil.isLogin(getContext())) {
            if (SharePrefUtil.isSuperLogin(this, SharePrefUtil.getName(getContext()))) {
                for (String s : SharePrefUtil.getCookies(this, SharePrefUtil.getName(getContext()))) {
                    AgentWebConfig.syncCookie(ApiConstant.BBS_BASE_URL, s);
                }
            }
        }
    }

    private void setUiMode() {
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (SharePrefUtil.getUiModeFollowSystem(getApplicationContext())) {
            if (mode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        } else {
            if (SharePrefUtil.isNightMode(getApplicationContext())) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    public static Context getContext() {
        return context;
    }
}
