package com.mp3.launcher4.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.activities.bases.BaseActivity;
import com.mp3.launcher4.customs.views.SliderView;
import com.mp3.launcher4.customs.views.complex.BaseComplexProxy;
import com.mp3.launcher4.customs.views.complex.ComplexView;
import com.mp3.launcher4.holders.HomeInstalledHolder;
import com.mp3.launcher4.proxys.CategoryProxy;
import com.mp3.launcher4.proxys.ExploreProxy;
import com.mp3.launcher4.proxys.InstalledProxy;
import com.mp3.launcher4.proxys.NewerProxy;
import com.mp3.launcher4.proxys.RecentProxy;
import com.mp3.launcher4.proxys.SourceProxy;
import com.mp3.launcher4.proxys.TrendingProxy;
import com.mp3.launcher4.proxys.VideoProxy;
import com.mp3.launcher4.services.AppsChangeService;
import com.mp3.launcher4.utils.CommonUtils;
import com.mp3.launcher4.utils.FontUtils;
import com.mp3.launcher4.utils.ImageUtils;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author longzj
 */
public class HomeActivity extends BaseActivity implements InstalledProxy.OnMenuStateChangeListener {

    private final static String IS_POWER_ON_PROPERTY = "mstar.launcher.1stinit";
    private ComplexView mContentComplex;
    private SliderView mSliderView;
    private TextView mHeaderTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        mContentComplex = (ComplexView) findViewById(R.id.home_content);
        mSliderView = (SliderView) findViewById(R.id.home_slider);
        mHeaderTips = (TextView) findViewById(R.id.header_tips_text);
        FontUtils fontUtils = FontUtils.getInstance(this);
        fontUtils.setRegularFont(mHeaderTips);
        ImageUtils.setScreenDensity(getWindowManager());
        initContent();
        initSlider();
        initHeaderTips();
        startService(new Intent(this, AppsChangeService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkoutOOBE();
        mContentComplex.onActivityResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContentComplex.onActivityDestroy();
    }

    @Override
    protected void onCheckNetworkState(boolean hasNetwork) {
        mContentComplex.onNetworkConnected(hasNetwork);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContentComplex.onActivityPaused();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mContentComplex.onActivityStopped();
    }

    @Override
    public void onBackPressed() {
        //屏蔽back处理
        //super.onBackPressed();
    }

    private void initContent() {
        List<BaseComplexProxy> data = new ArrayList<>();
        data.add(new SourceProxy(this));
        data.add(new TrendingProxy(this, R.string.slide_trending));
        InstalledProxy installedProxy = new InstalledProxy(this, R.string.menu_installed);
        installedProxy.addMenuStateChangeListener(this);
        data.add(installedProxy);
        data.add(new RecentProxy(this, R.string.menu_recent_used));
        data.add(new ExploreProxy(this, R.string.menu_explore));
        data.add(new NewerProxy(this, R.string.menu_new_app));
        data.add(new VideoProxy(this, R.string.menu_recommended_movie));
        data.add(new CategoryProxy(this, R.string.menu_demand_categories));
        mContentComplex.setData(data);
    }

    private void initSlider() {
        mSliderView.bindComplexRecyclerView(mContentComplex);
        mSliderView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSliderView.shrink();
            }
        }, 1200);
    }

    private void initHeaderTips() {
        Drawable drawable = getDrawable(R.drawable.ic_info);
        assert drawable != null;
        int size = ImageUtils.dp2Px(getApplicationContext(), 26);
        drawable.setBounds(0, 0, size, size);
        mHeaderTips.setCompoundDrawables(drawable, null, null, null);
    }

    @Override
    public void onMenuStateChanged(int state) {
        if (state == HomeInstalledHolder.STATE_NONE
                || state == HomeInstalledHolder.STATE_MENU) {
            mHeaderTips.setText("");
            mHeaderTips.setVisibility(View.GONE);
        } else if (state == HomeInstalledHolder.STATE_NORMAL) {
            mHeaderTips.setText(R.string.tips_menu);
            mHeaderTips.setVisibility(View.VISIBLE);
        } else if (state == HomeInstalledHolder.STATE_MOVED) {
            mHeaderTips.setText(R.string.tips_move);
            mHeaderTips.setVisibility(View.VISIBLE);
        }
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
}
