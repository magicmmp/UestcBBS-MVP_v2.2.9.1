package com.scatl.uestcbbs.module.main.view;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.base.BaseActivity;
import com.scatl.uestcbbs.base.BaseEvent;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.entity.OpenPicBean;
import com.scatl.uestcbbs.entity.SettingsBean;
import com.scatl.uestcbbs.entity.UpdateBean;
import com.scatl.uestcbbs.module.main.adapter.MainViewPagerAdapter;
import com.scatl.uestcbbs.module.main.presenter.MainPresenter;
import com.scatl.uestcbbs.module.post.view.CreatePostActivity;
import com.scatl.uestcbbs.module.post.view.HotPostFragment;
import com.scatl.uestcbbs.module.setting.view.SettingsActivity;
import com.scatl.uestcbbs.module.update.view.UpdateFragment;
import com.scatl.uestcbbs.services.HeartMsgService;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.ServiceUtil;
import com.scatl.uestcbbs.util.SharePrefUtil;
import com.scatl.uestcbbs.util.TimeUtil;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends BaseActivity implements MainView{

    private ViewPager2 mainViewpager;
    private AHBottomNavigation ahBottomNavigation;
    private FloatingActionButton floatingActionButton;
    CoordinatorLayout coordinatorLayout;
    private MainPresenter mainPresenter;
    private MainViewPagerAdapter mainViewPagerAdapter;

    Intent intent;

    private int selected;
    private boolean shortCutMessage;

    private int centerX;
    private int centerY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if ("com.scatl.uestcbbs.module.post.view.HotPostFragment".equals(getIntent().getAction())) {
            new Handler().postDelayed(() ->
                    HotPostFragment.getInstance(null)
                    .show(getSupportFragmentManager(), TimeUtil.getStringMs()), 200);
        }
        if ("com.scatl.uestcbbs.module.message.view.MessageFragment".equals(getIntent().getAction())) {
            shortCutMessage = true;
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getIntent(Intent intent) {
        super.getIntent(intent);
        selected = intent.getIntExtra("selected", 0);
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void findView() {
        mainViewpager = findViewById(R.id.main_viewpager);
        ahBottomNavigation = findViewById(R.id.main_bottom_navigation_bar);
        floatingActionButton = findViewById(R.id.main_create_new_post_btn);
        coordinatorLayout = findViewById(R.id.main_coor_layout);
    }

    @Override
    protected void initView() {
        mainPresenter = (MainPresenter) presenter;

        floatingActionButton.setOnClickListener(this);

        ahBottomNavigation.setDefaultBackgroundColor(getColor(R.color.statusbar_color));
        ahBottomNavigation.setBehaviorTranslationEnabled(true);
        ahBottomNavigation.manageFloatingActionButtonBehavior(floatingActionButton);
        ahBottomNavigation.setNotificationBackgroundColor(getColor(R.color.colorPrimary));
        ahBottomNavigation.setAccentColor(getColor(R.color.colorPrimary));
        ahBottomNavigation.addItem(new AHBottomNavigationItem("??????", R.drawable.ic_home));
        ahBottomNavigation.addItem(new AHBottomNavigationItem("??????", R.drawable.ic_boardlist));
        ahBottomNavigation.addItem(new AHBottomNavigationItem("??????", R.drawable.ic_notification));
        ahBottomNavigation.addItem(new AHBottomNavigationItem("??????", R.drawable.ic_mine));

        mainViewPagerAdapter = new MainViewPagerAdapter(this);
        mainViewpager.setAdapter(mainViewPagerAdapter);
        mainViewpager.setUserInputEnabled(false);
        mainViewpager.setOffscreenPageLimit(3);
        mainViewpager.setCurrentItem(selected, false);
        if (shortCutMessage) {
            selected = 2;
            mainViewpager.setCurrentItem(selected, false);
            ahBottomNavigation.setCurrentItem(selected, false);
        } else {
            ahBottomNavigation.setCurrentItem(selected, false);
        }

        floatingActionButton.setVisibility(selected == 0 ? View.VISIBLE : View.GONE);

        startService();
        mainPresenter.getSettings();
        mainPresenter.getOpenPic();
        mainPresenter.getUpdate(CommonUtil.getVersionCode(this), false);
    }

    @Override
    protected BasePresenter initPresenter() {
        return new MainPresenter();
    }

    @Override
    protected void onClickListener(View view) {
        if (view.getId() == R.id.main_create_new_post_btn) {
//            startActivity(new Intent(this, CreatePostActivity.class));
            intent = new Intent(this, CreatePostActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra(Constant.IntentKey.DATA_1, createRect(floatingActionButton));
            startActivity(intent);
        }
    }

    private Rect createRect(View view) {
        Rect rect = new Rect();
        view.getDrawingRect(rect);
        ((ViewGroup) view.getParent()).offsetDescendantRectToMyCoords(view, rect);
        return rect;
    }

    private long t = 0;
    @Override
    protected void setOnItemClickListener() {
        ahBottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            floatingActionButton.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

            //??????
            if (wasSelected && System.currentTimeMillis() - t < 300) {
                //????????????
                EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.HOME_REFRESH));
                return true;
            } else {
                t = System.currentTimeMillis();
                mainViewpager.setCurrentItem(position, false);
            }
            return true;
        });

        mainViewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                ahBottomNavigation.setCurrentItem(position, false);
            }
        });

    }

    @Override
    public void getUpdateSuccess(UpdateBean updateBean) {
        if (updateBean.updateInfo.isValid && updateBean.updateInfo.apkVersionCode > CommonUtil.getVersionCode(this) &&
                updateBean.updateInfo.apkVersionCode != SharePrefUtil.getIgnoreVersionCode(this)) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constant.IntentKey.DATA_1, updateBean);
            UpdateFragment.getInstance(bundle).show(getSupportFragmentManager(), TimeUtil.getStringMs());
        }
    }

    @Override
    public void getUpdateFail(String msg) { }

    @Override
    public void getSettingsSuccess(SettingsBean settingsBean) {
        if (SharePrefUtil.getGraySaturation(this) != settingsBean.graySaturation) {
            SharePrefUtil.setGraySaturation(this, settingsBean.graySaturation);

            Intent intent = new Intent( MainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void getSettingsFail(String msg) { }

    @Override
    public void getOpenPicSuccess(OpenPicBean openPicBean) {
        mainPresenter.showOpenPic(this, openPicBean);
    }

    @Override
    public void getOpenPicsFail(String msg) { }

    @Override
    protected boolean registerEventBus() {
        return true;
    }

    @Override
    public void receiveEventBusMsg(BaseEvent baseEvent) {
        if (baseEvent.eventCode == BaseEvent.EventCode.NIGHT_MODE_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            finish();
            Intent intent = new Intent( MainActivity.this, MainActivity.class);
            intent.putExtra("selected", 3);
            startActivity(intent);
            overridePendingTransition(R.anim.switch_night_mode_fade_in, R.anim.switch_night_mode_fade_out);
        }

        if (baseEvent.eventCode == BaseEvent.EventCode.NIGHT_MODE_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            finish();
            Intent intent = new Intent( MainActivity.this, MainActivity.class);
            intent.putExtra("selected", 3);
            startActivity(intent);
            overridePendingTransition(R.anim.switch_night_mode_fade_in, R.anim.switch_night_mode_fade_out);
        }

        if (baseEvent.eventCode == BaseEvent.EventCode.SET_MSG_COUNT) {
            int msg_count = HeartMsgService.at_me_msg_count +
                    HeartMsgService.private_me_msg_count +
                    HeartMsgService.reply_me_msg_count +
                    HeartMsgService.system_msg_count +
                    HeartMsgService.dianping_msg_count;
            if (msg_count != 0) {
                ahBottomNavigation.setNotification(msg_count + "", 2);
            } else {
                ahBottomNavigation.setNotification("", 2);
            }
        }

        if (baseEvent.eventCode == BaseEvent.EventCode.SWITCH_TO_MESSAGE) {
            selected = 2;
            mainViewpager.setCurrentItem(selected, false);
            ahBottomNavigation.setCurrentItem(selected, false);
        }

        if (baseEvent.eventCode == BaseEvent.EventCode.HOME_NAVIGATION_HIDE) {
            boolean hide = (Boolean) baseEvent.eventData;
            if (hide) {
                ahBottomNavigation.hideBottomNavigation(true);
            } else {
                ahBottomNavigation.restoreBottomNavigation(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, HeartMsgService.class));
    }

    public void startService() {
        if (SharePrefUtil.isLogin(this)) {
            if (!ServiceUtil.isServiceRunning(this, HeartMsgService.serviceName)) {
                Intent intent = new Intent(this, HeartMsgService.class);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(intent);
//                } else {
                    startService(intent);
//                }
            }
        }
    }

}
