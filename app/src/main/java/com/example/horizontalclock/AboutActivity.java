package com.example.horizontalclock;

import android.support.v7.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.content.pm.PackageManager;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.Context;

public class AboutActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "ClockPrefs";
    private static final String PREF_ABOUT_SHOWN = "aboutShown";
    
    private Button nextButton;
    private TextView titleText;
    private TextView nameTextView;
    private TextView versionTextView;
    
    private GestureDetector gestureDetector;
    private boolean isTVMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 隐藏三大金刚键
        hideSystemUI();
        
        // 检测TV模式
        isTVMode = (getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
                (getSystemService(Context.UI_MODE_SERVICE) != null &&
                 ((UiModeManager) getSystemService(Context.UI_MODE_SERVICE)).getCurrentModeType() == UiModeManager.MODE_NIGHT_YES));
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_about);
        
        initViews();
        setupTVFocusNavigation();
        
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }
            
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // 从右滑前进到位置设置
                if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 100) {
                    goToLocationSetting();
                    return true;
                }
                // 从左滑返回到操作说明
                if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 100) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });
    }
    
    // 隐藏系统UI（三大金刚键）
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
    
    private void initViews() {
        titleText = findViewById(R.id.titleText);
        nextButton = findViewById(R.id.confirmButton);
        nameTextView = findViewById(R.id.nameTextView);
        versionTextView = findViewById(R.id.versionTextView);
        
        // 设置开发者信息和版本号
        nameTextView.setText("小威");
        versionTextView.setText("版本 2.1");
        
        // 修改按钮文本
        nextButton.setText("下一步");
        
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLocationSetting();
            }
        });
    }
    
    private void setupTVFocusNavigation() {
        if (!isTVMode) return;
        
        // 设置可聚焦
        nextButton.setFocusable(true);
        nextButton.setFocusableInTouchMode(true);
        
        // 设置初始焦点
        nextButton.requestFocus();
        
        // 设置焦点变化监听
        nextButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 焦点状态：白色背景，黑色文字
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setColor(Color.WHITE);
                    drawable.setCornerRadius(8);
                    nextButton.setBackground(drawable);
                    nextButton.setTextColor(Color.BLACK);
                } else {
                    // 普通状态：恢复原背景色，白色文字
                    nextButton.setBackground(getResources().getDrawable(R.drawable.spinner_background_white_border));
                    nextButton.setTextColor(Color.WHITE);
                }
            }
        });
    }
    
    private void goToLocationSetting() {
        // 标记关于页面已显示
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_ABOUT_SHOWN, true);
        editor.apply();
        
        // 跳转到位置设置页面
        Intent intent = new Intent(this, LocationSettingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // 结束当前关于页面
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TV端按键处理
        if (isTVMode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    // OK键下一步
                    goToLocationSetting();
                    return true;
                    
                case KeyEvent.KEYCODE_BACK:
                    // 返回键返回到操作说明
                    onBackPressed();
                    return true;
                    
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    // 左键返回
                    onBackPressed();
                    return true;
                    
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    // 右键前进到位置设置
                    goToLocationSetting();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onBackPressed() {
        // 返回时回到操作说明
        super.onBackPressed();
    }
}
