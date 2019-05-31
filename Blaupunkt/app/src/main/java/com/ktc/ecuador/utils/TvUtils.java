package com.ktc.ecuador.utils;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mstar.android.tv.TvChannelManager;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tv.TvIsdbChannelManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;

/**
 * @author longzj
 */
public class TvUtils {

    public static final int STATE_NOT_CONNECTED = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_STARTED_TV = 2;
    public static final int STATE_PAUSED_TV = 3;
    public static final int STATE_STOPPED_TV = 4;
    public static final int STATE_DESTROYED_TV = 5;

    private static final int CHANNEL_MAX = 0xff;
    private static final int CHANNEL_MIN = 0x00;

    private static final int MSG_SOURCE_CHANGE = 0;
    private static final int MSG_FULL_SCREEN = 1;
    private static TvUtils instance;
    private TvCommonManager mTvCommonManager;
    private TvChannelManager mTvChannelManager;
    private int[] mLocation;
    private int[] mSize;
    private volatile int mState;
    private Handler mHandler;
    private SurfaceHolder mSurfaceHolder;
    private boolean isPreparePlaying = false;
    private float mScaleRatio = 1.0f;

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            isPreparePlaying = true;
            try {
                TvManager.getInstance().getPlayerManager().setDisplay(holder);
            } catch (TvCommonException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isPreparePlaying = false;

        }
    };

    /**
     * 构造函数
     *
     * @param context 默认填入ApplicationContext
     */
    private TvUtils(Context context) {
        this(context, null, null);
    }

    /**
     * 构造函数
     *
     * @param context  默认填入ApplicationContext
     * @param location surfaceView的坐标点
     * @param size     surfaceView的尺寸
     */
    private TvUtils(Context context, int[] location, int[] size) {
        this.mLocation = location;
        this.mSize = size;
        mTvCommonManager = TvCommonManager.getInstance();
        mTvChannelManager = TvChannelManager.getInstance();
        initHandlerThread();
        mState = STATE_NOT_CONNECTED;
    }

    /**
     * 初始化一个HandlerThread，用于后台切换信源和缩放电视等操作，防止阻塞主线程
     */
    private void initHandlerThread() {
        final HandlerThread mHandleThread = new HandlerThread("ChangeSource");
        mHandleThread.start();
        mHandler = new Handler(mHandleThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                int source = TvCommonManager.INPUT_SOURCE_STORAGE;
                if (msg.what == MSG_SOURCE_CHANGE) {
                    source = msg.arg1;
                    if (source == TvCommonManager.INPUT_SOURCE_STORAGE) {
                        mTvCommonManager.setInputSource(TvCommonManager.INPUT_SOURCE_STORAGE);
                    } else if (source == TvCommonManager.INPUT_SOURCE_ATV
                            || source == TvCommonManager.INPUT_SOURCE_DTV) {
                        handleSourceChange(source);
                    } else {
                        mTvCommonManager.setInputSource(source);
                    }
                }
                if (source != TvCommonManager.INPUT_SOURCE_STORAGE) {
                    scaleWindow(mScaleRatio);
                } else {
                    ImageUtils.fullScreen();
                }
                return true;
            }
        });
    }

    private void handleSourceChange(int source) {
        int type = source == TvCommonManager.INPUT_SOURCE_ATV
                ? TvIsdbChannelManager.DTV_ANTENNA_TYPE_CABLE
                : TvIsdbChannelManager.DTV_ANTENNA_TYPE_AIR;
        TvIsdbChannelManager.getInstance().setAntennaType(type);
        mTvCommonManager.setInputSource(source);
        if (source == TvCommonManager.INPUT_SOURCE_ATV) {
            int num = mTvChannelManager.getCurrentChannelNumber();
            if (num > CHANNEL_MAX || num < CHANNEL_MIN) {
                num = 0;
            }
            mTvChannelManager.setAtvChannel(num);
        } else {
            mTvChannelManager.playDtvCurrentProgram();
        }
    }

    public void scaleWindow(float ratio) {
        final float minRatio = 1.0f;
        if (ratio < minRatio) {
            ratio = minRatio;
        }
        mScaleRatio = ratio;
        if (!canStart()) {
            return;
        }
        int x = (int) (mLocation[0] - mSize[0] / 2 * (ratio - minRatio));
        int y = (int) (mLocation[1] - mSize[1] / 2 * (ratio - minRatio));
        int width = (int) (mSize[0] * ratio);
        int height = (int) (mSize[1] * ratio);
        ImageUtils.scaleWindow(x, y, width, height, false);
    }

    /**
     * 判断是否能启动小电视
     *
     * @return 坐标，宽高和holder均不为null时返回true
     */
    private boolean canStart() {
        return mLocation != null && mSize != null;
    }

    /**
     * 单实例
     *
     * @param context 填入ApplicationContext
     * @return TVUtils的单实例
     */
    public synchronized static TvUtils getInstance(Context context) {
        if (instance == null) {
            instance = new TvUtils(context);
        }
        return instance;
    }

    public static boolean isSourceDtv(int source) {
        return source == TvCommonManager.INPUT_SOURCE_DTV
                || source == TvCommonManager.INPUT_SOURCE_DTV2;
    }

    /**
     * 得到当前surfaceView的左上角坐标点
     *
     * @return 坐标点信息
     */
    public int[] getLocation() {
        return mLocation;
    }

    /**
     * 设置当前surfaceView的左上角坐标点
     *
     * @param mLocation 设置的坐标点
     */
    private void setLocation(int[] mLocation) {
        this.mLocation = mLocation;

    }

    public int[] getSize() {
        return mSize;
    }

    /**
     * surfaceView的宽高值
     *
     * @param mSize 宽高值
     */
    private void setSize(int[] mSize) {
        if (this.mSize != null) {
            return;
        }
        this.mSize = mSize;
    }

    /**
     * 启动小电视模式
     */
    public void startTv(int inputSource) {
        mHandler.removeMessages(MSG_SOURCE_CHANGE);
        changeSource(inputSource, 0);
        mState = STATE_STARTED_TV;
    }

    /**
     * 切换信源
     *
     * @param source 要切换的信源
     */
    public void changeSource(int source, long delay) {
        Message message = mHandler.obtainMessage(MSG_SOURCE_CHANGE);
        message.what = MSG_SOURCE_CHANGE;
        message.arg1 = source;
        mHandler.removeMessages(MSG_SOURCE_CHANGE);
        mHandler.sendMessageDelayed(message, delay);
    }

    /**
     * 获取当前小电视状态
     * <br>五个值，分别是：<br/>
     * <br><b>STATE_NOT_CONNECTED = </b>{@value STATE_NOT_CONNECTED}<br/>
     * <br><b>STATE_CONNECTED = </b>{@value STATE_CONNECTED}<br/>
     * <br><b>STATE_STARTED_TV = </b>{@value STATE_STARTED_TV}<br/>
     * <br><b>STATE_PAUSED_TV = </b>{@value STATE_PAUSED_TV}<br/>
     * <br><b>STATE_STOPPED_TV = </b>{@value STATE_STOPPED_TV}<br/>
     *
     * @return 当前小电视状态
     */
    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public void bindSurfaceView(SurfaceView surfaceView) {
        Rect rect = new Rect();
        surfaceView.getGlobalVisibleRect(rect);
        int[] location = new int[]{rect.left, rect.top};
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;
        int[] size = new int[]{
                width, height
        };
        setLocation(location);
        setSize(size);
        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(mCallback);
        }
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(mCallback);
    }

    public void destroyTv() {
        pauseTv();
        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(mCallback);
        }
        mLocation = null;
        mSize = null;
        mState = STATE_DESTROYED_TV;
    }

    /**
     * 暂停小电视
     */
    public void pauseTv() {
        changeSource(TvCommonManager.INPUT_SOURCE_STORAGE, 0);
        mState = STATE_PAUSED_TV;
    }

    public void fullScreen(long delayTime) {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(mCallback);
        }
        mHandler.removeMessages(MSG_SOURCE_CHANGE);
        mHandler.removeMessages(MSG_FULL_SCREEN);
        mHandler.sendEmptyMessageDelayed(MSG_FULL_SCREEN, delayTime);
    }
}
