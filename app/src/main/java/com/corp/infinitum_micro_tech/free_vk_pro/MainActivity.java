package com.corp.infinitum_micro_tech.free_vk_pro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;


    public View mDecorView;

    ProxyData FireProxy;

    EditText ViewHost;
    EditText ViewPort;


    Switch aSwitch_proxy;


    static CheckBox is_refresh_swipe;

    private WebView webView;
    private FrameLayout customViewContainer;

    private WebChromeClient.CustomViewCallback customViewCallback;
    private View mCustomView;
    private myWebChromeClient mWebChromeClient;
    private myWebViewClient mWebViewClient;


    static final String oauth_vk = "https://oauth.vk.com/authorize?client_id=6293014&display=page&redirect_uri=" +
            "https://oauth.vk.com/blank.html&scope=friends,offline,messages,docs,photos,groups&response_type=token&v=5.62";

    private static final String TAG = "myapp";
    private static final String buy_pro_url = "https://play.google.com/store/apps/details?id=com.corp.infinitum_micro_tech.free_vk_pro";
    private static final String main_url = "https://m.vk.com/feed";
    static SwipeRefreshLayout Web_swipe_refresh;


    ClipboardManager clipboardManager;
    ClipData clipData;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Config.ConfigInit(this);

        //WebView
        webView = (WebView) findViewById(R.id.webView);
        mWebViewClient = new myWebViewClient();
        webView.setWebViewClient(mWebViewClient);
        mWebChromeClient = new myWebChromeClient();
        webView.setWebChromeClient(mWebChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        WebSettings mWebSettings = webView.getSettings();
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(false);
        }


        //Proxy Info
        aSwitch_proxy = (Switch) findViewById(R.id.switch_is_use_proxy);
        aSwitch_proxy.setChecked(Config.SetProxy);
        aSwitch_proxy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getApplicationContext(), "Приложение будет перезапущено.", Toast.LENGTH_SHORT).show();
                Config.saveSetProxyState(getApplicationContext(), isChecked);
                doRestart(getApplicationContext());
            }
        });
        ViewHost = (EditText) findViewById(R.id.viewHost);
        ViewPort = (EditText) findViewById(R.id.viewPort);
        if (!Config.UserHost.equals("")) {
            ViewHost.setText(Config.UserHost);
            ViewPort.setText(String.valueOf(Config.UserPort));
        }


        //layouts

        customViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);
        mDecorView = getWindow().getDecorView();


        //NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Swipe Refresh
        Web_swipe_refresh = findViewById(R.id.web_swipe_refresh_layout);
        is_refresh_swipe = findViewById(R.id.isRefresh_page_swipe);

        is_refresh_swipe.setChecked(Config.EnableSwipeRefresh);
        Web_swipe_refresh.setEnabled(Config.EnableSwipeRefresh);

        is_refresh_swipe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Config.saveEnableSwipeRefresh(getApplicationContext(), isChecked);
                Web_swipe_refresh.setEnabled(isChecked);
            }
        });
        Web_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_page();
            }
        });
        Web_swipe_refresh.setRefreshing(true);


        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (Config.SetProxy) {
            if (!Config.UserHost.equals("")) {
                LoadPage(Config.UserHost, Config.UserPort, main_url);
            } else {
                Log.d(TAG, "onCreate: load fire proxy");
                LoadPage(main_url, true);
            }
        } else {
            LoadPage(main_url, false);
        }
        //LoadPage( "194.67.201.106",3128,"");

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        webView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
        if (inCustomView()) {
            hideCustomView();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (inCustomView()) {
                hideCustomView();
                return true;
            }

            if ((mCustomView == null) && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    void loadMainUrl() {
        if (Config.Oauth) {
            Log.d(TAG, "LoadPage: load feed");
            webView.loadUrl(main_url);
        } else {
            webView.loadUrl(oauth_vk);
        }

    }

    void LoadPage(String host, int port, String url) {

        com.corp.infinitum_micro_tech.free_vk_pro.Proxy.setProxy(host, port, webView, getApplicationContext());
        Log.d(TAG, "LoadPage: i try load page PROXY: host " + host + " " + "port " + port);
        loadMainUrl();

    }


    void LoadPage(final String url, boolean set_proxy) {
        if (set_proxy) {
            //Firebase init proxy
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FireProxy = new ProxyData();
            db.collection("proxy")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "onComplete: proxy find");
                                    LoadPage(document.getString("host"), Integer.parseInt(document.getString("port")), url);
                                    return;
                                }
                            } else {
                                Log.d(TAG, "onComplete: error");

                            }
                        }
                    });
        } else {
            loadMainUrl();

        }


    }


    public void ApplyChanges(View view) {
        if (ViewHost.getText().equals("") && (ViewPort.getText().equals(""))) {
            Toast.makeText(getApplicationContext(), "Приложение будет перезапущено.", Toast.LENGTH_SHORT).show();
            Config.deleteUserProxy(getApplicationContext());
            doRestart(getApplicationContext());
            return;
        }

        if ((!ViewHost.getText().equals("")) && (!ViewPort.getText().equals("")) && (!ViewHost.getText().equals("Стандарт")) && (!ViewPort.getText().equals("Стандарт"))) {
            ProxyData p = new ProxyData();
            p.Host = ViewHost.getText().toString();
            try {
                p.Port = Integer.parseInt(String.valueOf(ViewPort.getText()));

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Ошибка ввода!", Toast.LENGTH_SHORT).show();
                return;
            }
            Config.saveUserProxy(getApplicationContext(), p);
            if (Config.SetProxy) {
                doRestart(getApplicationContext());
            }

        }
    }

    public void ToDefault(View view) {
        Config.deleteUserProxy(getApplicationContext());
        Config.saveSetProxyState(getApplicationContext(), true);
        doRestart(getApplicationContext());
    }

    public void WebBack(View view) {

        if (webView.canGoBack()){
            webView.goBack();
        }
    }




    public void CopyLink(View view) {
        clipData = ClipData.newPlainText("text", webView.getUrl());
        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(getApplicationContext(), "Ссылка скопирована", Toast.LENGTH_SHORT).show();

    }

    public void RefreshPage(View view) {
        refresh_page();

    }

    public void refresh_page() {
        Web_swipe_refresh.setRefreshing(true);
        String url_temp = webView.getUrl();
        if (hasConnection(webView.getContext())) {
            if (webView.getUrl().contains("load_error.html") || webView.getUrl().contains("error_network.html")) {
                Web_swipe_refresh.setRefreshing(false);
                Log.d(TAG, "refresh_page: load feed");
                if (Config.Oauth) {
                    webView.loadUrl("https://m.vk.com/feed");
                } else {
                    webView.loadUrl(oauth_vk);
                }

            } else {
                webView.reload();
            }

        } else {
            Web_swipe_refresh.setRefreshing(false);
            webView.loadUrl("file:///android_asset/error_network.html");
        }
    }

    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        return false;
    }

    // Прячем панель навигации и строку состояния
    private void hideSystemUI() {
        // Используем флаг IMMERSIVE.


        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // прячем панель навигации
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // прячем строку состояния

                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY


        );

        Web_swipe_refresh.setEnabled(false);
        Web_swipe_refresh.setVisibility(View.GONE);

    }

    // Программно выводим системные панели обратно
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // прячем панель навигации
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // прячем строку состояния

                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY


        );

        Web_swipe_refresh.setEnabled(Config.EnableSwipeRefresh);

        Web_swipe_refresh.setVisibility(View.VISIBLE);
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        mDecorView.setSystemUiVisibility(uiOptions);
        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        mDecorView.setSystemUiVisibility(uiOptions);


    }


    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                    }
                } else {
                }
            } else {
            }
        } catch (Exception ex) {
        }
    }


    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }


    class myWebChromeClient extends WebChromeClient {
        private Bitmap mDefaultVideoPoster;
        private View mVideoProgressView;

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);    //To change body of overridden methods use File | Settings | File Templates.

        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            // if a view already exists then immediately terminate the new one

            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            webView.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.addView(view);
            customViewCallback = callback;
            hideSystemUI();

        }

        @Override
        public View getVideoLoadingProgressView() {

            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
            }
            return mVideoProgressView;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();


            //To change body of overridden methods use File | Settings | File Templates.
            if (mCustomView == null)
                return;

            customViewContainer.setVisibility(View.GONE);

            // Hide the custom view.

            // Remove the custom view from its container
            //
            mCustomView.setVisibility(View.GONE);

            customViewContainer.removeView(mCustomView);
            customViewCallback.onCustomViewHidden();

            mCustomView = null;

            webView.setVisibility(View.VISIBLE);


            showSystemUI();

        }

        // For 3.0+ Devices (Start)
        // onActivityResult attached before constructor
        protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            showUploadDialog(uploadMsg);
        }


        // For Lollipop 5.0+ Devices
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {


            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                intent = fileChooserParams.createIntent();
            }
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                return false;
            }
            return true;
        }

        //For Android 4.1 only
        protected void openFileChooser(final ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            showUploadDialog(uploadMsg);
            return;
        }

        protected void openFileChooser(final ValueCallback<Uri> uploadMsg) {
            showUploadDialog(uploadMsg);
            return;

        }

        void showUploadDialog(final ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }


    }


    class myWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            Web_swipe_refresh.setRefreshing(false);
            if ((url.contains("vk.com/login"))) {
                    webView.clearCache(true);
                    webView.clearHistory();
                    webView.clearFormData();
                Config.saveOauth(getApplicationContext(), false);
                webView.loadUrl(oauth_vk);


            }

        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (hasConnection(webView.getContext())) {
                webView.loadUrl("file:///android_asset/load_error.html");

            } else {
                webView.loadUrl("file:///android_asset/error_network.html");

            }

            super.onReceivedError(view, request, error);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("vk.com")) {
                if (!url.contains("m.vk") && !url.contains("oauth.vk")) {
                    Log.d(TAG, "shouldOverrideUrlLoading: zoom on");
                    webView.getSettings().setDisplayZoomControls(true);
                    webView.getSettings().setSupportZoom(true);
                    webView.getSettings().setBuiltInZoomControls(true);

                } else {
                    if (webView.getSettings().getDisplayZoomControls()) {
                        Log.d(TAG, "shouldOverrideUrlLoading: zoom off");
                        webView.getSettings().setDisplayZoomControls(false);
                        webView.getSettings().setSupportZoom(false);
                        webView.getSettings().setBuiltInZoomControls(false);
                    }
                }

                if (url.contains("oauth.vk.com/blank.html#")) {
                    Config.saveOauth(getApplicationContext(), true);
                    webView.loadUrl(main_url);
                }

                return super.shouldOverrideUrlLoading(view, url);


            } else {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(intent);
            webView.goBack();
            return false;
            }
            //To change body of overridden methods use File | Settings | File Templates.
        }

    }

}