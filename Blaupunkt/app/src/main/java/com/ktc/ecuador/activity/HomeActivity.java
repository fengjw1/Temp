package com.ktc.ecuador.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.ktc.ecuador.R;
import com.ktc.ecuador.fragment.AppsFragment;
import com.ktc.ecuador.fragment.DemandFragment;
import com.ktc.ecuador.fragment.HomeFragment;
import com.ktc.ecuador.fragment.SearchFragment;
import com.ktc.ecuador.utils.CommonUtils;
import com.ktc.ecuador.utils.FontUtils;
import com.ktc.ecuador.utils.ImageUtils;
import com.ktc.ecuador.view.SFragmentTabHost;
import com.ktc.ecuador.viewmodel.HomeViewModel;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class HomeActivity extends AppCompatActivity implements SFragmentTabHost.OnTabChangeListener {
    private final static String IS_POWER_ON_PROPERTY = "mstar.launcher.1stinit";
    public SFragmentTabHost tabHost;
    public String appsTagTd;
    private TextView tv_head_bar_time;
    private TextView tv_head_bar_date;
    private String[] tabTexts;
    private String[] tabTags;
    private SFragmentTabHost.onTabPageActiveEvent tabPageActiveEvent;
    private ImageView iv_title_bar_setting;
    private int lastTabSelectId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ImageUtils.setScreenDensity(getWindowManager());
        initView();
        initViewModel();
    }

    private void initView() {
        iv_title_bar_setting = this.findViewById(R.id.iv_title_bar_setting);
        tv_head_bar_time = this.findViewById(R.id.tv_head_bar_time);
        tv_head_bar_date = this.findViewById(R.id.tv_head_bar_date);
        tabHost = this.findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.contentLayout);
        tabHost.getTabWidget().setDividerDrawable(null);
        tabHost.setOnTabChangedListener(this);
        initTab();
        tabPageActiveEvent = new SFragmentTabHost.onTabPageActiveEvent() {
            @Override
            public void onTagPageActive() {
                iv_title_bar_setting.setAlpha(0.5f);
                for (int i = 0; i < tabTexts.length; i++) {
                    if (i != lastTabSelectId) {
                        tabHost.getTabWidget().getChildTabViewAt(i).setAlpha(0.5f);
                    }
                }
            }

            @Override
            public void onTagPageInActive() {
                iv_title_bar_setting.setAlpha(1.0f);
                for (int i = 0; i < tabTexts.length; i++) {
                    tabHost.getTabWidget().getChildTabViewAt(i).setAlpha(1.0f);
                }
                tabHost.getTabWidget().getChildTabViewAt(lastTabSelectId).requestFocus();
            }
        };
        tabHost.setTabPageActiveEvent(tabPageActiveEvent);

        iv_title_bar_setting.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
                if (i == KeyEvent.KEYCODE_DPAD_CENTER || i == KeyEvent.KEYCODE_ENTER) {
                    CommonUtils.startActivityForAction(getApplicationContext(), "android.settings.SETTINGS");
                }
                return false;

            }
        });

        iv_title_bar_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.startActivityForAction(getApplicationContext(), "android.settings.SETTINGS");
            }
        });
        initFontStyle();
    }

    private void initViewModel() {
        //更新时间控件
        Observer<String> mTimeTickObserver = new Observer<String>() {
            @Override
            public void onChanged(String newTime) {
                tv_head_bar_time.setText(newTime.substring(11));
                tv_head_bar_date.setText(newTime.substring(0,10));
            }
        };
        HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        homeViewModel.getTimeTickLiveData().observe(this, mTimeTickObserver);
    }

    private void initTab() {
        Class[] tabFragments = new Class[]{SearchFragment.class, HomeFragment.class, AppsFragment.class, DemandFragment.class};
        tabTexts = getResources().getStringArray(R.array.tabTxts);
        tabTags = getResources().getStringArray(R.array.tabTag);
        for (int i = 0; i < tabTexts.length; i++) {
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(tabTags[i]);
            tabSpec.setIndicator(buildIndicator(tabTexts[i]));
            tabHost.addTab(tabSpec, tabFragments[i], null);
            tabHost.setTag(i);
            if (i == 1) {
                tabHost.onTabChanged(tabSpec.getTag());
            }
            if (i == 2) {
                appsTagTd = tabSpec.getTag();
            }
        }
        tabHost.setCurrentTab(1);
    }

    private void initFontStyle() {
        FontUtils.getInstance(this).setRegularFont(tv_head_bar_time);
    }

    private View buildIndicator(String txt) {
        View tabView = View.inflate(this, R.layout.item_tab_layout, null);
        TextView tv_tab_txt = tabView.findViewById(R.id.tv_tab_txt);
        FontUtils.getInstance(this).setRegularFont(tv_tab_txt);
        tv_tab_txt.setText(txt);
        return tabView;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkoutOOBE();
    }

    private void checkoutOOBE() {

        SystemProperties.set("com.jrm.localmm", "true");
        SystemProperties.set(IS_POWER_ON_PROPERTY, "true");
        boolean startSetup = SystemProperties.getBoolean("persist.sys.startsetup", false);
        if (startSetup) {
            CommonUtils.startAppForComponent(this, "cn.ktc.android.oobe", "cn.ktc.android.oobe.StartupActivity");
        }
        SystemProperties.set("mstar.str.storage", "0");
        TvCommonManager tvCommonManager = TvCommonManager.getInstance();
        if (tvCommonManager != null) {
            if (tvCommonManager.getCurrentTvInputSource() != TvCommonManager.INPUT_SOURCE_STORAGE) {
                tvCommonManager.setInputSource(TvCommonManager.INPUT_SOURCE_STORAGE);
            }
        }
        Settings.System.putInt(getContentResolver(), "home_hot_key_disable", 0);
        try {
            TvManager.getInstance().setTvosCommonCommand("SetAutoSleepOffStatus");
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTabChanged(String tabId, int position) {
        lastTabSelectId = position;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String command) {
        if ("up".equals(command)) {
            tabPageActiveEvent.onTagPageInActive();
        }
    }
}
