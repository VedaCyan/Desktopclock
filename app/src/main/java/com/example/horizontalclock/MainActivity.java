package com.example.horizontalclock;

import android.support.v7.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.res.TypedArray;
import android.content.res.Resources;
import android.media.AudioManager;
import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.graphics.Typeface;
import android.view.Gravity;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.content.ContextCompat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import android.view.KeyEvent;
import android.app.UiModeManager;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ClockPrefs";
    private static final String PREF_TEXT_COLOR_INDEX = "textColorIndex";
    private static final String PREF_BG_COLOR_INDEX = "bgColorIndex";
    private static final String PREF_STYLE_INDEX = "styleIndex";
    private static final String PREF_LAST_WEATHER_UPDATE = "lastWeatherUpdate";
    private static final String PREF_WEATHER_DATA = "weatherData";
    private static final String PREF_PROVINCE = "province";
    private static final String PREF_CITY = "city";
    private static final String PREF_DISTRICT = "district";
    private static final String PREF_LOCATION_SET = "locationSet";
    private static final String PREF_LAST_WALLPAPER_UPDATE = "lastWallpaperUpdate";
    private static final String PREF_CURRENT_WALLPAPER_URL = "currentWallpaperUrl";
    private static final String PREF_FIRST_RUN = "firstRun";
    private static final String PREF_WALLPAPER_CACHE_INDEX = "wallpaperCacheIndex";
    private static final String PREF_SHOWED_MOBILE_WARNING = "showedMobileWarning";
    private static final String PREF_LAST_WALLPAPER_MINUTE = "lastWallpaperMinute";
    private static final String PREF_NEED_CACHE_WALLPAPER = "needCacheWallpaper";
    private static final String PREF_OPERATION_GUIDE_SHOWN = "operationGuideShown";
    private static final String TAG = "HorizontalClock";

    private TextView timeTextView;
    private TextView dateTextView;
    private TextView lunarTextView;
    private TextView festivalTextView;
    private TextView weatherTextView;
    private ImageView weatherIconImageView;
    private ImageView chargingIndicatorImageView;
    private FrameLayout containerLayout;
    private RelativeLayout clockContainer;
    private LinearLayout weatherContainer;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private PowerManager.WakeLock wakeLock;

    private LinearLayout batteryContainer;
    private FrameLayout batteryBody;
    private View batteryTip;
    private View batteryLevel;
    private TextView batteryPercentTextView;

    // 音乐氛围灯相关
    private View leftAmbientLight;
    private View rightAmbientLight;
    private AudioManager audioManager;
    private boolean isMusicPlaying = false;
    private Handler musicHandler;
    private Runnable musicCheckRunnable;
    private int currentAmbientColorIndex = 0;
    private boolean isLightOn = false;
    private Handler ambientLightHandler;
    private Runnable ambientLightRunnable;

    // 优化后的氛围灯颜色数组 - 去掉太淡的颜色和白色、黑色、灰黑色
    private static final int[] AMBIENT_COLORS = {
        Color.parseColor("#FF00FF"), // 紫色
        Color.parseColor("#00FFFF"), // 青色
        Color.parseColor("#FFFF00"), // 黄色
        Color.parseColor("#FF69B4"), // 粉色
        Color.parseColor("#00FF00"), // 绿色
        Color.parseColor("#FFA500"), // 橙色
        Color.parseColor("#9400D3"), // 深紫色
        Color.parseColor("#FF0000"), // 红色
        Color.parseColor("#FF1493"), // 深粉色
        Color.parseColor("#00CED1"), // 深青色
        Color.parseColor("#FFD700"), // 金色
        Color.parseColor("#FF6347"), // 番茄红
        Color.parseColor("#4169E1"), // 皇家蓝
        Color.parseColor("#32CD32"), // 酸橙绿
        Color.parseColor("#FF4500"), // 橙红色
        Color.parseColor("#DA70D6"), // 兰花紫
        Color.parseColor("#00FA9A"), // 春绿色
        Color.parseColor("#1E90FF"), // 道奇蓝
        Color.parseColor("#CD5C5C"), // 印度红
        Color.parseColor("#4682B4"), // 钢蓝
        Color.parseColor("#DAA520"), // 金麒麟
        Color.parseColor("#FF8C00"), // 深橙色
        Color.parseColor("#8B008B"), // 深洋红
        Color.parseColor("#008080"), // 青色
        Color.parseColor("#FF6B6B"), // 柔和红
        Color.parseColor("#4ECDC4"), // 青绿色
        Color.parseColor("#95E1D3"), // 薄荷绿
        Color.parseColor("#F38181"), // 浅珊瑚红
        Color.parseColor("#AA96DA"), // 淡紫色
        Color.parseColor("#FCBAD3"), // 浅粉红
        Color.parseColor("#FAD02E"), // 金黄色
        Color.parseColor("#F1C40F"), // 芥末黄
        Color.parseColor("#E74C3C"), // 红色
        Color.parseColor("#3498DB"), // 蓝色
        Color.parseColor("#2ECC71"), // 绿色
        Color.parseColor("#F39C12"), // 橙色
        Color.parseColor("#9B59B6"), // 紫色
        Color.parseColor("#1ABC9C"), // 青绿色
        Color.parseColor("#E67E22"), // 胡萝卜色
        Color.parseColor("#95A5A6"), // 灰色
        Color.parseColor("#34495E"), // 深灰色
        Color.parseColor("#16A085"), // 深青绿色
        Color.parseColor("#27AE60"), // 深绿色
        Color.parseColor("#2980B9"), // 深蓝色
        Color.parseColor("#8E44AD"), // 深紫色
        Color.parseColor("#F1C40F"), // 黄色
        Color.parseColor("#D35400"), // 南瓜色
        Color.parseColor("#C0392B"), // 深红色
        Color.parseColor("#7F8C8D") // 灰色
    };

    private int currentTextColorIndex = 0;
    private int currentBgColorIndex = 0;
    private int currentStyleIndex = 0;
    private int[] textColors;
    private int[] backgroundColors;
    private String[] colorNames;
    private String[] backgroundNames;

    private String currentLunarDate = "";
    private String currentDateString = "";
    private String currentFestival = "";
    private Random randomGenerator = new Random();
    private SharedPreferences preferences;

    private int lastMinute = -1;
    private int lastSecond = -1;
    private int lastWallpaperMinute = -1;

    private GestureDetector gestureDetector;
    private boolean isDoubleTap = false;
    private boolean isTVMode = false;
    private View currentFocusedView;
    private long backPressedTime = 0;

    private BatteryReceiver batteryReceiver;
    private int batteryLevelValue = -1;
    private boolean isCharging = false;

    private String currentWeather = "";
    private double currentTemperature = 0.0;
    private int currentWeatherCode = 0;
    private String currentWeatherDescription = "";
    private long lastWeatherUpdateTime = 0;
    private static final long WEATHER_UPDATE_INTERVAL = 10 * 60 * 1000;

    private String currentProvince = "北京";
    private String currentCity = "北京";
    private String currentDistrict = "";
    private String currentLocationName = "北京";

    private static final int WALLPAPER_CACHE_SIZE = 5; // 减少缓存数量以节省内存
    private String currentWallpaperUrl = "";
    private File wallpaperCacheDir;
    private int currentWallpaperIndex = 0;
    private boolean isPreloadingWallpapers = false;

    private Queue<String> wallpaperCacheQueue = new LinkedList<>();

    private ImageView wallpaperImageView;

    private ConnectivityManager connectivityManager;
    private boolean isUsingMobileData = false;
    private boolean showedMobileWarning = false;
    private NetworkReceiver networkReceiver;

    private WallpaperLoader wallpaperLoader;
    private ExecutorService wallpaperExecutor;
    private Bitmap currentWallpaperBitmap = null;
    private Bitmap preloadedWallpaperBitmap = null;
    private String preloadedWallpaperPath = null;

    private boolean hasWallpaperDisplayed = false;
    private int wallpaperRetryCount = 0;
    private static final int MAX_WALLPAPER_RETRY = 2; // 减少重试次数

    private static final int LOCATION_SETTING_REQUEST = 1001;
    private static final int OPERATION_GUIDE_REQUEST = 1002;

    // 添加标志位防止重复打开位置设置
    private boolean isLocationSettingOpen = false;

    // 添加壁纸下载状态跟踪
    private ConcurrentHashMap<String, Boolean> wallpaperDownloadStatus = new ConcurrentHashMap<>();
    private boolean isAppInitializing = false;

    // 呼吸灯连续动画控制
    private boolean isAmbientLightAnimating = false;
    private long lastAmbientLightTime = 0;
    private static final long AMBIENT_LIGHT_MAX_INTERVAL = 500;

    // TV端双击OK键检测
    private long lastOkKeyTime = 0;
    private static final long DOUBLE_TAP_TIMEOUT = 300;

    // 添加完全重启标志
    private boolean needsFullRestart = false;

    // 新增：壁纸加载状态控制
    private boolean isWallpaperLoading = false;

    // 新增：低质量壁纸模式
    private boolean useLowQualityWallpaper = false;
    private static final int LOW_QUALITY_SAMPLE_SIZE = 4; // 降低采样率
    private static final int NORMAL_QUALITY_SAMPLE_SIZE = 2;

    private class BatteryReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

            if (level >= 0 && scale > 0) {
                batteryLevelValue = (int) (level * 100 / (float) scale);
            }

            isCharging =
                    status == BatteryManager.BATTERY_STATUS_CHARGING
                            || status == BatteryManager.BATTERY_STATUS_FULL;

            updateBatteryDisplay();
        }
    }

    private class NetworkReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkNetworkType();
        }
    }

    private class WallpaperLoader {
        private static final int PRELOAD_AHEAD_TIME = 2000;
        private static final int DOWNLOAD_TIMEOUT = 15000; // 15秒超时
        private static final int BUFFER_SIZE = 8192; // 增大缓冲区

        public void preloadNextWallpaper() {
            if (wallpaperCacheQueue.isEmpty() || isPreloadingWallpapers) {
                return;
            }

            isPreloadingWallpapers = true;

            wallpaperExecutor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String nextWallpaperPath = wallpaperCacheQueue.peek();
                                if (nextWallpaperPath != null) {
                                    File wallpaperFile = new File(nextWallpaperPath);
                                    if (wallpaperFile.exists() && isWallpaperDownloadComplete(nextWallpaperPath)) {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.RGB_565; // 使用更省内存的配置
                                        options.inSampleSize = useLowQualityWallpaper ? LOW_QUALITY_SAMPLE_SIZE : NORMAL_QUALITY_SAMPLE_SIZE;
                                        options.inJustDecodeBounds = false;

                                        Bitmap bitmap =
                                                BitmapFactory.decodeFile(
                                                        wallpaperFile.getAbsolutePath(), options);
                                        if (bitmap != null) {
                                            synchronized (MainActivity.this) {
                                                // 释放之前的预加载位图
                                                if (preloadedWallpaperBitmap != null && !preloadedWallpaperBitmap.isRecycled()) {
                                                    preloadedWallpaperBitmap.recycle();
                                                }
                                                preloadedWallpaperBitmap = bitmap;
                                                preloadedWallpaperPath = nextWallpaperPath;
                                            }
                                            Log.d(
                                                    TAG,
                                                    "Wallpaper preloaded successfully: "
                                                            + wallpaperFile.getName());
                                        }
                                    }
                                }
                            } catch (OutOfMemoryError e) {
                                Log.e(TAG, "Out of memory while preloading wallpaper", e);
                                // 内存不足时启用低质量模式
                                useLowQualityWallpaper = true;
                                System.gc();
                            } catch (Exception e) {
                                Log.e(TAG, "Error preloading wallpaper", e);
                            } finally {
                                isPreloadingWallpapers = false;
                            }
                        }
                    });
        }

        public void loadWallpaperImmediate(final String wallpaperPath, final boolean usePreloaded) {
            if (isWallpaperLoading) {
                Log.d(TAG, "Wallpaper is already loading, skipping");
                return;
            }

            isWallpaperLoading = true;

            wallpaperExecutor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 检查壁纸是否完全下载
                                if (!isWallpaperDownloadComplete(wallpaperPath)) {
                                    Log.w(
                                            TAG,
                                            "Wallpaper not fully downloaded, skipping: "
                                                    + wallpaperPath);
                                    isWallpaperLoading = false;
                                    return;
                                }

                                Bitmap bitmapToUse = null;

                                if (usePreloaded
                                        && preloadedWallpaperPath != null
                                        && preloadedWallpaperPath.equals(wallpaperPath)) {
                                    synchronized (MainActivity.this) {
                                        bitmapToUse = preloadedWallpaperBitmap;
                                        preloadedWallpaperBitmap = null;
                                        preloadedWallpaperPath = null;
                                    }
                                    Log.d(TAG, "Using preloaded wallpaper: " + wallpaperPath);
                                }

                                if (bitmapToUse == null) {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inPreferredConfig = Bitmap.Config.RGB_565; // 使用更省内存的配置
                                    options.inSampleSize = useLowQualityWallpaper ? LOW_QUALITY_SAMPLE_SIZE : NORMAL_QUALITY_SAMPLE_SIZE;
                                    options.inJustDecodeBounds = false;

                                    File wallpaperFile = new File(wallpaperPath);
                                    bitmapToUse =
                                            BitmapFactory.decodeFile(
                                                    wallpaperFile.getAbsolutePath(), options);
                                }

                                if (bitmapToUse != null) {
                                    final Bitmap finalBitmap = bitmapToUse;
                                    runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        // 释放当前壁纸资源
                                                        if (currentWallpaperBitmap != null
                                                                && !currentWallpaperBitmap.isRecycled()) {
                                                            currentWallpaperBitmap.recycle();
                                                        }

                                                        // 确保壁纸完全加载后再显示
                                                        wallpaperImageView.setScaleType(
                                                                ImageView.ScaleType.CENTER_CROP);
                                                        wallpaperImageView.setImageBitmap(
                                                                finalBitmap);
                                                        currentWallpaperBitmap = finalBitmap;

                                                        FrameLayout.LayoutParams params =
                                                                (FrameLayout.LayoutParams)
                                                                        wallpaperImageView
                                                                                .getLayoutParams();
                                                        params.width =
                                                                FrameLayout.LayoutParams
                                                                        .MATCH_PARENT;
                                                        params.height =
                                                                FrameLayout.LayoutParams
                                                                        .MATCH_PARENT;
                                                        wallpaperImageView.setLayoutParams(params);

                                                        hasWallpaperDisplayed = true;
                                                        wallpaperRetryCount = 0;

                                                        Log.d(TAG, "Wallpaper set successfully");
                                                    } catch (Exception e) {
                                                        Log.e(
                                                                TAG,
                                                                "Error setting wallpaper on UI thread",
                                                                e);
                                                    } finally {
                                                        isWallpaperLoading = false;
                                                    }
                                                }
                                            });

                                    wallpaperCacheQueue.poll();
                                } else {
                                    handleWallpaperLoadFailure(wallpaperPath);
                                }
                            } catch (OutOfMemoryError e) {
                                Log.e(TAG, "Out of memory while loading wallpaper", e);
                                useLowQualityWallpaper = true;
                                System.gc();
                                handleWallpaperLoadFailure(wallpaperPath);
                            } catch (Exception e) {
                                Log.e(TAG, "Error loading wallpaper immediately", e);
                                handleWallpaperLoadFailure(wallpaperPath);
                            }
                        }
                    });
        }

        private void handleWallpaperLoadFailure(String wallpaperPath) {
            isWallpaperLoading = false;
            wallpaperRetryCount++;
            Log.w(TAG, "Wallpaper load failure, retry count: " + wallpaperRetryCount);

            if (wallpaperRetryCount <= MAX_WALLPAPER_RETRY) {
                wallpaperCacheQueue.poll();

                if (!wallpaperCacheQueue.isEmpty()) {
                    String nextWallpaperPath = wallpaperCacheQueue.peek();
                    Log.d(TAG, "Retrying with next wallpaper: " + nextWallpaperPath);
                    loadWallpaperImmediate(nextWallpaperPath, false);
                } else {
                    Log.d(TAG, "Wallpaper cache empty, triggering reload");
                    rebuildCacheQueue();
                    if (!wallpaperCacheQueue.isEmpty()) {
                        loadWallpaperImmediate(wallpaperCacheQueue.peek(), false);
                    } else {
                        preloadWallpaperCacheWithFallback();
                    }
                }
            } else {
                wallpaperRetryCount = 0;
                Log.e(TAG, "Max wallpaper retry count reached");
                // 显示默认背景
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wallpaperImageView.setImageResource(android.R.color.black);
                    }
                });
            }
        }

        /**
         * 优化下载方法，增加超时控制和进度跟踪
         */
        public void downloadWallpaper(final String imageUrl, final File wallpaperFile, final OnDownloadListener listener) {
            wallpaperExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    FileOutputStream outputStream = null;
                    InputStream inputStream = null;
                    
                    try {
                        markWallpaperDownloading(wallpaperFile.getAbsolutePath());

                        URL url = new URL(imageUrl);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty(
                                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                        connection.setConnectTimeout(DOWNLOAD_TIMEOUT);
                        connection.setReadTimeout(DOWNLOAD_TIMEOUT);
                        connection.setInstanceFollowRedirects(true);

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            inputStream = connection.getInputStream();
                            outputStream = new FileOutputStream(wallpaperFile);

                            byte[] buffer = new byte[BUFFER_SIZE];
                            int bytesRead;
                            long totalBytesRead = 0;
                            
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;
                                
                                // 定期检查是否应该取消下载
                                if (Thread.currentThread().isInterrupted()) {
                                    Log.d(TAG, "Wallpaper download cancelled");
                                    break;
                                }
                            }

                            outputStream.close();
                            inputStream.close();

                            // 标记下载完成
                            markWallpaperDownloadCompleted(wallpaperFile.getAbsolutePath());

                            if (listener != null) {
                                listener.onDownloadSuccess(wallpaperFile.getAbsolutePath());
                            }

                            Log.d(TAG, "Wallpaper downloaded successfully: " + wallpaperFile.getName());

                        } else {
                            Log.e(TAG, "Failed to download wallpaper, response code: " + responseCode);
                            if (wallpaperFile.exists()) {
                                wallpaperFile.delete();
                            }
                            if (listener != null) {
                                listener.onDownloadFailure("HTTP " + responseCode);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error downloading wallpaper", e);
                        if (wallpaperFile.exists()) {
                            wallpaperFile.delete();
                        }
                        if (listener != null) {
                            listener.onDownloadFailure(e.getMessage());
                        }
                    } finally {
                        if (outputStream != null) {
                            try { outputStream.close(); } catch (Exception e) { }
                        }
                        if (inputStream != null) {
                            try { inputStream.close(); } catch (Exception e) { }
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            });
        }
    }

    interface OnDownloadListener {
        void onDownloadSuccess(String filePath);
        void onDownloadFailure(String error);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 标记应用正在初始化
        isAppInitializing = true;

        try {
            hideNavigationBar();

            setContentView(R.layout.activity_main);

            // 初始化线程池，限制并发数
            wallpaperExecutor = Executors.newFixedThreadPool(1); // 改为单线程，避免并发下载
            wallpaperLoader = new WallpaperLoader();

            preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            // 修复后的 TV 模式检测 - 兼容 Android 4.1+
            UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
            isTVMode = (getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
                    (uiModeManager != null && uiModeManager.getCurrentModeType() == UiModeManager.MODE_NIGHT_YES));

            // 检查是否需要显示操作说明
            boolean operationGuideShown = preferences.getBoolean(PREF_OPERATION_GUIDE_SHOWN, false);
            if (!operationGuideShown) {
                showOperationGuide();
                return; // 显示操作说明，不继续执行
            }

            // 检查位置设置，如果未设置则跳转到位置设置
            boolean locationSet = preferences.getBoolean(PREF_LOCATION_SET, false);
            if (!locationSet) {
                showLocationSetting();
                return; // 等待位置设置完成后再继续
            }

            connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            networkReceiver = new NetworkReceiver();
            IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkReceiver, networkFilter);

            checkNetworkType();

            wallpaperCacheDir = new File(getCacheDir(), "wallpapers");
            if (!wallpaperCacheDir.exists()) {
                wallpaperCacheDir.mkdirs();
            }

            currentWallpaperIndex = preferences.getInt(PREF_WALLPAPER_CACHE_INDEX, 0);

            lastWallpaperMinute = preferences.getInt(PREF_LAST_WALLPAPER_MINUTE, -1);

            timeTextView = findViewById(R.id.timeTextView);
            dateTextView = findViewById(R.id.dateTextView);
            lunarTextView = findViewById(R.id.lunarTextView);
            festivalTextView = findViewById(R.id.festivalTextView);
            weatherTextView = findViewById(R.id.weatherTextView);
            weatherIconImageView = findViewById(R.id.weatherIconImageView);
            containerLayout = findViewById(R.id.containerLayout);
            clockContainer = findViewById(R.id.clockContainer);
            weatherContainer = findViewById(R.id.weatherContainer);

            wallpaperImageView = findViewById(R.id.wallpaperImageView);

            batteryContainer = findViewById(R.id.batteryContainer);

            // TV模式下不创建电池图标
            if (!isTVMode) {
                createBatteryIcon();
            }

            // 初始化音乐氛围灯
            initAmbientLights();

            batteryReceiver = new BatteryReceiver();

            loadLocationData();

            // 设置TV模式焦点
            setupFocusForTV();

            // 修改手势检测器
            gestureDetector =
                    new GestureDetector(
                            this,
                            new GestureDetector.SimpleOnGestureListener() {
                                @Override
                                public boolean onSingleTapConfirmed(MotionEvent e) {
                                    if (!isDoubleTap) {
                                        if (isClickOnClock(e)) {
                                            changeTextColors(); // 点击时钟更换颜色
                                        } else {
                                            // 移除点击限制，直接更换壁纸
                                            forceUpdateWallpaperImmediate(); // 点击壁纸更换壁纸
                                        }
                                    }
                                    return true;
                                }

                                @Override
                                public boolean onDoubleTap(MotionEvent e) {
                                    isDoubleTap = true;
                                    if (isClickOnClock(e)) {
                                        changeStyle(); // 双击时钟更换字体
                                    }

                                    handler.postDelayed(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    isDoubleTap = false;
                                                }
                                            },
                                            500);
                                    return true;
                                }

                                @Override
                                public boolean onFling(
                                        MotionEvent e1,
                                        MotionEvent e2,
                                        float velocityX,
                                        float velocityY) {
                                    // 从左滑重新设置定位
                                    if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 100) {
                                        showLocationSetting();
                                        return true;
                                    }
                                    return false;
                                }
                            });

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (powerManager != null) {
                wakeLock =
                        powerManager.newWakeLock(
                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "HorizontalClock:ScreenLock");
            }

            // 修复：使用 length() 替代 getLength() 确保 Android 4.1 兼容性
            TypedArray textColorsTa = getResources().obtainTypedArray(R.array.text_colors);
            int textColorsLength = textColorsTa.length();
            textColors = new int[textColorsLength];
            for (int i = 0; i < textColorsLength; i++) {
                textColors[i] = textColorsTa.getColor(i, 0);
            }
            textColorsTa.recycle();

            // 修复：使用 length() 替代 getLength() 确保 Android 4.1 兼容性
            TypedArray bgColorsTa = getResources().obtainTypedArray(R.array.background_colors);
            int bgColorsLength = bgColorsTa.length();
            backgroundColors = new int[bgColorsLength];
            for (int i = 0; i < bgColorsLength; i++) {
                backgroundColors[i] = bgColorsTa.getColor(i, 0);
            }
            bgColorsTa.recycle();

            colorNames = getResources().getStringArray(R.array.color_names);
            backgroundNames = getResources().getStringArray(R.array.background_names);

            loadColorPreferences();

            showedMobileWarning = preferences.getBoolean(PREF_SHOWED_MOBILE_WARNING, false);

            applyStyle();

            containerLayout.setOnTouchListener(
                    new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (isTVMode) return false;
                            gestureDetector.onTouchEvent(event);

                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                v.performClick();
                            }
                            return true;
                        }
                    });

            handler = new Handler();
            updateTimeRunnable =
                    new Runnable() {
                        @Override
                        public void run() {
                            updateTimeAndDate();
                            handler.postDelayed(this, 1000);
                        }
                    };

            updateTimeAndDate();

            checkAndUpdateWeather();

            // 检查是否需要缓存壁纸
            boolean needCacheWallpaper = preferences.getBoolean(PREF_NEED_CACHE_WALLPAPER, false);
            if (needCacheWallpaper) {
                preloadWallpaperCacheWithFallback();
                // 重置标志
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREF_NEED_CACHE_WALLPAPER, false);
                editor.apply();
            } else {
                // 延迟初始化壁纸，避免启动时卡顿
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        preloadWallpaperCacheWithFallback();
                    }
                }, 2000);
            }

            handler.post(updateTimeRunnable);

            clockContainer.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            centerClock();
                        }
                    },
                    100);

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        } finally {
            // 初始化完成
            isAppInitializing = false;
        }
    }

    /** 设置TV模式焦点 */
    private void setupFocusForTV() {
        if (!isTVMode) return;

        containerLayout.setFocusable(true);
        containerLayout.setFocusableInTouchMode(true);
        clockContainer.setFocusable(true);
        clockContainer.setFocusableInTouchMode(true);

        // 设置初始焦点
        clockContainer.requestFocus();

        // 设置焦点变化监听
        clockContainer.setOnFocusChangeListener(
                (v, hasFocus) -> {
                    if (hasFocus) {
                        currentFocusedView = v;
                        // 添加焦点指示器
                        clockContainer.setBackgroundColor(Color.parseColor("#80FFFFFF"));
                    } else {
                        clockContainer.setBackgroundColor(Color.TRANSPARENT);
                    }
                });

        containerLayout.setOnFocusChangeListener(
                (v, hasFocus) -> {
                    if (hasFocus) {
                        currentFocusedView = v;
                        // 添加焦点指示器
                        containerLayout.setBackgroundColor(Color.parseColor("#80FFFFFF"));
                    } else {
                        containerLayout.setBackgroundColor(Color.TRANSPARENT);
                    }
                });
    }

    /** 检测点击是否在时钟区域 */
    private boolean isClickOnClock(MotionEvent e) {
        int[] location = new int[2];
        clockContainer.getLocationOnScreen(location);
        return e.getRawX() >= location[0]
                && e.getRawX() <= location[0] + clockContainer.getWidth()
                && e.getRawY() >= location[1]
                && e.getRawY() <= location[1] + clockContainer.getHeight();
    }

    /** TV端按键处理 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isTVMode) return super.onKeyDown(keyCode, event);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastOkKeyTime < DOUBLE_TAP_TIMEOUT) {
                    // 双击OK键重新设置定位
                    showLocationSetting();
                    lastOkKeyTime = 0;
                } else {
                    // 单击OK键
                    if (currentFocusedView == clockContainer) {
                        changeTextColors(); // 焦点在时钟上更换颜色
                    } else {
                        // 移除点击限制
                        forceUpdateWallpaperImmediate(); // 焦点在背景上更换壁纸
                    }
                    lastOkKeyTime = currentTime;
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                changeTextColors(); // 上下键更换颜色
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                changeStyle(); // 左右键更换字体
                return true;

            case KeyEvent.KEYCODE_BACK:
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    // 双击返回退出
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        finish();
                        return true;
                    } else {
                        backPressedTime = System.currentTimeMillis();
                        Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** 初始化音乐氛围灯 */
    private void initAmbientLights() {
        try {
            // 创建左侧氛围灯
            leftAmbientLight = new View(this);
            FrameLayout.LayoutParams leftParams =
                    new FrameLayout.LayoutParams(
                            dpToPx(100), FrameLayout.LayoutParams.MATCH_PARENT);
            leftParams.gravity = Gravity.LEFT;
            leftAmbientLight.setLayoutParams(leftParams);
            containerLayout.addView(leftAmbientLight);

            // 创建右侧氛围灯
            rightAmbientLight = new View(this);
            FrameLayout.LayoutParams rightParams =
                    new FrameLayout.LayoutParams(
                            dpToPx(100), FrameLayout.LayoutParams.MATCH_PARENT);
            rightParams.gravity = Gravity.RIGHT;
            rightAmbientLight.setLayoutParams(rightParams);
            containerLayout.addView(rightAmbientLight);

            // 初始状态为隐藏
            leftAmbientLight.setVisibility(View.GONE);
            rightAmbientLight.setVisibility(View.GONE);

            // 初始化音频管理器
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            // 初始化音乐检测
            musicHandler = new Handler();
            musicCheckRunnable =
                    new Runnable() {
                        @Override
                        public void run() {
                            checkMusicStatus();
                            musicHandler.postDelayed(this, 100); // 每100ms检测一次
                        }
                    };

            // 初始化氛围灯动画处理器
            ambientLightHandler = new Handler();

            // 开始音乐检测
            startMusicDetection();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing ambient lights", e);
        }
    }

    /** 开始音乐检测 */
    private void startMusicDetection() {
        try {
            if (musicHandler != null && musicCheckRunnable != null) {
                musicHandler.post(musicCheckRunnable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting music detection", e);
        }
    }

    /** 停止音乐检测 */
    private void stopMusicDetection() {
        try {
            if (musicHandler != null && musicCheckRunnable != null) {
                musicHandler.removeCallbacks(musicCheckRunnable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping music detection", e);
        }
    }

    /** 检查音乐播放状态 */
    private void checkMusicStatus() {
        try {
            boolean wasMusicPlaying = isMusicPlaying;
            isMusicPlaying = audioManager.isMusicActive();

            if (isMusicPlaying && !wasMusicPlaying) {
                // 音乐开始播放
                showAmbientLights();
                startAmbientLightAnimation();
            } else if (!isMusicPlaying && wasMusicPlaying) {
                // 音乐停止播放
                hideAmbientLights();
                stopAmbientLightAnimation();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking music status", e);
        }
    }

    /** 显示氛围灯 */
    private void showAmbientLights() {
        try {
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            leftAmbientLight.setVisibility(View.VISIBLE);
                            rightAmbientLight.setVisibility(View.VISIBLE);

                            // 设置初始颜色
                            updateAmbientLightColor();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error showing ambient lights", e);
        }
    }

    /** 隐藏氛围灯 */
    private void hideAmbientLights() {
        try {
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            leftAmbientLight.setVisibility(View.GONE);
                            rightAmbientLight.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error hiding ambient lights", e);
        }
    }

    /** 开始氛围灯动画 - 改进版本：连续呼吸效果，解决间歇问题 */
    private void startAmbientLightAnimation() {
        try {
            if (ambientLightHandler != null && !isAmbientLightAnimating) {
                isAmbientLightAnimating = true;
                lastAmbientLightTime = System.currentTimeMillis();

                ambientLightRunnable =
                        new Runnable() {
                            @Override
                            public void run() {
                                if (isMusicPlaying) {
                                    // 检查动画间隔，确保连续性
                                    long currentTime = System.currentTimeMillis();
                                    long interval = currentTime - lastAmbientLightTime;

                                    if (interval > AMBIENT_LIGHT_MAX_INTERVAL) {
                                        Log.w(
                                                TAG,
                                                "Ambient light animation interval too long: "
                                                        + interval
                                                        + "ms, restarting");
                                        // 立即执行下一个动画
                                        performBreathingAnimation();
                                    }

                                    // 执行呼吸动画
                                    performBreathingAnimation();

                                    // 更换颜色
                                    currentAmbientColorIndex =
                                            (currentAmbientColorIndex + 1) % AMBIENT_COLORS.length;

                                    // 更新时间戳
                                    lastAmbientLightTime = System.currentTimeMillis();

                                    // 随机持续时间：800ms到3000ms，确保舒适的眼部体验
                                    int duration = randomGenerator.nextInt(2200) + 800;

                                    // 安排下一次动画
                                    ambientLightHandler.postDelayed(this, duration);
                                } else {
                                    isAmbientLightAnimating = false;
                                }
                            }
                        };

                // 开始动画
                ambientLightHandler.post(ambientLightRunnable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting ambient light animation", e);
        }
    }

    /** 执行呼吸动画 - 舒适的闪烁效果 */
    private void performBreathingAnimation() {
        try {
            final int color = AMBIENT_COLORS[currentAmbientColorIndex];

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // 设置左侧氛围灯颜色（从左到右渐变）
                            GradientDrawable leftGradient =
                                    new GradientDrawable(
                                            GradientDrawable.Orientation.LEFT_RIGHT,
                                            new int[] {color, Color.TRANSPARENT});
                            leftGradient.setCornerRadius(0);
                            leftAmbientLight.setBackground(leftGradient);

                            // 设置右侧氛围灯颜色（从右到左渐变）
                            GradientDrawable rightGradient =
                                    new GradientDrawable(
                                            GradientDrawable.Orientation.RIGHT_LEFT,
                                            new int[] {color, Color.TRANSPARENT});
                            rightGradient.setCornerRadius(0);
                            rightAmbientLight.setBackground(rightGradient);

                            // 创建呼吸动画序列
                            createBreathingSequence();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error performing breathing animation", e);
        }
    }

    /** 创建呼吸动画序列 - 舒适的闪烁效果 */
    private void createBreathingSequence() {
        try {
            // 随机选择呼吸速度类型
            int speedType = randomGenerator.nextInt(3); // 0=慢, 1=中, 2=快

            int fadeInDuration, holdDuration, fadeOutDuration;

            switch (speedType) {
                case 0: // 慢速呼吸
                    fadeInDuration = 800;
                    holdDuration = 600;
                    fadeOutDuration = 800;
                    break;
                case 1: // 中速呼吸
                    fadeInDuration = 400;
                    holdDuration = 300;
                    fadeOutDuration = 400;
                    break;
                case 2: // 快速呼吸
                default:
                    fadeInDuration = 200;
                    holdDuration = 150;
                    fadeOutDuration = 200;
                    break;
            }

            // 设置初始透明度为0
            leftAmbientLight.setAlpha(0f);
            rightAmbientLight.setAlpha(0f);

            // 淡入阶段
            leftAmbientLight
                    .animate()
                    .alpha(1.0f)
                    .setDuration(fadeInDuration)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            rightAmbientLight
                    .animate()
                    .alpha(1.0f)
                    .setDuration(fadeInDuration)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            // 保持阶段
            ambientLightHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            // 淡出阶段
                            leftAmbientLight
                                    .animate()
                                    .alpha(0f)
                                    .setDuration(fadeOutDuration)
                                    .setInterpolator(
                                            new android.view.animation.AccelerateInterpolator())
                                    .start();

                            rightAmbientLight
                                    .animate()
                                    .alpha(0f)
                                    .setDuration(fadeOutDuration)
                                    .setInterpolator(
                                            new android.view.animation.AccelerateInterpolator())
                                    .start();
                        }
                    },
                    fadeInDuration + holdDuration);

        } catch (Exception e) {
            Log.e(TAG, "Error creating breathing sequence", e);
        }
    }

    /** 停止氛围灯动画 */
    private void stopAmbientLightAnimation() {
        try {
            isAmbientLightAnimating = false;
            if (ambientLightHandler != null && ambientLightRunnable != null) {
                ambientLightHandler.removeCallbacks(ambientLightRunnable);
                ambientLightRunnable = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping ambient light animation", e);
        }
    }

    /** 更新氛围灯颜色 */
    private void updateAmbientLightColor() {
        try {
            final int color = AMBIENT_COLORS[currentAmbientColorIndex];

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            // 设置左侧氛围灯颜色（从左到右渐变）
                            GradientDrawable leftGradient =
                                    new GradientDrawable(
                                            GradientDrawable.Orientation.LEFT_RIGHT,
                                            new int[] {color, Color.TRANSPARENT});
                            leftGradient.setCornerRadius(0);
                            leftAmbientLight.setBackground(leftGradient);

                            // 设置右侧氛围灯颜色（从右到左渐变）
                            GradientDrawable rightGradient =
                                    new GradientDrawable(
                                            GradientDrawable.Orientation.RIGHT_LEFT,
                                            new int[] {color, Color.TRANSPARENT});
                            rightGradient.setCornerRadius(0);
                            rightAmbientLight.setBackground(rightGradient);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error updating ambient light color", e);
        }
    }

    private void showOperationGuide() {
        try {
            Intent intent = new Intent(this, OperationGuideActivity.class);
            startActivityForResult(intent, OPERATION_GUIDE_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Error showing operation guide", e);
        }
    }

    private void showLocationSetting() {
        try {
            // 防止重复打开
            if (isLocationSettingOpen) {
                return;
            }
            isLocationSettingOpen = true;

            // 完全清理当前界面资源
            cleanupResources();

            // 标记需要完全重启
            needsFullRestart = true;

            Intent intent = new Intent(this, LocationSettingActivity.class);
            startActivityForResult(intent, LOCATION_SETTING_REQUEST);
            
            // 立即结束当前Activity，确保不在后台运行
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error showing location setting", e);
            isLocationSettingOpen = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == OPERATION_GUIDE_REQUEST) {
            // 操作说明已显示，继续检查位置设置
            boolean locationSet = preferences.getBoolean(PREF_LOCATION_SET, false);
            if (!locationSet) {
                showLocationSetting();
            } else {
                // 位置已设置，重新初始化主界面
                recreate();
            }
        } else if (requestCode == LOCATION_SETTING_REQUEST) {
            // 重置标志位
            isLocationSettingOpen = false;

            if (resultCode == RESULT_OK) {
                // 位置设置成功，需要完全重启
                needsFullRestart = true;
                
                // 完全清理资源
                cleanupResources();
                
                // 重新启动主界面
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                
                // 结束当前Activity
                finish();
            } else {
                // 位置设置取消，如果位置未设置则继续显示位置设置
                boolean locationSet = preferences.getBoolean(PREF_LOCATION_SET, false);
                if (!locationSet) {
                    showLocationSetting();
                } else {
                    // 位置已设置，重新初始化主界面
                    recreate();
                }
            }
        }
    }

    /** 完全清理资源 */
    private void cleanupResources() {
        try {
            // 停止所有Handler
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            
            if (musicHandler != null) {
                musicHandler.removeCallbacksAndMessages(null);
            }
            
            if (ambientLightHandler != null) {
                ambientLightHandler.removeCallbacksAndMessages(null);
            }
            
            // 停止音乐检测
            stopMusicDetection();
            
            // 停止氛围灯动画
            stopAmbientLightAnimation();
            
            // 注销所有接收器
            if (batteryReceiver != null) {
                try {
                    unregisterReceiver(batteryReceiver);
                } catch (Exception e) {
                    Log.e(TAG, "Error unregistering battery receiver", e);
                }
            }
            
            if (networkReceiver != null) {
                try {
                    unregisterReceiver(networkReceiver);
                } catch (Exception e) {
                    Log.e(TAG, "Error unregistering network receiver", e);
                }
            }
            
            // 释放WakeLock
            if (wakeLock != null && wakeLock.isHeld()) {
                try {
                    wakeLock.release();
                } catch (Exception e) {
                    Log.e(TAG, "Error releasing wake lock", e);
                }
            }
            
            // 释放壁纸资源
            if (currentWallpaperBitmap != null && !currentWallpaperBitmap.isRecycled()) {
                try {
                    currentWallpaperBitmap.recycle();
                } catch (Exception e) {
                    Log.e(TAG, "Error recycling current wallpaper bitmap", e);
                }
                currentWallpaperBitmap = null;
            }
            
            if (preloadedWallpaperBitmap != null && !preloadedWallpaperBitmap.isRecycled()) {
                try {
                    preloadedWallpaperBitmap.recycle();
                } catch (Exception e) {
                    Log.e(TAG, "Error recycling preloaded wallpaper bitmap", e);
                }
                preloadedWallpaperBitmap = null;
            }
            
            // 关闭线程池
            if (wallpaperExecutor != null && !wallpaperExecutor.isShutdown()) {
                wallpaperExecutor.shutdownNow();
            }
            
            // 清理下载状态
            wallpaperDownloadStatus.clear();
            
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up resources", e);
        }
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);

        getWindow()
                .setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }

    private void checkNetworkType() {
        try {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            boolean wasUsingMobileData = isUsingMobileData;

            if (activeNetwork != null) {
                isUsingMobileData = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

                if (!wasUsingMobileData && isUsingMobileData && !showedMobileWarning) {
                    showMobileDataWarning();
                    showedMobileWarning = true;

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(PREF_SHOWED_MOBILE_WARNING, true);
                    editor.apply();
                }
            } else {
                isUsingMobileData = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network type", e);
            isUsingMobileData = false;
        }
    }

    private void showMobileDataWarning() {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(
                                            MainActivity.this,
                                            "检测到您正在使用移动数据，壁纸加载可能会消耗较多流量。",
                                            Toast.LENGTH_LONG)
                                    .show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error showing mobile warning", e);
                        }
                    }
                });
    }

    private void preloadWallpaperCacheWithFallback() {
        if (isPreloadingWallpapers) {
            return;
        }

        isPreloadingWallpapers = true;

        new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    File[] cachedWallpapers = getCachedWallpapers();
                                    if (cachedWallpapers.length > 0) {
                                        rebuildCacheQueue();
                                        useCachedWallpaper();
                                    } else {
                                        Log.d(
                                                TAG,
                                                "No cached wallpapers found, downloading first wallpaper immediately");
                                        downloadAndDisplayFirstWallpaper();
                                    }

                                    int wallpapersToLoad =
                                            WALLPAPER_CACHE_SIZE - cachedWallpapers.length;
                                    for (int i = 0; i < wallpapersToLoad; i++) {
                                        preloadSingleWallpaper();
                                    }

                                    isPreloadingWallpapers = false;

                                } catch (Exception e) {
                                    Log.e(TAG, "Error preloading wallpapers", e);
                                    isPreloadingWallpapers = false;
                                }
                            }
                        })
                .start();
    }

    private void downloadAndDisplayFirstWallpaper() {
        if (isUsingMobileData && !showedMobileWarning) {
            Log.d(TAG, "Mobile data warning not confirmed, skipping wallpaper download");
            return;
        }

        try {
            String imageUrl = "https://tu.ltyuanfang.cn/api/fengjing.php";
            String fileName = "wallpaper_first_" + System.currentTimeMillis() + ".jpg";
            File wallpaperFile = new File(wallpaperCacheDir, fileName);

            wallpaperLoader.downloadWallpaper(imageUrl, wallpaperFile, new OnDownloadListener() {
                @Override
                public void onDownloadSuccess(String filePath) {
                    wallpaperCacheQueue.offer(filePath);
                    useCachedWallpaper();
                }

                @Override
                public void onDownloadFailure(String error) {
                    Log.e(TAG, "Failed to download first wallpaper: " + error);
                    // 延迟重试
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (!hasWallpaperDisplayed) {
                                        downloadAndDisplayFirstWallpaper();
                                    }
                                }
                            },
                            10000); // 10秒后重试
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error downloading first wallpaper", e);

            handler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!hasWallpaperDisplayed) {
                                downloadAndDisplayFirstWallpaper();
                            }
                        }
                    },
                    10000);
        }
    }

    private void preloadWallpaperCache() {
        if (isPreloadingWallpapers) {
            return;
        }

        isPreloadingWallpapers = true;

        new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    File[] cachedWallpapers = getCachedWallpapers();
                                    if (cachedWallpapers.length > 0) {
                                        rebuildCacheQueue();
                                        useCachedWallpaper();
                                    }

                                    int wallpapersToLoad =
                                            WALLPAPER_CACHE_SIZE - cachedWallpapers.length;
                                    for (int i = 0; i < wallpapersToLoad; i++) {
                                        preloadSingleWallpaper();
                                    }

                                    isPreloadingWallpapers = false;

                                } catch (Exception e) {
                                    Log.e(TAG, "Error preloading wallpapers", e);
                                    isPreloadingWallpapers = false;
                                }
                            }
                        })
                .start();
    }

    private void preloadSingleWallpaper() {
        if (isUsingMobileData && !showedMobileWarning) {
            return;
        }

        try {
            String imageUrl = "https://tu.ltyuanfang.cn/api/fengjing.php";
            String fileName =
                    "wallpaper_cache_"
                            + System.currentTimeMillis()
                            + "_"
                            + randomGenerator.nextInt(1000)
                            + ".jpg";
            File wallpaperFile = new File(wallpaperCacheDir, fileName);

            wallpaperLoader.downloadWallpaper(imageUrl, wallpaperFile, new OnDownloadListener() {
                @Override
                public void onDownloadSuccess(String filePath) {
                    wallpaperCacheQueue.offer(filePath);

                    if (!hasWallpaperDisplayed) {
                        Log.d(
                                TAG,
                                "First wallpaper cached, displaying immediately: "
                                        + wallpaperFile.getName());
                        useCachedWallpaper();
                    }

                    cleanupExcessCache();
                    Log.d(TAG, "Successfully preloaded wallpaper: " + wallpaperFile.getName());
                }

                @Override
                public void onDownloadFailure(String error) {
                    Log.e(TAG, "Failed to preload wallpaper: " + error);
                    if (wallpaperFile.exists()) {
                        wallpaperFile.delete();
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preloading single wallpaper", e);
        }
    }

    private void cleanupExcessCache() {
        try {
            File[] files = wallpaperCacheDir.listFiles();
            if (files != null && files.length > WALLPAPER_CACHE_SIZE) {
                Arrays.sort(
                        files,
                        new Comparator<File>() {
                            @Override
                            public int compare(File f1, File f2) {
                                return Long.compare(f1.lastModified(), f2.lastModified());
                            }
                        });

                int filesToDelete = files.length - WALLPAPER_CACHE_SIZE;
                for (int i = 0; i < filesToDelete; i++) {
                    boolean deleted = files[i].delete();
                    Log.d(
                            TAG,
                            "Deleted old wallpaper cache: "
                                    + files[i].getName()
                                    + ", success: "
                                    + deleted);
                }

                rebuildCacheQueue();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning excess cache", e);
        }
    }

    private void rebuildCacheQueue() {
        try {
            wallpaperCacheQueue.clear();
            File[] files = getCachedWallpapers();
            for (File file : files) {
                // 只添加完全下载的壁纸到队列
                if (isWallpaperDownloadComplete(file.getAbsolutePath())) {
                    wallpaperCacheQueue.offer(file.getAbsolutePath());
                }
            }
            Log.d(TAG, "Rebuilt cache queue, size: " + wallpaperCacheQueue.size());
        } catch (Exception e) {
            Log.e(TAG, "Error rebuilding cache queue", e);
        }
    }

    private File[] getCachedWallpapers() {
        try {
            File[] files = wallpaperCacheDir.listFiles();
            if (files != null) {
                Arrays.sort(
                        files,
                        new Comparator<File>() {
                            @Override
                            public int compare(File f1, File f2) {
                                return Long.compare(f2.lastModified(), f1.lastModified());
                            }
                        });

                int count = Math.min(files.length, WALLPAPER_CACHE_SIZE);
                File[] result = new File[count];
                System.arraycopy(files, 0, result, 0, count);

                Log.d(TAG, "Found " + result.length + " cached wallpapers");
                return result;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting cached wallpapers", e);
        }
        return new File[0];
    }

    private void useCachedWallpaper() {
        if (wallpaperCacheQueue.isEmpty()) {
            Log.d(TAG, "Wallpaper cache is empty, trying to rebuild");
            rebuildCacheQueue();

            if (wallpaperCacheQueue.isEmpty()) {
                Log.d(TAG, "Wallpaper cache still empty after rebuild, preloading new wallpaper");
                if (!isUsingMobileData || showedMobileWarning) {
                    new Thread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            preloadSingleWallpaper();
                                        }
                                    })
                            .start();
                }
                return;
            }
        }

        String wallpaperPath = wallpaperCacheQueue.peek();
        if (wallpaperPath != null) {
            File wallpaperFile = new File(wallpaperPath);
            if (wallpaperFile.exists()) {
                Log.d(TAG, "Setting wallpaper from file: " + wallpaperFile.getName());

                wallpaperLoader.loadWallpaperImmediate(wallpaperPath, true);

                Log.d(
                        TAG,
                        "Wallpaper update initiated, cache size: "
                                + (wallpaperCacheQueue.size() - 1));

                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                wallpaperLoader.preloadNextWallpaper();
                            }
                        },
                        2000);

                if (wallpaperCacheQueue.size() < WALLPAPER_CACHE_SIZE - 1
                        && (!isUsingMobileData || showedMobileWarning)) {
                    new Thread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            preloadSingleWallpaper();
                                        }
                                    })
                            .start();
                }
            } else {
                Log.e(TAG, "Wallpaper file does not exist: " + wallpaperPath);
                wallpaperCacheQueue.poll();
                useCachedWallpaper();
            }
        }
    }

    private void forceUpdateWallpaperImmediate() {
        if (isWallpaperLoading) {
            Log.d(TAG, "Wallpaper is already loading, skipping duplicate request");
            return;
        }

        if (isUsingMobileData && !showedMobileWarning) {
            showMobileDataWarning();
            showedMobileWarning = true;

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_SHOWED_MOBILE_WARNING, true);
            editor.apply();
            return;
        }

        Log.d(TAG, "Forcing immediate wallpaper update");

        if (!wallpaperCacheQueue.isEmpty()) {
            String wallpaperPath = wallpaperCacheQueue.peek();
            if (wallpaperPath != null) {
                wallpaperLoader.loadWallpaperImmediate(wallpaperPath, false);
            }
        } else {
            downloadAndDisplayFirstWallpaper();
        }

        // 延迟预加载，避免同时进行多个网络请求
        handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                preloadSingleWallpaper();
                            }
                        },
                5000); // 5秒后预加载下一张
    }

    private void forceUpdateWallpaper() {
        if (isUsingMobileData && !showedMobileWarning) {
            showMobileDataWarning();
            showedMobileWarning = true;

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_SHOWED_MOBILE_WARNING, true);
            editor.apply();
            return;
        }

        Log.d(TAG, "Forcing wallpaper update");

        useCachedWallpaper();

        new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                preloadSingleWallpaper();
                            }
                        })
                .start();
    }

    private void checkAndUpdateWallpaperByMinute(int currentMinute) {
        if (currentMinute % 5 == 0) {
            if (currentMinute != lastWallpaperMinute) {
                Log.d(TAG, "Wallpaper update triggered by minute: " + currentMinute);

                useCachedWallpaper();

                lastWallpaperMinute = currentMinute;

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(PREF_LAST_WALLPAPER_MINUTE, lastWallpaperMinute);
                editor.apply();

                if (!isUsingMobileData || showedMobileWarning) {
                    new Thread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            preloadSingleWallpaper();
                                        }
                                    })
                            .start();
                }
            } else {
                Log.d(TAG, "Wallpaper already updated at minute: " + currentMinute);
            }
        }
    }

    private void clearWallpaperCache() {
        try {
            File[] files = wallpaperCacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        file.delete();
                    }
                }
            }

            wallpaperCacheQueue.clear();
            wallpaperDownloadStatus.clear();

        } catch (Exception e) {
            Log.e(TAG, "Error clearing wallpaper cache", e);
        }
    }

    private void loadLocationData() {
        try {
            currentProvince = preferences.getString(PREF_PROVINCE, "北京");
            currentCity = preferences.getString(PREF_CITY, "北京");
            currentDistrict = preferences.getString(PREF_DISTRICT, "");

            // 修复位置显示逻辑：优先显示县区，如果没有县区则显示城市
            if (!currentDistrict.isEmpty()) {
                currentLocationName = currentDistrict;
            } else if (currentProvince.equals(currentCity)) {
                currentLocationName = currentCity;
            } else {
                currentLocationName = currentCity;
            }

            Log.d(TAG, "Location data loaded: " + currentLocationName);
        } catch (Exception e) {
            Log.e(TAG, "Error loading location data", e);
            currentProvince = "北京";
            currentCity = "北京";
            currentDistrict = "";
            currentLocationName = "北京";
        }
    }

    private void createBatteryIcon() {
        // TV模式下不创建电池图标
        if (isTVMode) {
            return;
        }
        
        try {
            // 修复：调整电池布局，将闪电图标放在电池右侧，减少距离
            batteryContainer.setOrientation(LinearLayout.HORIZONTAL);
            batteryContainer.setGravity(Gravity.CENTER_VERTICAL);

            // 电池主体
            batteryBody = new FrameLayout(this);
            LinearLayout.LayoutParams bodyParams =
                    new LinearLayout.LayoutParams(dpToPx(40), dpToPx(20));
            batteryBody.setLayoutParams(bodyParams);

            GradientDrawable bodyDrawable = new GradientDrawable();
            bodyDrawable.setShape(GradientDrawable.RECTANGLE);
            bodyDrawable.setCornerRadius(dpToPx(2));
            bodyDrawable.setStroke(dpToPx(1), Color.WHITE);
            bodyDrawable.setColor(Color.TRANSPARENT);
            batteryBody.setBackground(bodyDrawable);
            batteryBody.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));

            // 电池电量指示器
            batteryLevel = new View(this);
            FrameLayout.LayoutParams levelParams =
                    new FrameLayout.LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT);
            levelParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            batteryLevel.setLayoutParams(levelParams);

            GradientDrawable levelDrawable = new GradientDrawable();
            levelDrawable.setShape(GradientDrawable.RECTANGLE);
            levelDrawable.setCornerRadius(dpToPx(1));
            levelDrawable.setColor(Color.argb(128, 255, 255, 255));
            batteryLevel.setBackground(levelDrawable);

            batteryBody.addView(batteryLevel);

            // 电池正极
            batteryTip = new View(this);
            FrameLayout.LayoutParams tipParams =
                    new FrameLayout.LayoutParams(dpToPx(3), dpToPx(10));
            tipParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            batteryTip.setLayoutParams(tipParams);

            GradientDrawable tipDrawable = new GradientDrawable();
            tipDrawable.setShape(GradientDrawable.RECTANGLE);
            tipDrawable.setCornerRadius(dpToPx(1));
            tipDrawable.setColor(Color.WHITE);
            batteryTip.setBackground(tipDrawable);

            // 电池百分比文字
            batteryPercentTextView = new TextView(this);
            FrameLayout.LayoutParams textParams =
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT);
            textParams.gravity = Gravity.CENTER;
            batteryPercentTextView.setLayoutParams(textParams);
            batteryPercentTextView.setTextSize(10);
            batteryPercentTextView.setText("100%");
            batteryPercentTextView.setTextColor(Color.BLACK);
            batteryPercentTextView.setTypeface(Typeface.DEFAULT_BOLD);

            batteryBody.addView(batteryPercentTextView);

            // 修复：将闪电图标放在电池右侧，减少距离
            chargingIndicatorImageView = new ImageView(this);
            LinearLayout.LayoutParams lightningParams =
                    new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
            lightningParams.gravity = Gravity.CENTER_VERTICAL;
            lightningParams.setMargins(dpToPx(2), 0, 0, 0); // 修复：减少左边距从8dp到2dp
            chargingIndicatorImageView.setLayoutParams(lightningParams);

            try {
                Drawable lightningDrawable =
                        ContextCompat.getDrawable(this, R.drawable.ic_lightning);
                if (lightningDrawable != null) {
                    lightningDrawable = DrawableCompat.wrap(lightningDrawable);
                    chargingIndicatorImageView.setImageDrawable(lightningDrawable);
                }
            } catch (Exception e) {
                TextView fallbackView = new TextView(this);
                fallbackView.setText("⚡");
                fallbackView.setTextSize(16);
                fallbackView.setTextColor(textColors[currentTextColorIndex]);
                batteryContainer.addView(fallbackView);
            }

            // 修复：添加组件到容器的顺序调整
            batteryContainer.addView(batteryBody);
            batteryContainer.addView(batteryTip);
            batteryContainer.addView(chargingIndicatorImageView);

        } catch (Exception e) {
            Log.e(TAG, "Error creating battery icon", e);
        }
    }

    private void updateTimeAndDate() {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault());

            Date now = new Date();
            String currentTime = timeFormat.format(now);
            String currentDate = dateFormat.format(now);

            int currentMinute = now.getMinutes();
            int currentSecond = now.getSeconds();

            if (currentMinute != lastMinute) {
                lastMinute = currentMinute;
                moveClockPosition();

                if (currentMinute % 10 == 0) {
                    checkAndUpdateWeather();
                }

                checkAndUpdateWallpaperByMinute(currentMinute);
            }

            if (!currentDate.equals(currentDateString)) {
                currentDateString = currentDate;
                dateTextView.setText(currentDate);

                String lunarDate = LunarUtil.getLunarDate(now);
                currentLunarDate = lunarDate;

                String festival = LunarUtil.getFestival(now);
                currentFestival = festival;

                lunarTextView.setText(currentLunarDate);

                if (festival != null && !festival.isEmpty()) {
                    festivalTextView.setText(festival);
                    festivalTextView.setVisibility(View.VISIBLE);
                } else {
                    festivalTextView.setVisibility(View.GONE);
                }
            }

            if (currentSecond != lastSecond) {
                lastSecond = currentSecond;
                timeTextView.setText(currentTime);
            }

        } catch (Exception e) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                SimpleDateFormat dateFormat =
                        new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault());

                Date now = new Date();
                String currentTime = timeFormat.format(now);
                String currentDate = dateFormat.format(now);

                timeTextView.setText(currentTime);
                dateTextView.setText(currentDate);
                lunarTextView.setText("农历计算失败");
                festivalTextView.setVisibility(View.GONE);
            } catch (Exception ex) {
                Log.e(TAG, "Error updating time and date", ex);
            }
        }
    }

    private void forceUpdateWeather() {
        new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                fetchWeatherData();
                            }
                        })
                .start();
    }

    private void checkAndUpdateWeather() {
        long currentTime = System.currentTimeMillis();
        lastWeatherUpdateTime = preferences.getLong(PREF_LAST_WEATHER_UPDATE, 0);

        String cachedWeatherData = preferences.getString(PREF_WEATHER_DATA, "");
        if (!cachedWeatherData.isEmpty()) {
            try {
                updateWeatherDisplayFromString(cachedWeatherData);
            } catch (Exception e) {
                Log.e(TAG, "Error loading cached weather data", e);
            }
        }

        if (currentTime - lastWeatherUpdateTime > WEATHER_UPDATE_INTERVAL) {
            new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    fetchWeatherData();
                                }
                            })
                    .start();
        }
    }

    private void fetchWeatherData() {
        HttpURLConnection connection = null;
        try {
            // 优先使用县区，如果没有县区则使用城市
            String locationParam = !currentDistrict.isEmpty() ? currentDistrict : currentCity;

            // 修复：腾讯天气API使用county参数，如果没有县区则只传省和市
            String urlString;
            if (!currentDistrict.isEmpty()) {
                // 有县区：省+市+县区
                urlString =
                        "https://wis.qq.com/weather/common?source=pc&weather_type=observe&province="
                                + currentProvince
                                + "&city="
                                + currentCity
                                + "&county="
                                + locationParam;
            } else {
                // 没有县区：省+市
                urlString =
                        "https://wis.qq.com/weather/common?source=pc&weather_type=observe&province="
                                + currentProvince
                                + "&city="
                                + currentCity;
            }

            urlString = urlString.replace(" ", "%20");

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String weatherDataStr = parseTencentWeatherDataFromResponse(response.toString());

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREF_WEATHER_DATA, weatherDataStr);
                editor.putLong(PREF_LAST_WEATHER_UPDATE, System.currentTimeMillis());
                editor.apply();

                final String finalWeatherDataStr = weatherDataStr;
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                updateWeatherDisplayFromString(finalWeatherDataStr);
                            }
                        });

                Log.d(
                        TAG,
                        "Tencent weather data fetched successfully for location: " + locationParam);
            } else {
                Log.e(TAG, "Failed to fetch Tencent weather data, response code: " + responseCode);
                fetchBackupWeatherData();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching Tencent weather data", e);
            fetchBackupWeatherData();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void fetchBackupWeatherData() {
        HttpURLConnection connection = null;
        try {
            // 优先使用县区，如果没有县区则使用城市
            String locationParam = !currentDistrict.isEmpty() ? currentDistrict : currentCity;
            String urlString = "http://wthrcdn.etouch.cn/weather_mini?city=" + locationParam;

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String weatherDataStr = parseBackupWeatherDataFromResponse(response.toString());

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREF_WEATHER_DATA, weatherDataStr);
                editor.putLong(PREF_LAST_WEATHER_UPDATE, System.currentTimeMillis());
                editor.apply();

                final String finalWeatherDataStr = weatherDataStr;
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                updateWeatherDisplayFromString(finalWeatherDataStr);
                            }
                        });

                Log.d(
                        TAG,
                        "Backup weather data fetched successfully for location: " + locationParam);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching backup weather data", e);
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            String cachedWeatherData = preferences.getString(PREF_WEATHER_DATA, "");
                            if (!cachedWeatherData.isEmpty()) {
                                try {
                                    updateWeatherDisplayFromString(cachedWeatherData);
                                } catch (Exception ex) {
                                    Log.e(TAG, "Error loading cached weather data", ex);
                                }
                            }
                        }
                    });
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String parseTencentWeatherDataFromResponse(String response) {
        try {
            double temperature = 0.0;
            String weatherDescription = "未知";

            if (response.contains("\"degree\"")) {
                int degreeIndex = response.indexOf("\"degree\":");
                int commaIndex = response.indexOf(",", degreeIndex);
                if (commaIndex == -1) commaIndex = response.indexOf("}", degreeIndex);
                if (commaIndex != -1) {
                    String degreeStr =
                            response.substring(degreeIndex + 9, commaIndex)
                                    .replace("\"", "")
                                    .trim();
                    try {
                        temperature = Double.parseDouble(degreeStr);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing temperature: " + degreeStr);
                    }
                }
            }

            if (response.contains("\"weather\"")) {
                int weatherIndex = response.indexOf("\"weather\":");
                int commaIndex = response.indexOf(",", weatherIndex);
                if (commaIndex == -1) commaIndex = response.indexOf("}", weatherIndex);
                if (commaIndex != -1) {
                    int quoteStart = response.indexOf("\"", weatherIndex + 10);
                    int quoteEnd = response.indexOf("\"", quoteStart + 1);
                    if (quoteStart != -1 && quoteEnd != -1) {
                        weatherDescription = response.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }

            int weatherCode = getWeatherCodeFromDescription(weatherDescription);
            return temperature + "," + weatherDescription + "," + weatherCode;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing Tencent weather data from response", e);
            return "0,未知,0";
        }
    }

    private String parseBackupWeatherDataFromResponse(String response) {
        try {
            double temperature = 0.0;
            String weatherDescription = "未知";

            if (response.contains("\"wendu\"")) {
                int tempIndex = response.indexOf("\"wendu\":");
                int commaIndex = response.indexOf(",", tempIndex);
                if (commaIndex == -1) commaIndex = response.indexOf("}", tempIndex);
                if (commaIndex != -1) {
                    String tempStr =
                            response.substring(tempIndex + 8, commaIndex).replace("\"", "").trim();
                    try {
                        temperature = Double.parseDouble(tempStr);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing temperature: " + tempStr);
                    }
                }
            }

            if (response.contains("\"type\"")) {
                int typeIndex = response.indexOf("\"type\":");
                int commaIndex = response.indexOf(",", typeIndex);
                if (commaIndex == -1) commaIndex = response.indexOf("}", typeIndex);
                if (commaIndex != -1) {
                    int quoteStart = response.indexOf("\"", typeIndex +7);
                    int quoteEnd = response.indexOf("\"", quoteStart + 1);
                    if (quoteStart != -1 && quoteEnd != -1) {
                        weatherDescription = response.substring(quoteStart + 1, quoteEnd);
                    }
                }
            }

            int weatherCode = getWeatherCodeFromDescription(weatherDescription);
            return temperature + "," + weatherDescription + "," + weatherCode;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing backup weather data from response", e);
            return "0,未知,0";
        }
    }

    private int getWeatherCodeFromDescription(String weatherDescription) {
        if (weatherDescription.contains("晴")) {
            return 0;
        } else if (weatherDescription.contains("多云") || weatherDescription.contains("阴")) {
            return 3;
        } else if (weatherDescription.contains("雨")) {
            return 61;
        } else if (weatherDescription.contains("雪")) {
            return 71;
        } else if (weatherDescription.contains("雾")) {
            return 45;
        } else if (weatherDescription.contains("雷")) {
            return 95;
        } else {
            return 0;
        }
    }

    /** 从字符串更新天气显示 - 修复重复显示问题，位置显示在温度前面 */
    private void updateWeatherDisplayFromString(String weatherDataStr) {
        try {
            // 修复：添加空指针检查，防止weatherContainer为null时崩溃
            if (weatherContainer == null) {
                Log.w(TAG, "weatherContainer is null, skipping weather display update");
                return;
            }

            String[] parts = weatherDataStr.split(",");
            if (parts.length >= 2) {
                double temperature = Double.parseDouble(parts[0]);
                String weatherDescription = parts[1];
                int weatherCode = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;

                // 位置显示在温度前面
                String locationDisplay = getLocationDisplayText();
                String weatherText =
                        String.format(
                                Locale.getDefault(),
                                "%s %.0f°C %s",
                                locationDisplay,
                                temperature,
                                weatherDescription);
                
                // 修复：添加空指针检查
                if (weatherTextView != null) {
                    weatherTextView.setText(weatherText);
                }

                setWeatherIcon(weatherCode);

                weatherContainer.setVisibility(View.VISIBLE);

                int textColor = textColors[currentTextColorIndex];
                if (weatherTextView != null) {
                    weatherTextView.setTextColor(textColor);
                }

                Log.d(TAG, "Weather display updated: " + weatherText);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating weather display from string", e);
            if (weatherContainer != null) {
                weatherContainer.setVisibility(View.GONE);
            }
        }
    }

    private String getLocationDisplayText() {
        // 修复位置显示逻辑：优先显示县区，如果没有县区则显示城市
        if (!currentDistrict.isEmpty()) {
            return currentDistrict;
        } else if (currentProvince.equals(currentCity)) {
            return currentCity;
        } else {
            return currentCity;
        }
    }

    private void setWeatherIcon(int weatherCode) {
        try {
            // 修复：添加空指针检查
            if (weatherIconImageView == null) {
                return;
            }

            int iconResource = R.drawable.ic_weather_clear;

            if (weatherCode == 0) {
                iconResource = R.drawable.ic_weather_clear;
            } else if (weatherCode > 0 && weatherCode < 4) {
                iconResource = R.drawable.ic_weather_cloudy;
            } else if (weatherCode >= 45 && weatherCode <= 48) {
                iconResource = R.drawable.ic_weather_fog;
            } else if (weatherCode >= 51 && weatherCode <= 67) {
                iconResource = R.drawable.ic_weather_rain;
            } else if (weatherCode >= 71 && weatherCode <= 77) {
                iconResource = R.drawable.ic_weather_snow;
            } else if (weatherCode >= 80 && weatherCode <= 82) {
                iconResource = R.drawable.ic_weather_rain;
            } else if (weatherCode >= 95 && weatherCode <= 99) {
                iconResource = R.drawable.ic_weather_thunderstorm;
            }

            Drawable weatherIcon = ContextCompat.getDrawable(this, iconResource);
            if (weatherIcon != null) {
                weatherIcon = DrawableCompat.wrap(weatherIcon);
                DrawableCompat.setTint(weatherIcon, textColors[currentTextColorIndex]);
                weatherIconImageView.setImageDrawable(weatherIcon);
                weatherIconImageView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting weather icon", e);
            if (weatherIconImageView != null) {
                weatherIconImageView.setVisibility(View.GONE);
            }
        }
    }

    private void updateBatteryDisplay() {
        // TV模式下不更新电池显示
        if (isTVMode) {
            return;
        }
        
        try {
            if (batteryLevelValue >= 0) {
                if (batteryPercentTextView != null) {
                    batteryPercentTextView.setText(
                            String.format(Locale.getDefault(), "%d%%", batteryLevelValue));
                }

                if (chargingIndicatorImageView != null) {
                    if (isCharging) {
                        // 修复：确保充电图标可见
                        chargingIndicatorImageView.setVisibility(View.VISIBLE);
                        DrawableCompat.setTint(
                                chargingIndicatorImageView.getDrawable(),
                                textColors[currentTextColorIndex]);
                    } else {
                        chargingIndicatorImageView.setVisibility(View.GONE);
                    }
                }

                if (batteryBody != null && batteryLevel != null) {
                    int batteryBodyWidth = batteryBody.getWidth();
                    int batteryPadding =
                            batteryBody.getPaddingLeft() + batteryBody.getPaddingRight();
                    int maxLevelWidth = batteryBodyWidth - batteryPadding;

                    int levelWidth = (int) (maxLevelWidth * batteryLevelValue / 100f);

                    android.view.ViewGroup.LayoutParams levelParams =
                            batteryLevel.getLayoutParams();
                    levelParams.width = levelWidth;
                    batteryLevel.setLayoutParams(levelParams);

                    int textColor = textColors[currentTextColorIndex];

                    float[] hsv = new float[3];
                    Color.colorToHSV(textColor, hsv);

                    float brightnessFactor = 0.5f + (batteryLevelValue / 200f);
                    hsv[2] = Math.max(0.3f, Math.min(1.0f, hsv[2] * brightnessFactor));

                    int batteryColor = Color.HSVToColor(hsv);

                    int alphaBatteryColor =
                            Color.argb(
                                    128,
                                    Color.red(batteryColor),
                                    Color.green(batteryColor),
                                    Color.blue(batteryColor));
                    GradientDrawable batteryLevelDrawable =
                            (GradientDrawable) batteryLevel.getBackground();
                    if (batteryLevelDrawable != null) {
                        batteryLevelDrawable.setColor(alphaBatteryColor);
                    }

                    GradientDrawable bodyDrawable = (GradientDrawable) batteryBody.getBackground();
                    if (bodyDrawable != null) {
                        bodyDrawable.setStroke(dpToPx(1), textColor);
                    }

                    GradientDrawable tipDrawable = (GradientDrawable) batteryTip.getBackground();
                    if (tipDrawable != null) {
                        tipDrawable.setColor(textColor);
                    }

                    if (batteryPercentTextView != null) {
                        batteryPercentTextView.setTextColor(textColor);

                        int bgColor = backgroundColors[currentBgColorIndex];
                        if (isColorDark(bgColor)) {
                            batteryPercentTextView.setTextColor(textColor);
                        } else {
                            batteryPercentTextView.setTextColor(darkenColor(textColor));
                        }
                    }
                }
            } else {
                if (batteryPercentTextView != null) {
                    batteryPercentTextView.setText("?");
                }
                if (chargingIndicatorImageView != null) {
                    chargingIndicatorImageView.setVisibility(View.GONE);
                }
                int textColor = textColors[currentTextColorIndex];
                if (batteryPercentTextView != null) {
                    batteryPercentTextView.setTextColor(textColor);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating battery display", e);
        }
    }

    private boolean isColorDark(int color) {
        double darkness =
                1
                        - (0.299 * Color.red(color)
                                        + 0.587 * Color.green(color)
                                        + 0.114 * Color.blue(color))
                                / 255;
        return darkness >= 0.5;
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    private void registerBatteryReceiver() {
        // TV模式下不注册电池接收器
        if (isTVMode) {
            return;
        }
        
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(batteryReceiver, filter);
        } catch (Exception e) {
            Log.e(TAG, "Error registering battery receiver", e);
        }
    }

    private void unregisterBatteryReceiver() {
        try {
            if (batteryReceiver != null) {
                unregisterReceiver(batteryReceiver);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering battery receiver", e);
        }
    }

    private void centerClock() {
        try {
            Resources resources = getResources();
            int screenWidth = resources.getDisplayMetrics().widthPixels;
            int screenHeight = resources.getDisplayMetrics().heightPixels;

            if (clockContainer.getWidth() == 0 || clockContainer.getHeight() == 0) {
                clockContainer.measure(
                        View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(screenHeight, View.MeasureSpec.AT_MOST));
            }

            int clockWidth = clockContainer.getMeasuredWidth();
            int clockHeight = clockContainer.getMeasuredHeight();

            if (clockWidth == 0) clockWidth = 400;
            if (clockHeight == 0) clockHeight = 300;

            int centerX = (screenWidth - clockWidth) / 2;
            int centerY = (screenHeight - clockHeight) / 2;

            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) clockContainer.getLayoutParams();
            params.leftMargin = centerX;
            params.topMargin = centerY;
            clockContainer.setLayoutParams(params);

        } catch (Exception e) {
            Log.e(TAG, "Error centering clock", e);
        }
    }

    private void moveClockPosition() {
        try {
            Resources resources = getResources();
            int screenWidth = resources.getDisplayMetrics().widthPixels;
            int screenHeight = resources.getDisplayMetrics().heightPixels;

            clockContainer.measure(
                    View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(screenHeight, View.MeasureSpec.AT_MOST));
            int clockWidth = clockContainer.getMeasuredWidth();
            int clockHeight = clockContainer.getMeasuredHeight();

            if (clockWidth == 0 || clockHeight == 0) {
                return;
            }

            int minMargin = 80;

            int maxX = screenWidth - clockWidth - minMargin * 2;
            int maxY = screenHeight - clockHeight - minMargin * 2;

            if (maxX > 0 && maxY > 0) {
                int randomX = randomGenerator.nextInt(maxX) + minMargin;
                int randomY = randomGenerator.nextInt(maxY) + minMargin;

                FrameLayout.LayoutParams params =
                        (FrameLayout.LayoutParams) clockContainer.getLayoutParams();
                params.leftMargin = randomX;
                params.topMargin = randomY;
                clockContainer.setLayoutParams(params);
            } else {
                centerClock();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error moving clock position", e);
        }
    }

    private void changeTextColors() {
        try {
            currentTextColorIndex = (currentTextColorIndex + 1) % textColors.length;

            int textColor = textColors[currentTextColorIndex];
            if (timeTextView != null) {
                timeTextView.setTextColor(textColor);
            }
            if (dateTextView != null) {
                dateTextView.setTextColor(textColor);
            }
            if (lunarTextView != null) {
                lunarTextView.setTextColor(textColor);
            }
            if (festivalTextView != null) {
                festivalTextView.setTextColor(textColor);
            }
            if (weatherTextView != null) {
                weatherTextView.setTextColor(textColor);
            }
            if (!isTVMode && batteryPercentTextView != null) {
                batteryPercentTextView.setTextColor(textColor);
            }

            updateBatteryDisplay();

            if (weatherIconImageView != null && weatherIconImageView.getVisibility() == View.VISIBLE) {
                Drawable weatherIcon = weatherIconImageView.getDrawable();
                if (weatherIcon != null) {
                    DrawableCompat.setTint(weatherIcon, textColor);
                }
            }

            saveColorPreferences();

            // 取消文字提示，直接保持农历显示
        } catch (Exception e) {
            Log.e(TAG, "Error changing text colors", e);
        }
    }

    private void changeStyle() {
        try {
            currentStyleIndex = (currentStyleIndex + 1) % 4;

            applyStyle();

            saveColorPreferences();

            // 取消文字提示，直接保持农历显示
        } catch (Exception e) {
            Log.e(TAG, "Error changing style", e);
        }
    }

    private void applyStyle() {
        try {
            int textColor = textColors[currentTextColorIndex];

            // 修复：声明所有局部变量在方法开始处
            FrameLayout.LayoutParams timeParams;
            FrameLayout.LayoutParams batteryParams;
            FrameLayout.LayoutParams tipParams;
            LinearLayout.LayoutParams lightningParams;
            LinearLayout.LayoutParams weatherIconParams;

            switch (currentStyleIndex) {
                case 0:
                    if (timeTextView != null) timeTextView.setTextSize(80);
                    if (dateTextView != null) dateTextView.setTextSize(30);
                    if (lunarTextView != null) lunarTextView.setTextSize(24);
                    if (festivalTextView != null) festivalTextView.setTextSize(24);
                    if (weatherTextView != null) weatherTextView.setTextSize(20);

                    if (timeTextView != null) timeTextView.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                    if (dateTextView != null) dateTextView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                    if (lunarTextView != null) lunarTextView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                    if (festivalTextView != null) festivalTextView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                    if (weatherTextView != null) weatherTextView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

                    // 修复：获取父容器的布局参数
                    if (timeTextView != null) {
                        timeParams = (FrameLayout.LayoutParams) timeTextView.getLayoutParams();
                        timeParams.topMargin = dpToPx(0);
                        timeTextView.setLayoutParams(timeParams);
                    }

                    if (!isTVMode) {
                        if (batteryBody != null) {
                            batteryParams = (FrameLayout.LayoutParams) batteryBody.getLayoutParams();
                            batteryParams.width = dpToPx(40);
                            batteryParams.height = dpToPx(20);
                            batteryBody.setLayoutParams(batteryParams);
                        }

                        if (batteryTip != null) {
                            tipParams = (FrameLayout.LayoutParams) batteryTip.getLayoutParams();
                            tipParams.width = dpToPx(3);
                            tipParams.height = dpToPx(10);
                            batteryTip.setLayoutParams(tipParams);
                        }

                        if (chargingIndicatorImageView != null) {
                            lightningParams = (LinearLayout.LayoutParams) chargingIndicatorImageView.getLayoutParams();
                            lightningParams.width = dpToPx(16);
                            lightningParams.height = dpToPx(16);
                            chargingIndicatorImageView.setLayoutParams(lightningParams);
                        }

                        if (batteryPercentTextView != null) {
                            batteryPercentTextView.setTextSize(10);
                        }

                        if (weatherIconImageView != null) {
                            weatherIconParams = (LinearLayout.LayoutParams) weatherIconImageView.getLayoutParams();
                            weatherIconParams.width = dpToPx(24);
                            weatherIconParams.height = dpToPx(24);
                            weatherIconImageView.setLayoutParams(weatherIconParams);
                        }
                    }
                    break;

                case 1:
                    if (timeTextView != null) timeTextView.setTextSize(70);
                    if (dateTextView != null) dateTextView.setTextSize(28);
                    if (lunarTextView != null) lunarTextView.setTextSize(22);
                    if (festivalTextView != null) festivalTextView.setTextSize(22);
                    if (weatherTextView != null) weatherTextView.setTextSize(18);

                    if (timeTextView != null) timeTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                    if (dateTextView != null) dateTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.ITALIC));
                    if (lunarTextView != null) lunarTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.ITALIC));
                    if (festivalTextView != null) festivalTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
                    if (weatherTextView != null) weatherTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

                    // 修复：获取父容器的布局参数
                    if (timeTextView != null) {
                        timeParams = (FrameLayout.LayoutParams) timeTextView.getLayoutParams();
                        timeParams.topMargin = dpToPx(10);
                        timeTextView.setLayoutParams(timeParams);
                    }

                    if (!isTVMode) {
                        if (batteryBody != null) {
                            batteryParams = (FrameLayout.LayoutParams) batteryBody.getLayoutParams();
                            batteryParams.width = dpToPx(35);
                            batteryParams.height = dpToPx(18);
                            batteryBody.setLayoutParams(batteryParams);
                        }

                        if (batteryTip != null) {
                            tipParams = (FrameLayout.LayoutParams) batteryTip.getLayoutParams();
                            tipParams.width = dpToPx(2);
                            tipParams.height = dpToPx(8);
                            batteryTip.setLayoutParams(tipParams);
                        }

                        if (chargingIndicatorImageView != null) {
                            lightningParams = (LinearLayout.LayoutParams) chargingIndicatorImageView.getLayoutParams();
                            lightningParams.width = dpToPx(14);
                            lightningParams.height = dpToPx(14);
                            chargingIndicatorImageView.setLayoutParams(lightningParams);
                        }

                        if (batteryPercentTextView != null) {
                            batteryPercentTextView.setTextSize(9);
                        }

                        if (weatherIconImageView != null) {
                            weatherIconParams = (LinearLayout.LayoutParams) weatherIconImageView.getLayoutParams();
                            weatherIconParams.width = dpToPx(22);
                            weatherIconParams.height = dpToPx(22);
                            weatherIconImageView.setLayoutParams(weatherIconParams);
                        }
                    }
                    break;

                case 2:
                    if (timeTextView != null) timeTextView.setTextSize(90);
                    if (dateTextView != null) dateTextView.setTextSize(26);
                    if (lunarTextView != null) lunarTextView.setTextSize(20);
                    if (festivalTextView != null) festivalTextView.setTextSize(20);
                    if (weatherTextView != null) weatherTextView.setTextSize(16);

                    if (timeTextView != null) timeTextView.setTypeface(Typeface.create("serif", Typeface.BOLD));
                    if (dateTextView != null) dateTextView.setTypeface(Typeface.create("serif", Typeface.NORMAL));
                    if (lunarTextView != null) lunarTextView.setTypeface(Typeface.create("serif", Typeface.NORMAL));
                    if (festivalTextView != null) festivalTextView.setTypeface(Typeface.create("serif", Typeface.NORMAL));
                    if (weatherTextView != null) weatherTextView.setTypeface(Typeface.create("serif", Typeface.NORMAL));

                    // 修复：获取父容器的布局参数
                    if (timeTextView != null) {
                        timeParams = (FrameLayout.LayoutParams) timeTextView.getLayoutParams();
                        timeParams.topMargin = dpToPx(5);
                        timeTextView.setLayoutParams(timeParams);
                    }

                    if (!isTVMode) {
                        if (batteryBody != null) {
                            batteryParams = (FrameLayout.LayoutParams) batteryBody.getLayoutParams();
                            batteryParams.width = dpToPx(30);
                            batteryParams.height = dpToPx(15);
                            batteryBody.setLayoutParams(batteryParams);
                        }

                        if (batteryTip != null) {
                            tipParams = (FrameLayout.LayoutParams) batteryTip.getLayoutParams();
                            tipParams.width = dpToPx(2);
                            tipParams.height = dpToPx(6);
                            batteryTip.setLayoutParams(tipParams);
                        }

                        if (chargingIndicatorImageView != null) {
                            lightningParams = (LinearLayout.LayoutParams) chargingIndicatorImageView.getLayoutParams();
                            lightningParams.width = dpToPx(12);
                            lightningParams.height = dpToPx(12);
                            chargingIndicatorImageView.setLayoutParams(lightningParams);
                        }

                        if (batteryPercentTextView != null) {
                            batteryPercentTextView.setTextSize(8);
                        }

                        if (weatherIconImageView != null) {
                            weatherIconParams = (LinearLayout.LayoutParams) weatherIconImageView.getLayoutParams();
                            weatherIconParams.width = dpToPx(20);
                            weatherIconParams.height = dpToPx(20);
                            weatherIconImageView.setLayoutParams(weatherIconParams);
                        }
                    }
                    break;

                case 3:
                    if (timeTextView != null) timeTextView.setTextSize(75);
                    if (dateTextView != null) dateTextView.setTextSize(25);
                    if (lunarTextView != null) lunarTextView.setTextSize(20);
                    if (festivalTextView != null) festivalTextView.setTextSize(20);
                    if (weatherTextView != null) weatherTextView.setTextSize(16);

                    if (timeTextView != null) timeTextView.setTypeface(Typeface.create("monospace", Typeface.BOLD));
                    if (dateTextView != null) dateTextView.setTypeface(Typeface.create("monospace", Typeface.BOLD));
                    if (lunarTextView != null) lunarTextView.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
                    if (festivalTextView != null) festivalTextView.setTypeface(Typeface.create("monospace", Typeface.BOLD));
                    if (weatherTextView != null) weatherTextView.setTypeface(Typeface.create("monospace", Typeface.BOLD));

                    // 修复：获取父容器的布局参数
                    if (timeTextView != null) {
                        timeParams = (FrameLayout.LayoutParams) timeTextView.getLayoutParams();
                        timeParams.topMargin = dpToPx(-5);
                        timeTextView.setLayoutParams(timeParams);
                    }

                    if (!isTVMode) {
                        if (batteryBody != null) {
                            batteryParams = (FrameLayout.LayoutParams) batteryBody.getLayoutParams();
                            batteryParams.width = dpToPx(38);
                            batteryParams.height = dpToPx(16);
                            batteryBody.setLayoutParams(batteryParams);
                        }

                        if (batteryTip != null) {
                            tipParams = (FrameLayout.LayoutParams) batteryTip.getLayoutParams();
                            tipParams.width = dpToPx(2);
                            tipParams.height = dpToPx(7);
                            batteryTip.setLayoutParams(tipParams);
                        }

                        if (chargingIndicatorImageView != null) {
                            lightningParams = (LinearLayout.LayoutParams) chargingIndicatorImageView.getLayoutParams();
                            lightningParams.width = dpToPx(14);
                            lightningParams.height = dpToPx(14);
                            chargingIndicatorImageView.setLayoutParams(lightningParams);
                        }

                        if (batteryPercentTextView != null) {
                            batteryPercentTextView.setTextSize(9);
                        }

                        if (weatherIconImageView != null) {
                            weatherIconParams = (LinearLayout.LayoutParams) weatherIconImageView.getLayoutParams();
                            weatherIconParams.width = dpToPx(20);
                            weatherIconParams.height = dpToPx(20);
                            weatherIconImageView.setLayoutParams(weatherIconParams);
                        }
                    }
                    break;
            }

            if (timeTextView != null) timeTextView.setTextColor(textColor);
            if (dateTextView != null) dateTextView.setTextColor(textColor);
            if (lunarTextView != null) lunarTextView.setTextColor(textColor);
            if (festivalTextView != null) festivalTextView.setTextColor(textColor);
            if (weatherTextView != null) weatherTextView.setTextColor(textColor);
            if (!isTVMode && batteryPercentTextView != null) {
                batteryPercentTextView.setTextColor(textColor);
            }

            if (weatherIconImageView != null && weatherIconImageView.getVisibility() == View.VISIBLE) {
                Drawable weatherIcon = weatherIconImageView.getDrawable();
                if (weatherIcon != null) {
                    DrawableCompat.setTint(weatherIcon, textColor);
                }
            }

            updateBatteryDisplay();

            moveClockPosition();
        } catch (Exception e) {
            Log.e(TAG, "Error applying style", e);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void saveColorPreferences() {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREF_TEXT_COLOR_INDEX, currentTextColorIndex);
            editor.putInt(PREF_BG_COLOR_INDEX, currentBgColorIndex);
            editor.putInt(PREF_STYLE_INDEX, currentStyleIndex);
            editor.putInt(PREF_WALLPAPER_CACHE_INDEX, currentWallpaperIndex);
            editor.putBoolean(PREF_SHOWED_MOBILE_WARNING, showedMobileWarning);
            editor.putInt(PREF_LAST_WALLPAPER_MINUTE, lastWallpaperMinute);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving color preferences", e);
        }
    }

    private void loadColorPreferences() {
        try {
            currentTextColorIndex = preferences.getInt(PREF_TEXT_COLOR_INDEX, 0);
            currentBgColorIndex = preferences.getInt(PREF_BG_COLOR_INDEX, 0);
            currentStyleIndex = preferences.getInt(PREF_STYLE_INDEX, 0);
            currentWallpaperIndex = preferences.getInt(PREF_WALLPAPER_CACHE_INDEX, 0);
            showedMobileWarning = preferences.getBoolean(PREF_SHOWED_MOBILE_WARNING, false);
            lastWallpaperMinute = preferences.getInt(PREF_LAST_WALLPAPER_MINUTE, -1);

            if (currentBgColorIndex < backgroundColors.length) {
                if (containerLayout != null) {
                    containerLayout.setBackgroundColor(backgroundColors[currentBgColorIndex]);
                }
            }

            if (currentTextColorIndex < textColors.length) {
                int textColor = textColors[currentTextColorIndex];
                if (timeTextView != null) timeTextView.setTextColor(textColor);
                if (dateTextView != null) dateTextView.setTextColor(textColor);
                if (lunarTextView != null) lunarTextView.setTextColor(textColor);
                if (festivalTextView != null) festivalTextView.setTextColor(textColor);
                if (weatherTextView != null) weatherTextView.setTextColor(textColor);
                if (!isTVMode && batteryPercentTextView != null) {
                    batteryPercentTextView.setTextColor(textColor);
                }
            }

            updateBatteryDisplay();
        } catch (Exception e) {
            Log.e(TAG, "Error loading color preferences", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // 如果需要完全重启，直接返回
        if (needsFullRestart) {
            return;
        }
        
        try {
            hideNavigationBar();

            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire();
            }
            
            // 重新初始化Handler
            handler = new Handler();
            updateTimeRunnable =
                    new Runnable() {
                        @Override
                        public void run() {
                            updateTimeAndDate();
                            handler.postDelayed(this, 1000);
                        }
                    };
            
            // 重新注册接收器
            if (!isTVMode) {
                registerBatteryReceiver();
            }
            
            if (networkReceiver == null) {
                networkReceiver = new NetworkReceiver();
            }
            IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkReceiver, networkFilter);

            checkNetworkType();

            forceUpdateWeather();

            // 延迟初始化壁纸，确保UI完全加载后再处理壁纸
            handler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!hasWallpaperDisplayed && !isAppInitializing) {
                                preloadWallpaperCacheWithFallback();
                            }
                        }
                    },
                    500);

            handler.post(updateTimeRunnable);

            clockContainer.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            centerClock();
                        }
                    },
                    100);

        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            // 如果需要完全重启，直接返回
            if (needsFullRestart) {
                return;
            }
            
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
            
            // 保存状态但不清理资源
            saveColorPreferences();
            
            // 停止Handler但不清理其他资源
            if (handler != null) {
                handler.removeCallbacks(updateTimeRunnable);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onPause", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            saveColorPreferences();

            // 完全清理所有资源
            cleanupResources();

        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }

    // 添加壁纸下载状态管理方法
    private void markWallpaperDownloading(String wallpaperPath) {
        wallpaperDownloadStatus.put(wallpaperPath, false);
    }

    private void markWallpaperDownloadCompleted(String wallpaperPath) {
        wallpaperDownloadStatus.put(wallpaperPath, true);
    }

    private boolean isWallpaperDownloadComplete(String wallpaperPath) {
        Boolean status = wallpaperDownloadStatus.get(wallpaperPath);
        return status != null && status;
    }
}