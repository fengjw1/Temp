package com.mp3.launcher4.customs.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.utils.FontUtils;
import com.mp3.launcher4.utils.ImageUtils;


/**
 * @author maogl
 */
public class SearchView extends LinearLayout implements View.OnKeyListener, View.OnClickListener {
    public Button searchView_btn_back;
    /**
     * 键盘布局
     */
    int blockWidth = 34;
    int blockHeight = 34;
    private View rootView;
    private Context mContext;
    private EditText searchView_et;
    private Button searchView_btn_type;
    private ImageButton mIcBtnDel;
    private ImageButton mIcBtnSpace;
    private TextView mSearch;
    private LinearLayout searchView_ll_method;
    private SearchViewListener mViewListener;
    private INPUT_TYPE curInputType = INPUT_TYPE.NUMBER;

    private FontUtils mFontUtils;

    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.rootView = View.inflate(context, R.layout.search_layout, null);
        mFontUtils = FontUtils.getInstance(context.getApplicationContext());
        initView();
        this.addView(rootView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams
                .MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
    }

    public void addListener(SearchViewListener listener) {
        this.mViewListener = listener;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            //back
            if (mViewListener != null) {
                mViewListener.backPress();
            }

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (v.getId() == R.id.searchView_btn_type) {
                if (curInputType == INPUT_TYPE.NUMBER) {
                    curInputType = INPUT_TYPE.DIGIT;
                    searchView_btn_type.setText("ABC");
                } else {
                    curInputType = INPUT_TYPE.NUMBER;
                    searchView_btn_type.setText("123");
                }
                initKeyBoard();
            } else if (v.getId() == R.id.ic_btn_del) {
                String text = searchView_et.getText().toString();
                if (text.length() > 0) {
                    text = text.substring(0, text.length() - 1);
                    searchView_et.setText(text);
                }
            } else if (v.getId() == R.id.searchView_btn_back) {
                if (mViewListener != null) {
                    mViewListener.backPress();
                }
            } else if (v.getId() == R.id.ic_btn_space) {
                searchView_et.setText(searchView_et.getText().toString() + " ");
            }
            return true;
        }
        return false;
    }

    private void initView() {
        searchView_et = (EditText) rootView.findViewById(R.id.searchView_et);
        searchView_btn_back = (Button) rootView.findViewById(R.id.searchView_btn_back);
        searchView_btn_type = (Button) rootView.findViewById(R.id.searchView_btn_type);
        mIcBtnDel = (ImageButton) rootView.findViewById(R.id.ic_btn_del);
        mIcBtnSpace = (ImageButton) rootView.findViewById(R.id.ic_btn_space);
        searchView_ll_method = (LinearLayout) rootView.findViewById(R.id.searchView_ll_method);
        mSearch= rootView.findViewById(R.id.searchView_text);

        mFontUtils.setLightFont(searchView_et);
        mFontUtils.setRegularFont(mSearch);
        mFontUtils.setRegularFont(searchView_btn_back);
        mFontUtils.setRegularFont(searchView_btn_type);



        mIcBtnSpace.setOnKeyListener(this);
        mIcBtnDel.setOnKeyListener(this);
        searchView_et.setOnKeyListener(null);
        searchView_btn_back.setOnKeyListener(this);
        searchView_btn_type.setOnKeyListener(this);
        searchView_btn_back.setOnClickListener(this);
        searchView_btn_type.setOnClickListener(this);
        mIcBtnSpace.setOnClickListener(this);
        mIcBtnDel.setOnClickListener(this);
        initKeyBoard();
    }

    @SuppressLint("SetTextI18n")
    private void initKeyBoard() {
        searchView_ll_method.removeAllViews();
        BlockKeyListener blockKeyListener = new BlockKeyListener();
        BlockKeyClickListener blockKeyClickListener = new BlockKeyClickListener();
        BlockKeyOnFocusListener blockKeyOnFocusListener = new BlockKeyOnFocusListener();
        int minChar = 48;
        int maxChar = 58;

        if (curInputType == INPUT_TYPE.NUMBER) {
            minChar = 65;
            maxChar = 91;
        }
        for (int i = minChar; i < maxChar; i++) {
            Button button = new Button(mContext);
            button.setText(String.valueOf((char) i));
            LayoutParams layoutParams = new LayoutParams(ImageUtils.dp2Px(mContext, blockWidth),
                    ImageUtils.dp2Px(mContext, blockHeight));
            button.setLayoutParams(layoutParams);
            button.setMinWidth(0);
            button.setMinHeight(0);
            button.setGravity(Gravity.CENTER);
            button.setPadding(0, 0, 0, 0);
            button.setTextSize(24);
            button.setElevation(2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                button.setTextColor(mContext.getResources().getColorStateList(R.drawable.search_btn_selector,
                        mContext.getTheme()));
            }else {
                button.setTextColor(mContext.getResources().getColorStateList(R.drawable.search_btn_selector));
            }
            button.setBackgroundResource(R.drawable.selector_search_tab_bg);
            button.setOnKeyListener(blockKeyListener);
            button.setOnClickListener(blockKeyClickListener);
            button.setOnFocusChangeListener(blockKeyOnFocusListener);
            mFontUtils.setLightFont(button);
            searchView_ll_method.addView(button);
        }
    }

    enum INPUT_TYPE {
        /**
         * 数字
         */
        NUMBER,
        /**
         *
         */
        DIGIT
    }

    public interface SearchViewListener {
        /**
         * 监听back事件
         */
        void backPress();
    }

    class BlockKeyListener implements OnKeyListener {

        @SuppressLint("SetTextI18n")
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                searchView_et.setText(searchView_et.getText().toString() + ((Button) v).getText().toString().trim());
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mViewListener != null) {
                    mViewListener.backPress();
                }
                return true;
            }
            return false;
        }
    }

    class BlockKeyClickListener implements OnClickListener {

        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View v) {
            searchView_et.setText(searchView_et.getText().toString() + ((Button) v).getText().toString().trim());
        }
    }

    class BlockKeyOnFocusListener implements  OnFocusChangeListener{

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus){
                mFontUtils.setSemiBoldFont((Button) v);
            }else {
                mFontUtils.setLightFont((Button)v);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.searchView_btn_type) {
            if (curInputType == INPUT_TYPE.NUMBER) {
                curInputType = INPUT_TYPE.DIGIT;
                searchView_btn_type.setText("ABC");
            } else {
                curInputType = INPUT_TYPE.NUMBER;
                searchView_btn_type.setText("123");
            }
            initKeyBoard();
        } else if (v.getId() == R.id.ic_btn_del) {
            String text = searchView_et.getText().toString();
            if (text.length() > 0) {
                text = text.substring(0, text.length() - 1);
                searchView_et.setText(text);
            }
        } else if (v.getId() == R.id.searchView_btn_back) {
            mViewListener.backPress();
        } else if (v.getId() == R.id.ic_btn_space) {
            searchView_et.setText(searchView_et.getText().toString() + " ");
        }
    }
}
