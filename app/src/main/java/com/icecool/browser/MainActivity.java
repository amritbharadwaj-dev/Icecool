package com.icecool.browser;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int BLUE = 0xFF2979FF;
    private static final int INK = 0xFF3C4043;
    private static final int MAX_TABS = 20;

    private static final String EXTRA_MODE = "ice_mode";
    private static final String EXTRA_NAME = "ice_name";
    private static final String EXTRA_THEME = "ice_theme";
    private static final String MODE_APP = "app";
    private static final String MODE_WEB = "web";

    private static final int[] PASTELS = {0xFFE3F2FD, 0xFFFCE4EC, 0xFFE8F5E9, 0xFFFFF3E0, 0xFFF3E5F5, 0xFFE0F7FA};

    // Runs inside the page: detects manifest + service worker and reports back.
    private static final String PWA_JS =
            "(function(){"
                    + "var T='__TOKEN__';"
                    + "function send(o){try{IceBridge.onPwaInfo(T,JSON.stringify(o));}catch(e){}}"
                    + "var info={title:document.title||'',manifest:false,sw:false,name:'',icon:'',start:'',theme:'',bg:'',maskable:false};"
                    + "var tm=document.querySelector('meta[name=\"theme-color\"]');if(tm&&tm.content)info.theme=tm.content;"
                    + "var fav=document.querySelector('link[rel~=\"apple-touch-icon\"]')||document.querySelector('link[rel~=\"icon\"]');"
                    + "if(fav&&fav.href)info.icon=fav.href;"
                    + "var done=0;function fin(){done++;if(done>=2)send(info);}"
                    + "try{if(navigator.serviceWorker&&navigator.serviceWorker.getRegistrations){"
                    + "navigator.serviceWorker.getRegistrations().then(function(r){info.sw=r.length>0;fin();}).catch(function(){fin();});"
                    + "}else{fin();}}catch(e){fin();}"
                    + "var link=document.querySelector('link[rel~=\"manifest\"]');"
                    + "if(link&&link.href){fetch(link.href).then(function(r){return r.json();}).then(function(m){"
                    + "info.manifest=true;info.name=m.short_name||m.name||'';if(m.theme_color&&!info.theme)info.theme=m.theme_color;if(m.background_color)info.bg=m.background_color;"
                    + "if(m.start_url){try{info.start=new URL(m.start_url,link.href).href;}catch(e){}}"
                    + "if(m.icons&&m.icons.length){var best=null,bs=-1,bm=null,bms=-1;"
                    + "for(var i=0;i<m.icons.length;i++){var ic=m.icons[i];if(/\\.svg(\\?|$)/i.test(ic.src||''))continue;"
                    + "var sz=parseInt(((ic.sizes||'').split(' ')[0]||'0').split('x')[0])||0;"
                    + "if((ic.purpose||'').indexOf('maskable')>=0){if(sz>=bms){bms=sz;bm=ic;}}else if(sz>=bs){bs=sz;best=ic;}}"
                    + "var pick=bm||best;if(pick){info.maskable=!!bm;try{info.icon=new URL(pick.src,link.href).href;}catch(e){}}}"
                    + "fin();}).catch(function(){fin();});}else{fin();}"
                    + "})();";

    // Runs inside the installed app: follows the page's live <meta name="theme-color"> like Chrome does.
    private static final String THEME_JS =
            "(function(){if(window.__iceTheme)return;window.__iceTheme=1;"
                    + "function g(){var ms=document.querySelectorAll('meta[name=\"theme-color\"]');"
                    + "for(var i=0;i<ms.length;i++){var m=ms[i];if(!m.media||window.matchMedia(m.media).matches)return m.content||'';}return '';}"
                    + "var last=null;function s(){var c=g();if(c!==last){last=c;try{IceBridge.onTheme(c);}catch(e){}}}"
                    + "s();try{new MutationObserver(s).observe(document.documentElement,{subtree:true,attributes:true,childList:true,attributeFilter:['content','name']});}catch(e){}"
                    + "setInterval(s,1500);})();";

    private FrameLayout root, webContainer, tabSwitcher;
    private LinearLayout tabGrid;
    private ScrollView homeView;
    private EditText etSearchUrl, etHomeSearch;
    private TextView btnTabCounter, switcherTitle;
    private ProgressBar progressBar;
    private View accentLine;
    private TabManager tabManager;
    private PopupWindow menuPopup;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private int currentZoomLevel = 100;

    private boolean appMode = false;
    private WebView appWebView;
    private String pendingToken = null;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Map<TabManager.TabItem, Bitmap> thumbs = new HashMap<>();
    private final Map<String, Bitmap> faviconCache = Collections.synchronizedMap(new HashMap<String, Bitmap>());

    // ------------------------------------------------------------------ lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root);
        tabManager = TabManager.getInstance();

        Window w = getWindow();
        w.setStatusBarColor(Color.WHITE);
        w.setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            w.getDecorView().setSystemUiVisibility(
                    w.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        boolean edge = Build.VERSION.SDK_INT >= 35 && getApplicationInfo().targetSdkVersion >= 35;
        if (edge) {
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                v.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
                return insets;
            });
        }

        checkPermissions();

        Intent in = getIntent();
        if (in != null && MODE_APP.equals(in.getStringExtra(EXTRA_MODE)) && in.getData() != null) {
            startAppMode(in);
            return;
        }

        buildUi();
        if (savedInstanceState == null && handleWebIntent(in)) {
            // opened a shortcut / link in a new tab
        } else if (tabManager.getTabs().isEmpty()) {
            createNewTab("");
        } else {
            renderActiveTab();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (!appMode && handleWebIntent(intent)) {
            if (tabSwitcher != null) tabSwitcher.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appMode && appWebView != null) {
            try {
                root.removeView(appWebView);
                appWebView.destroy();
            } catch (Throwable ignored) {
            }
        }
    }

    private boolean handleWebIntent(Intent in) {
        if (in == null) return false;
        Uri d = in.getData();
        if (d == null) return false;
        if (MODE_APP.equals(in.getStringExtra(EXTRA_MODE))) return false;
        String s = d.getScheme();
        if (!"http".equals(s) && !"https".equals(s)) return false;
        createNewTab(d.toString());
        return true;
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> p = new ArrayList<>();
            p.add(Manifest.permission.CAMERA);
            p.add(Manifest.permission.RECORD_AUDIO);
            p.add(Manifest.permission.ACCESS_FINE_LOCATION);
            if (Build.VERSION.SDK_INT <= 28) p.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT >= 33) p.add("android.permission.POST_NOTIFICATIONS");
            ActivityCompat.requestPermissions(this, p.toArray(new String[0]), 101);
        }
    }

    // ------------------------------------------------------------------ standalone app mode (installed PWA)

    private void startAppMode(Intent in) {
        appMode = true;
        String url = in.getData().toString();
        String name = in.getStringExtra(EXTRA_NAME);
        Integer parsedTheme = parseCssColor(in.getStringExtra(EXTRA_THEME));
        int theme = parsedTheme != null ? parsedTheme : Color.WHITE;
        if (name != null && !name.isEmpty()) {
            try {
                setTaskDescription(new ActivityManager.TaskDescription(name));
            } catch (Throwable ignored) {
            }
        }

        applyBarColor(theme);

        appWebView = new WebView(this);
        setupWebView(appWebView);
        root.addView(appWebView, new FrameLayout.LayoutParams(MATCH, MATCH));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        progressBar.setProgressTintList(ColorStateList.valueOf(isLight(theme) ? BLUE : Color.WHITE));
        root.addView(progressBar, new FrameLayout.LayoutParams(MATCH, dp(3), Gravity.TOP));

        appWebView.loadUrl(url);
    }

    private void applyBarColor(int c) {
        Window w = getWindow();
        w.setStatusBarColor(c);
        w.setNavigationBarColor(c);
        boolean light = isLight(c);
        int f = w.getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            f = light ? (f | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) : (f & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            f = light ? (f | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) : (f & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        w.getDecorView().setSystemUiVisibility(f);
        root.setBackgroundColor(c);
    }

    private Integer parseCssColor(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try {
            if (s.startsWith("#")) {
                if (s.length() == 4) {
                    s = "#" + s.charAt(1) + s.charAt(1) + s.charAt(2) + s.charAt(2) + s.charAt(3) + s.charAt(3);
                }
                return Color.parseColor(s);
            }
            if (s.startsWith("rgb")) {
                String inner = s.substring(s.indexOf('(') + 1, s.indexOf(')')).trim();
                String[] p = inner.split("[,\\s/]+");
                return Color.rgb((int) Float.parseFloat(p[0]), (int) Float.parseFloat(p[1]), (int) Float.parseFloat(p[2]));
            }
            return Color.parseColor(s);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isLight(int c) {
        double l = (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)) / 255.0;
        return l > 0.6;
    }

    // ------------------------------------------------------------------ install / create shortcut

    /** Called from JS inside the page. Only accepted while a check is pending (token must match). */
    public class PwaBridge {
        @JavascriptInterface
        public void onTheme(final String css) {
            runOnUiThread(() -> {
                if (!appMode) return;
                Integer c = parseCssColor(css);
                if (c != null) applyBarColor(c);
            });
        }

        @JavascriptInterface
        public void onPwaInfo(final String token, final String json) {
            runOnUiThread(() -> {
                if (token == null || !token.equals(pendingToken)) return;
                pendingToken = null;
                TabManager.TabItem tab = currentTab();
                if (tab == null) return;
                try {
                    JSONObject o = new JSONObject(json);
                    boolean pwa = o.optBoolean("manifest") && o.optBoolean("sw");
                    onSiteInfo(tab.url, o.optString("title"), pwa, o.optString("name"),
                            o.optString("icon"), o.optString("start"), o.optString("theme"),
                            o.optBoolean("maskable"), o.optString("bg"));
                } catch (Exception e) {
                    onSiteInfo(tab.url, tab.title == null ? "" : tab.title, false, "", "", "", "", false, "");
                }
            });
        }
    }

    private void startInstallFlow() {
        final TabManager.TabItem tab = currentTab();
        if (tab == null || tab.url == null || tab.url.isEmpty() || tab.webView == null) {
            toast("Pehle koi website kholo");
            return;
        }
        final String token = UUID.randomUUID().toString();
        pendingToken = token;
        toast("Site check ho rahi hai...");
        tab.webView.evaluateJavascript(PWA_JS.replace("__TOKEN__", token), null);
        mainHandler.postDelayed(() -> {
            if (token.equals(pendingToken)) {
                pendingToken = null;
                onSiteInfo(tab.url, tab.title == null ? "" : tab.title, false, "", "", "", "", false, "");
            }
        }, 5000);
    }

    private void onSiteInfo(String pageUrl, String title, final boolean pwa, String mName,
                            final String icon, String start, final String theme,
                            final boolean maskable, final String bg) {
        final String host = hostOf(pageUrl);
        final String defName = !mName.isEmpty() ? mName : (!title.isEmpty() ? title : host);
        String launch = pageUrl;
        if (pwa && !start.isEmpty() && hostOf(start).equals(host)) launch = start;
        final String launchUrl = launch;

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(12), dp(22), 0);

        TextView msg = new TextView(this);
        msg.setText(pwa
                ? "Ye site installable app hai (manifest + service worker mila). Install karne par ye full screen me, bina URL bar ke, native app jaisi khulegi."
                : "Is site me manifest/service worker nahi hai, isliye home screen par shortcut banega. Shortcut dabane par site browser me khulegi.");
        msg.setTextSize(14);
        msg.setTextColor(0xFF5F6368);
        box.addView(msg, new LinearLayout.LayoutParams(MATCH, WRAP));

        final EditText et = new EditText(this);
        et.setText(defName);
        et.setSingleLine(true);
        et.setSelectAllOnFocus(true);
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(MATCH, WRAP);
        elp.topMargin = dp(10);
        box.addView(et, elp);

        new AlertDialog.Builder(this)
                .setTitle(pwa ? "Install app" : "Add to Home screen")
                .setView(box)
                .setPositiveButton(pwa ? "Install" : "Add", (d, w) -> {
                    String nm = et.getText().toString().trim();
                    if (nm.isEmpty()) nm = defName;
                    createShortcut(nm, launchUrl, icon, pwa, theme, host, maskable, bg);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createShortcut(final String name, final String url, final String iconUrl,
                                final boolean app, final String theme, final String host,
                                final boolean maskable, final String bg) {
        toast("Shortcut ban raha hai...");
        new Thread(() -> {
            Bitmap src = iconUrl.isEmpty() ? null : downloadBitmap(iconUrl);
            if (src == null && !host.isEmpty()) {
                src = downloadBitmap("https://www.google.com/s2/favicons?domain=" + host + "&sz=128");
            }
            final Bitmap icon = composeAdaptive(src, maskable, bg, name);
            runOnUiThread(() -> pinShortcut(name, url, icon, app, theme));
        }).start();
    }

    private void pinShortcut(String name, String url, Bitmap icon, boolean app, String theme) {
        try {
            if (!ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
                toast("Is launcher me shortcut support nahi hai");
                return;
            }
            Intent i = new Intent(this, MainActivity.class);
            i.setAction(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.putExtra(EXTRA_MODE, app ? MODE_APP : MODE_WEB);
            i.putExtra(EXTRA_NAME, name);
            i.putExtra(EXTRA_THEME, theme == null ? "" : theme);
            if (app) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            } else {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
            String id = (app ? "app_" : "web_") + Integer.toHexString(url.hashCode());
            ShortcutInfoCompat info = new ShortcutInfoCompat.Builder(this, id)
                    .setShortLabel(name)
                    .setLongLabel(name)
                    .setIcon(IconCompat.createWithAdaptiveBitmap(icon))
                    .setIntent(i)
                    .build();
            ShortcutManagerCompat.requestPinShortcut(this, info, null);
            toast("Home screen par add karne ka popup confirm karo");
        } catch (Exception e) {
            toast("Shortcut nahi ban paya");
        }
    }

    private Bitmap composeAdaptive(Bitmap src, boolean maskable, String bgCss, String name) {
        int s = 432; // adaptive icon canvas (108dp x4); launcher applies its own mask
        Bitmap out = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (src != null) {
            int bg = Color.WHITE;
            int corner = src.getPixel(0, 0);
            Integer parsed = parseCssColor(bgCss);
            if (Color.alpha(corner) >= 250) bg = corner;
            else if (parsed != null) bg = parsed;
            c.drawColor(bg);
            int t = (int) (s * (maskable ? 0.84f : 0.58f));
            Bitmap sc = Bitmap.createScaledBitmap(src, t, t, true);
            c.drawBitmap(sc, (s - t) / 2f, (s - t) / 2f, null);
        } else {
            p.setShader(new LinearGradient(0, 0, s, s, 0xFF00E5FF, 0xFF7C4DFF, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, s, s, p);
            p.setShader(null);
            p.setColor(Color.WHITE);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(190);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            String letter = name == null || name.isEmpty() ? "I" : name.substring(0, 1).toUpperCase();
            c.drawText(letter, s / 2f, s / 2f - (p.descent() + p.ascent()) / 2f, p);
        }
        return out;
    }

    private Bitmap downloadBitmap(String u) {
        try {
            if (!u.startsWith("http")) return null;
            HttpURLConnection con = (HttpURLConnection) new URL(u).openConnection();
            con.setConnectTimeout(7000);
            con.setReadTimeout(7000);
            InputStream is = con.getInputStream();
            Bitmap b = BitmapFactory.decodeStream(is);
            is.close();
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ UI building

    private int dp(float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private GradientDrawable roundBg(int color, float radiusDp, int strokeColor, float strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) g.setStroke(dp(strokeDp), strokeColor);
        return g;
    }

    private GradientDrawable ovalBg(int color, int strokeColor, float strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        if (strokeDp > 0) g.setStroke(dp(strokeDp), strokeColor);
        return g;
    }

    private Drawable ripple(boolean borderless) {
        TypedValue tv = new TypedValue();
        int attr = borderless ? android.R.attr.selectableItemBackgroundBorderless
                : android.R.attr.selectableItemBackground;
        getTheme().resolveAttribute(attr, tv, true);
        if (tv.resourceId == 0) return null;
        return getResources().getDrawable(tv.resourceId, getTheme());
    }

    private View iconButton(int type, View.OnClickListener l) {
        return iconButton(type, INK, l);
    }

    private View iconButton(int type, int color, View.OnClickListener l) {
        IconView iv = new IconView(this, type, color);
        iv.setPadding(dp(9), dp(9), dp(9), dp(9));
        iv.setBackground(ripple(true));
        iv.setOnClickListener(l);
        return iv;
    }

    private void applyGradient(TextView tv, int[] colors) {
        float wd = tv.getPaint().measureText(tv.getText().toString());
        tv.getPaint().setShader(new LinearGradient(0, 0, Math.max(wd, 1f), 0, colors, null, Shader.TileMode.CLAMP));
    }

    private void showProgress(int value) {
        if (progressBar == null) return;
        if (value < 0 || value >= 100) {
            progressBar.setVisibility(View.GONE);
            if (accentLine != null) accentLine.setVisibility(View.VISIBLE);
        } else {
            progressBar.setProgress(value);
            progressBar.setVisibility(View.VISIBLE);
            if (accentLine != null) accentLine.setVisibility(View.INVISIBLE);
        }
    }

    private void buildUi() {
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        root.addView(main, new FrameLayout.LayoutParams(MATCH, MATCH));

        main.addView(buildTopBar());

        FrameLayout line = new FrameLayout(this);
        accentLine = new View(this);
        accentLine.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF00E5FF, 0xFF2979FF, 0xFF7C4DFF, 0xFFFF4081}));
        line.addView(accentLine, new FrameLayout.LayoutParams(MATCH, MATCH));
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        progressBar.setProgressTintList(ColorStateList.valueOf(BLUE));
        line.addView(progressBar, new FrameLayout.LayoutParams(MATCH, MATCH));
        main.addView(line, new LinearLayout.LayoutParams(MATCH, dp(3)));

        webContainer = new FrameLayout(this);
        main.addView(webContainer, new LinearLayout.LayoutParams(MATCH, 0, 1f));

        homeView = buildHomeView();
        tabSwitcher = buildTabSwitcher();
        tabSwitcher.setVisibility(View.GONE);
        root.addView(tabSwitcher, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private View buildTopBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(Color.WHITE);
        bar.setPadding(dp(6), 0, dp(6), 0);
        bar.setElevation(dp(2));
        bar.setLayoutParams(new LinearLayout.LayoutParams(MATCH, dp(56)));

        bar.addView(iconButton(IconView.HOME, 0xFF2979FF, v -> navigateHome()), new LinearLayout.LayoutParams(dp(40), dp(40)));
        bar.addView(iconButton(IconView.REFRESH, 0xFF00ACC1, v -> refreshCurrentTab()), new LinearLayout.LayoutParams(dp(40), dp(40)));

        LinearLayout pill = new LinearLayout(this);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setBackground(roundBg(0xFFEEF1FF, 20, 0, 0));
        pill.setPadding(dp(12), 0, dp(8), 0);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(0, dp(40), 1f);
        plp.setMargins(dp(4), 0, dp(6), 0);

        pill.addView(new IconView(this, IconView.SEARCH, 0xFF7C4DFF), new LinearLayout.LayoutParams(dp(18), dp(18)));
        etSearchUrl = new EditText(this);
        etSearchUrl.setBackground(null);
        etSearchUrl.setHint("Search Google or type URL");
        etSearchUrl.setHintTextColor(0xFF70757A);
        etSearchUrl.setTextColor(0xFF202124);
        etSearchUrl.setTextSize(14);
        etSearchUrl.setSingleLine(true);
        etSearchUrl.setSelectAllOnFocus(true);
        etSearchUrl.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        etSearchUrl.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        etSearchUrl.setPadding(dp(8), 0, 0, 0);
        etSearchUrl.setOnEditorActionListener((v, actionId, event) -> handleSearch(etSearchUrl, actionId, event));
        pill.addView(etSearchUrl, new LinearLayout.LayoutParams(0, MATCH, 1f));
        bar.addView(pill, plp);

        btnTabCounter = new TextView(this);
        btnTabCounter.setGravity(Gravity.CENTER);
        btnTabCounter.setText("1");
        btnTabCounter.setTextColor(BLUE);
        btnTabCounter.setTextSize(12);
        btnTabCounter.setTypeface(Typeface.DEFAULT_BOLD);
        btnTabCounter.setBackground(roundBg(Color.TRANSPARENT, 7, BLUE, 2));
        btnTabCounter.setOnClickListener(v -> showTabSwitcher());
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(dp(26), dp(26));
        clp.setMargins(dp(6), 0, dp(8), 0);
        bar.addView(btnTabCounter, clp);

        bar.addView(iconButton(IconView.DOTS, this::showMenu), new LinearLayout.LayoutParams(dp(40), dp(40)));
        return bar;
    }

    private boolean handleSearch(EditText et, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH
                || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
            String q = et.getText().toString().trim();
            if (!q.isEmpty()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                et.clearFocus();
                loadUrlInCurrentTab(formatUrl(q));
            }
            return true;
        }
        return false;
    }

    private ScrollView buildHomeView() {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFFF3F6FF, 0xFFFFF1F7}));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(0, 0, 0, dp(28));

        col.addView(new IceScene(this), new LinearLayout.LayoutParams(MATCH, dp(220)));

        // Big stylish title: ICE (heavy, cyan->blue) + cool (script, purple->pink->orange)
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView ice = new TextView(this);
        ice.setText("ICE");
        ice.setTextSize(54);
        ice.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        ice.setLetterSpacing(0.1f);
        ice.setIncludeFontPadding(false);
        ice.setShadowLayer(dp(10), 0, dp(3), 0x6600B4D8);
        applyGradient(ice, new int[]{0xFF00E5FF, 0xFF2979FF});
        titleRow.addView(ice, new LinearLayout.LayoutParams(WRAP, WRAP));

        TextView flake = new TextView(this);
        flake.setText("\u2744");
        flake.setTextSize(20);
        flake.setTextColor(0xFFFF4081);
        flake.setPadding(dp(6), 0, dp(2), 0);
        titleRow.addView(flake, new LinearLayout.LayoutParams(WRAP, WRAP));

        TextView cool = new TextView(this);
        cool.setText("cool");
        cool.setTextSize(52);
        cool.setTypeface(Typeface.create("cursive", Typeface.BOLD_ITALIC));
        cool.setIncludeFontPadding(false);
        cool.setPadding(0, 0, dp(6), 0);
        cool.setShadowLayer(dp(10), 0, dp(3), 0x66FF4081);
        applyGradient(cool, new int[]{0xFF7C4DFF, 0xFFFF4081, 0xFFFF9100});
        titleRow.addView(cool, new LinearLayout.LayoutParams(WRAP, WRAP));
        col.addView(titleRow, new LinearLayout.LayoutParams(WRAP, WRAP));

        TextView tag = new TextView(this);
        tag.setText("Stay chill  \u2022  Browse fast");
        tag.setTextColor(0xFF5C6BC0);
        tag.setTextSize(13);
        tag.setTypeface(Typeface.DEFAULT_BOLD);
        tag.setLetterSpacing(0.05f);
        tag.setBackground(roundBg(0x1F7C4DFF, 16, 0, 0));
        tag.setPadding(dp(16), dp(6), dp(16), dp(6));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(WRAP, WRAP);
        tlp.setMargins(0, dp(10), 0, dp(20));
        col.addView(tag, tlp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(roundBg(Color.WHITE, 28, 0xFFD6DCFF, 2));
        card.setElevation(dp(5));
        card.setPadding(dp(18), 0, dp(14), 0);
        card.addView(new IconView(this, IconView.SEARCH, 0xFF2979FF), new LinearLayout.LayoutParams(dp(22), dp(22)));
        etHomeSearch = new EditText(this);
        etHomeSearch.setBackground(null);
        etHomeSearch.setHint("Search Google or type URL");
        etHomeSearch.setHintTextColor(0xFF70757A);
        etHomeSearch.setTextColor(0xFF202124);
        etHomeSearch.setTextSize(16);
        etHomeSearch.setSingleLine(true);
        etHomeSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        etHomeSearch.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        etHomeSearch.setPadding(dp(12), 0, 0, 0);
        etHomeSearch.setOnEditorActionListener((v, actionId, event) -> handleSearch(etHomeSearch, actionId, event));
        card.addView(etHomeSearch, new LinearLayout.LayoutParams(0, MATCH, 1f));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(MATCH, dp(54));
        cardLp.setMargins(dp(24), 0, dp(24), dp(24));
        col.addView(card, cardLp);

        String[] names = {"YouTube", "Instagram", "Facebook", "X", "WhatsApp", "Google", "GitHub", "Wikipedia"};
        String[] urls = {"https://www.youtube.com", "https://www.instagram.com", "https://www.facebook.com",
                "https://x.com", "https://web.whatsapp.com", "https://www.google.com", "https://github.com",
                "https://www.wikipedia.org"};
        String[] domains = {"youtube.com", "instagram.com", "facebook.com", "x.com", "whatsapp.com",
                "google.com", "github.com", "wikipedia.org"};
        int[] rings = {0xFFFF1744, 0xFFE1306C, 0xFF1877F2, 0xFF212121, 0xFF25D366, 0xFF4285F4, 0xFF6E5494, 0xFF9E9E9E};

        for (int r = 0; r < 2; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int c = 0; c < 4; c++) {
                int i = r * 4 + c;
                row.addView(buildShortcut(names[i], urls[i], domains[i], rings[i]), new LinearLayout.LayoutParams(0, WRAP, 1f));
            }
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(MATCH, WRAP);
            rlp.setMargins(dp(16), 0, dp(16), 0);
            col.addView(row, rlp);
        }

        sv.addView(col, new ViewGroup.LayoutParams(MATCH, WRAP));
        return sv;
    }

    private View buildShortcut(String name, String url, String domain, int ring) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(0, dp(8), 0, dp(8));

        FrameLayout circle = new FrameLayout(this);
        circle.setBackground(ovalBg(Color.WHITE, ring, 2));
        circle.setElevation(dp(3));

        TextView letter = new TextView(this);
        letter.setText(name.substring(0, 1));
        letter.setTextColor(ring);
        letter.setTextSize(20);
        letter.setTypeface(Typeface.DEFAULT_BOLD);
        letter.setGravity(Gravity.CENTER);
        circle.addView(letter, new FrameLayout.LayoutParams(MATCH, MATCH));

        ImageView icon = new ImageView(this);
        circle.addView(icon, new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER));
        tile.addView(circle, new LinearLayout.LayoutParams(dp(58), dp(58)));

        TextView label = new TextView(this);
        label.setText(name);
        label.setTextColor(0xFF3C4043);
        label.setTextSize(11);
        label.setSingleLine(true);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(WRAP, WRAP);
        llp.topMargin = dp(6);
        tile.addView(label, llp);

        tile.setOnClickListener(v -> loadUrlInCurrentTab(url));
        loadFavicon(domain, icon, letter);
        return tile;
    }

    private FrameLayout buildTabSwitcher() {
        FrameLayout sw = new FrameLayout(this);
        sw.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFFEAF2FF, 0xFFFBEFFA}));
        sw.setClickable(true);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        sw.addView(col, new FrameLayout.LayoutParams(MATCH, MATCH));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(10), dp(8), dp(6));

        IconView plus = new IconView(this, IconView.PLUS, Color.WHITE);
        plus.setPadding(dp(11), dp(11), dp(11), dp(11));
        GradientDrawable pg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF2979FF, 0xFF7C4DFF});
        pg.setCornerRadius(dp(16));
        plus.setBackground(pg);
        plus.setOnClickListener(v -> {
            createNewTab("");
            hideTabSwitcher();
        });
        header.addView(plus, new LinearLayout.LayoutParams(dp(48), dp(44)));

        switcherTitle = new TextView(this);
        switcherTitle.setTextSize(18);
        switcherTitle.setTypeface(Typeface.DEFAULT_BOLD);
        switcherTitle.setTextColor(0xFF283593);
        switcherTitle.setPadding(dp(16), 0, 0, 0);
        header.addView(switcherTitle, new LinearLayout.LayoutParams(0, WRAP, 1f));

        header.addView(iconButton(IconView.CLOSEALL, 0xFFF4511E, v -> {
            closeAllTabs();
            hideTabSwitcher();
        }), new LinearLayout.LayoutParams(dp(44), dp(44)));
        header.addView(iconButton(IconView.CLOSE, v -> hideTabSwitcher()), new LinearLayout.LayoutParams(dp(44), dp(44)));
        col.addView(header, new LinearLayout.LayoutParams(MATCH, WRAP));

        ScrollView sv = new ScrollView(this);
        tabGrid = new LinearLayout(this);
        tabGrid.setOrientation(LinearLayout.VERTICAL);
        tabGrid.setPadding(dp(8), dp(6), dp(8), dp(24));
        sv.addView(tabGrid, new ViewGroup.LayoutParams(MATCH, WRAP));
        col.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        return sw;
    }

    private void refreshTabGrid() {
        tabGrid.removeAllViews();
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        switcherTitle.setText(tabs.size() + (tabs.size() == 1 ? " tab" : " tabs"));
        int active = tabManager.getActiveTabIdx();
        for (int i = 0; i < tabs.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < 2; j++) {
                int idx = i + j;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, WRAP, 1f);
                lp.setMargins(dp(6), dp(6), dp(6), dp(6));
                if (idx < tabs.size()) {
                    row.addView(buildTabCard(tabs.get(idx), idx, idx == active), lp);
                } else {
                    row.addView(new View(this), lp);
                }
            }
            tabGrid.addView(row, new LinearLayout.LayoutParams(MATCH, WRAP));
        }
    }

    private View buildTabCard(TabManager.TabItem tab, int index, boolean active) {
        boolean isHome = tab.url == null || tab.url.isEmpty();

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundBg(PASTELS[index % PASTELS.length], 20, BLUE, active ? 2.5f : 0));
        card.setElevation(dp(active ? 4 : 1));
        card.setPadding(dp(8), dp(6), dp(8), dp(8));
        card.setOnClickListener(v -> {
            tabManager.setActiveTabIdx(index);
            hideTabSwitcher();
            renderActiveTab();
        });

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        ImageView fav = new ImageView(this);
        fav.setBackground(ovalBg(0xFFFFFFFF, 0, 0));
        fav.setPadding(dp(2), dp(2), dp(2), dp(2));
        head.addView(fav, new LinearLayout.LayoutParams(dp(20), dp(20)));

        TextView t = new TextView(this);
        String shown = isHome ? "New tab" : (tab.title != null && !tab.title.isEmpty() ? tab.title : tab.url);
        t.setText(shown);
        t.setTextSize(13);
        t.setTextColor(0xFF202124);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        t.setPadding(dp(8), 0, dp(4), 0);
        head.addView(t, new LinearLayout.LayoutParams(0, WRAP, 1f));

        IconView close = new IconView(this, IconView.CLOSE, INK);
        close.setPadding(dp(4), dp(4), dp(4), dp(4));
        close.setOnClickListener(v -> closeTab(index));
        head.addView(close, new LinearLayout.LayoutParams(dp(26), dp(26)));
        card.addView(head, new LinearLayout.LayoutParams(MATCH, dp(32)));

        FrameLayout holder = new FrameLayout(this);
        holder.setBackgroundColor(isHome ? 0xFFE3F2FD : Color.WHITE);
        holder.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(14));
            }
        });
        holder.setClipToOutline(true);

        Bitmap thumb = thumbs.get(tab);
        if (!isHome && thumb != null) {
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageBitmap(thumb);
            holder.addView(iv, new FrameLayout.LayoutParams(MATCH, MATCH));
        } else {
            TextView ph = new TextView(this);
            ph.setGravity(Gravity.CENTER);
            ph.setTextSize(isHome ? 26 : 14);
            ph.setTextColor(isHome ? 0xFF7C4DFF : 0xFF5F6368);
            ph.setText(isHome ? "\u2744\nICE\u00B7cool" : hostOf(tab.url));
            holder.addView(ph, new FrameLayout.LayoutParams(MATCH, MATCH));
        }
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(MATCH, dp(200));
        hlp.topMargin = dp(4);
        card.addView(holder, hlp);

        if (!isHome) {
            String host = hostOf(tab.url);
            if (!host.isEmpty()) loadFavicon(host, fav, null);
        }
        return card;
    }

    // ------------------------------------------------------------------ tab switcher show/hide

    private void showTabSwitcher() {
        captureCurrentThumb();
        refreshTabGrid();
        tabSwitcher.setAlpha(0f);
        tabSwitcher.setScaleX(0.96f);
        tabSwitcher.setScaleY(0.96f);
        tabSwitcher.setVisibility(View.VISIBLE);
        tabSwitcher.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(160).start();
    }

    private void hideTabSwitcher() {
        if (tabSwitcher == null || tabSwitcher.getVisibility() != View.VISIBLE) return;
        tabSwitcher.animate().alpha(0f).setDuration(120).withEndAction(() -> tabSwitcher.setVisibility(View.GONE)).start();
    }

    // ------------------------------------------------------------------ menu

    private void showMenu(View anchor) {
        if (menuPopup != null && menuPopup.isShowing()) menuPopup.dismiss();

        FrameLayout wrapper = new FrameLayout(this);
        wrapper.setPadding(dp(10), dp(10), dp(10), dp(14));

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackground(roundBg(Color.WHITE, 20, 0, 0));
        menu.setElevation(dp(8));
        menu.setPadding(0, dp(8), 0, dp(8));
        wrapper.addView(menu, new FrameLayout.LayoutParams(MATCH, WRAP));

        menu.addView(menuRow(IconView.PLUS, "New tab", 0xFF2979FF, v -> {
            menuPopup.dismiss();
            createNewTab("");
        }));
        menu.addView(menuRow(IconView.CLOSEALL, "Close all tabs", 0xFFF4511E, v -> {
            menuPopup.dismiss();
            closeAllTabs();
        }));
        menu.addView(menuRow(IconView.DOWNLOAD, "Downloads", 0xFF0F9D58, v -> {
            menuPopup.dismiss();
            try {
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
            } catch (Exception e) {
                toast("Downloads app nahi mili");
            }
        }));
        menu.addView(menuRow(IconView.DOWNLOAD, "Download this page", 0xFF00ACC1, v -> {
            menuPopup.dismiss();
            TabManager.TabItem t = currentTab();
            if (t == null || t.url.isEmpty()) {
                toast("Pehle koi page kholo");
            } else {
                startDownload(t.url, t.webView.getSettings().getUserAgentString(), null, null);
            }
        }));
        menu.addView(menuRow(IconView.INSTALL, "Install & create shortcut", 0xFF7C4DFF, v -> {
            menuPopup.dismiss();
            startInstallFlow();
        }));

        // Zoom row
        LinearLayout zoom = new LinearLayout(this);
        zoom.setOrientation(LinearLayout.HORIZONTAL);
        zoom.setGravity(Gravity.CENTER_VERTICAL);
        zoom.setPadding(dp(16), 0, dp(10), 0);
        zoom.addView(new IconView(this, IconView.SEARCH, 0xFFFFA000), new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView zl = new TextView(this);
        zl.setText("Zoom");
        zl.setTextSize(16);
        zl.setTextColor(0xFF202124);
        zl.setPadding(dp(16), 0, 0, 0);
        zoom.addView(zl, new LinearLayout.LayoutParams(0, WRAP, 1f));
        final TextView zv = new TextView(this);
        zv.setText(currentZoomLevel + "%");
        zv.setTextSize(15);
        zv.setGravity(Gravity.CENTER);
        zv.setTextColor(0xFF202124);

        IconView minus = new IconView(this, IconView.MINUS, INK);
        minus.setPadding(dp(8), dp(8), dp(8), dp(8));
        minus.setBackground(ripple(true));
        minus.setOnClickListener(v -> {
            changeZoom(-25);
            zv.setText(currentZoomLevel + "%");
        });
        IconView plusZ = new IconView(this, IconView.PLUS, INK);
        plusZ.setPadding(dp(8), dp(8), dp(8), dp(8));
        plusZ.setBackground(ripple(true));
        plusZ.setOnClickListener(v -> {
            changeZoom(25);
            zv.setText(currentZoomLevel + "%");
        });
        zoom.addView(minus, new LinearLayout.LayoutParams(dp(36), dp(36)));
        zoom.addView(zv, new LinearLayout.LayoutParams(dp(52), WRAP));
        zoom.addView(plusZ, new LinearLayout.LayoutParams(dp(36), dp(36)));
        menu.addView(zoom, new LinearLayout.LayoutParams(MATCH, dp(48)));

        View div = new View(this);
        div.setBackgroundColor(0xFFE8EAED);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(MATCH, dp(1));
        dlp.setMargins(dp(16), dp(4), dp(16), dp(4));
        menu.addView(div, dlp);

        menu.addView(menuRow(IconView.TRASH, "Clear cookies & data", 0xFFD93025, v -> {
            menuPopup.dismiss();
            confirmClearData();
        }));

        menuPopup = new PopupWindow(wrapper, dp(290), WRAP, true);
        menuPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        menuPopup.showAsDropDown(anchor, anchor.getWidth() - dp(290) + dp(8), -anchor.getHeight() + dp(4));
    }

    private View menuRow(int icon, String label, int color, View.OnClickListener l) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(16), 0, dp(16), 0);
        r.setBackground(ripple(false));
        r.addView(new IconView(this, icon, color), new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(16);
        t.setTextColor(color == 0xFFD93025 ? color : 0xFF202124);
        t.setPadding(dp(16), 0, 0, 0);
        r.addView(t, new LinearLayout.LayoutParams(0, WRAP, 1f));
        r.setOnClickListener(l);
        r.setLayoutParams(new LinearLayout.LayoutParams(MATCH, dp(48)));
        return r;
    }

    private void changeZoom(int delta) {
        int z = currentZoomLevel + delta;
        if (z < 50) z = 50;
        if (z > 200) z = 200;
        currentZoomLevel = z;
        for (TabManager.TabItem t : tabManager.getTabs()) {
            if (t.webView != null) t.webView.getSettings().setTextZoom(currentZoomLevel);
        }
    }

    private void confirmClearData() {
        new AlertDialog.Builder(this)
                .setTitle("Clear all data?")
                .setMessage("Cookies, cache, history aur site data sab delete ho jayega.")
                .setPositiveButton("Clear", (d, w) -> clearAllDataAndCookies())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllDataAndCookies() {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        WebStorage.getInstance().deleteAllData();
        for (TabManager.TabItem tab : tabManager.getTabs()) {
            if (tab.webView != null) {
                tab.webView.clearCache(true);
                tab.webView.clearHistory();
                tab.webView.clearFormData();
            }
        }
        toast("All cookies & data cleared!");
    }

    // ------------------------------------------------------------------ tabs

    private TabManager.TabItem currentTab() {
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        if (tabs.isEmpty()) return null;
        int idx = tabManager.getActiveTabIdx();
        if (idx < 0 || idx >= tabs.size()) idx = tabs.size() - 1;
        return tabs.get(idx);
    }

    private TabManager.TabItem findTab(WebView wv) {
        for (TabManager.TabItem t : tabManager.getTabs()) {
            if (t.webView == wv) return t;
        }
        return null;
    }

    private boolean isActive(WebView wv) {
        if (appMode) return wv == appWebView;
        TabManager.TabItem t = currentTab();
        return t != null && t.webView == wv;
    }

    private void createNewTab(String url) {
        if (tabManager.getTabs().size() >= MAX_TABS) {
            toast("Max " + MAX_TABS + " tabs allowed");
            return;
        }
        WebView webView = new WebView(this);
        setupWebView(webView);

        TabManager.TabItem tab = new TabManager.TabItem(url, url.isEmpty() ? "New Tab" : url, webView);
        tabManager.addTab(tab);
        tabManager.setActiveTabIdx(tabManager.getTabs().size() - 1);

        if (!url.isEmpty()) webView.loadUrl(url);
        renderActiveTab();
    }

    private void destroyTab(TabManager.TabItem t) {
        thumbs.remove(t);
        try {
            if (t.webView != null) {
                ViewGroup p = (ViewGroup) t.webView.getParent();
                if (p != null) p.removeView(t.webView);
                t.webView.destroy();
            }
        } catch (Throwable ignored) {
        }
    }

    private void closeTab(int index) {
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        if (index < 0 || index >= tabs.size()) return;
        int activeBefore = tabManager.getActiveTabIdx();
        TabManager.TabItem t = tabs.get(index);
        destroyTab(t);
        tabManager.removeTab(index);

        if (tabManager.getTabs().isEmpty()) {
            createNewTab("");
            hideTabSwitcher();
            return;
        }
        int desired = index < activeBefore ? activeBefore - 1 : activeBefore;
        int max = tabManager.getTabs().size() - 1;
        if (desired > max) desired = max;
        if (desired < 0) desired = 0;
        tabManager.setActiveTabIdx(desired);
        renderActiveTab();
        refreshTabGrid();
    }

    private void closeAllTabs() {
        for (TabManager.TabItem t : new ArrayList<>(tabManager.getTabs())) destroyTab(t);
        thumbs.clear();
        tabManager.clearAllTabs();
        createNewTab("");
    }

    private void captureCurrentThumb() {
        for (int i = 0; i < webContainer.getChildCount(); i++) {
            View c = webContainer.getChildAt(i);
            if (c instanceof WebView && c.getWidth() > 0 && c.getHeight() > 0) {
                TabManager.TabItem t = findTab((WebView) c);
                if (t == null) continue;
                try {
                    Bitmap b = Bitmap.createBitmap(c.getWidth() / 2, c.getHeight() / 2, Bitmap.Config.RGB_565);
                    Canvas cv = new Canvas(b);
                    cv.scale(0.5f, 0.5f);
                    c.draw(cv);
                    thumbs.put(t, b);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private void renderActiveTab() {
        captureCurrentThumb();
        webContainer.removeAllViews();
        btnTabCounter.setText(String.valueOf(tabManager.getTabs().size()));
        showProgress(100);

        TabManager.TabItem tab = currentTab();
        if (tab == null || tab.url == null || tab.url.isEmpty()) {
            if (homeView.getParent() != null) ((ViewGroup) homeView.getParent()).removeView(homeView);
            homeView.setVisibility(View.VISIBLE);
            webContainer.addView(homeView, new FrameLayout.LayoutParams(MATCH, MATCH));
            etSearchUrl.setText("");
        } else {
            if (tab.webView.getParent() != null) ((ViewGroup) tab.webView.getParent()).removeView(tab.webView);
            webContainer.addView(tab.webView, new FrameLayout.LayoutParams(MATCH, MATCH));
            etSearchUrl.setText(tab.url);
        }
    }

    private void loadUrlInCurrentTab(String url) {
        TabManager.TabItem tab = currentTab();
        if (tab == null) {
            createNewTab(url);
            return;
        }
        tab.url = url;
        tab.title = url;
        thumbs.remove(tab);
        tab.webView.loadUrl(url);
        renderActiveTab();
    }

    private void navigateHome() {
        TabManager.TabItem tab = currentTab();
        if (tab != null) {
            tab.url = "";
            tab.title = "New Tab";
            thumbs.remove(tab);
        }
        renderActiveTab();
    }

    private void refreshCurrentTab() {
        TabManager.TabItem tab = currentTab();
        if (tab != null && tab.webView != null && !tab.url.isEmpty()) tab.webView.reload();
    }

    // ------------------------------------------------------------------ webview

    private String formatUrl(String input) {
        if (input.startsWith("http://") || input.startsWith("https://")) return input;
        if (input.contains(".") && !input.contains(" ")) return "https://" + input;
        return "https://www.google.com/search?q=" + Uri.encode(input);
    }

    private String hostOf(String url) {
        try {
            String h = Uri.parse(url).getHost();
            return h == null ? "" : h;
        } catch (Exception e) {
            return "";
        }
    }

    private void setupWebView(WebView webView) {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setTextZoom(currentZoomLevel);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(new PwaBridge(), "IceBridge");
        webView.setDownloadListener((url, ua, cd, mime, len) -> startDownload(url, ua, cd, mime));

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://") || url.startsWith("https://")
                        || url.startsWith("file:") || url.startsWith("about:")) {
                    return false;
                }
                try {
                    startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME));
                } catch (Exception ignored) {
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                TabManager.TabItem t = findTab(view);
                if (t != null && !t.url.isEmpty()) t.url = url;
                if (isActive(view)) {
                    showProgress(5);
                    if (t != null && !t.url.isEmpty() && etSearchUrl != null && !etSearchUrl.hasFocus()) {
                        etSearchUrl.setText(url);
                    }
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                TabManager.TabItem t = findTab(view);
                if (t != null && !t.url.isEmpty()) t.url = url;
                if (isActive(view)) {
                    showProgress(100);
                    if (t != null && !t.url.isEmpty() && etSearchUrl != null && !etSearchUrl.hasFocus()) {
                        etSearchUrl.setText(url);
                    }
                }
                CookieManager.getInstance().flush();
                if (appMode && view == appWebView) view.evaluateJavascript(THEME_JS, null);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (isActive(view)) showProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                TabManager.TabItem t = findTab(view);
                if (t != null && title != null && !title.isEmpty()) t.title = title;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                root.setVisibility(View.GONE);
                ViewGroup decor = (ViewGroup) getWindow().getDecorView();
                decor.addView(customView, new ViewGroup.LayoutParams(MATCH, MATCH));
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }
        });
    }

    private void hideCustomView() {
        if (customView == null) return;
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        decor.removeView(customView);
        customView = null;
        root.setVisibility(View.VISIBLE);
        if (customViewCallback != null) customViewCallback.onCustomViewHidden();
        customViewCallback = null;
    }

    private void startDownload(String url, String ua, String cd, String mime) {
        try {
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            if (mime != null && !mime.isEmpty()) r.setMimeType(mime);
            String cookies = CookieManager.getInstance().getCookie(url);
            if (cookies != null) r.addRequestHeader("cookie", cookies);
            if (ua != null) r.addRequestHeader("User-Agent", ua);
            String name = URLUtil.guessFileName(url, cd, mime);
            r.setTitle(name);
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(r);
            toast("Downloading: " + name);
        } catch (Exception e) {
            toast("Download nahi ho paya");
        }
    }

    private void loadFavicon(final String domain, final ImageView iv, final View fallback) {
        Bitmap cached = faviconCache.get(domain);
        if (cached != null) {
            iv.setImageBitmap(cached);
            if (fallback != null) fallback.setVisibility(View.GONE);
            return;
        }
        new Thread(() -> {
            final Bitmap fb = downloadBitmap("https://www.google.com/s2/favicons?domain=" + domain + "&sz=128");
            if (fb != null) {
                faviconCache.put(domain, fb);
                runOnUiThread(() -> {
                    iv.setImageBitmap(fb);
                    if (fallback != null) fallback.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    // ------------------------------------------------------------------ back

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideCustomView();
            return;
        }
        if (appMode) {
            if (appWebView != null && appWebView.canGoBack()) {
                appWebView.goBack();
                return;
            }
            super.onBackPressed();
            return;
        }
        if (tabSwitcher.getVisibility() == View.VISIBLE) {
            hideTabSwitcher();
            return;
        }
        TabManager.TabItem t = currentTab();
        if (t != null && !t.url.isEmpty() && t.webView != null && t.webView.canGoBack()) {
            t.webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    // ------------------------------------------------------------------ custom views

    /** Simple vector-style icons drawn with Canvas (no drawable files needed). */
    static class IconView extends View {
        static final int HOME = 0, REFRESH = 1, DOTS = 2, PLUS = 3, CLOSE = 4, MINUS = 5,
                DOWNLOAD = 6, TRASH = 7, SEARCH = 8, CLOSEALL = 9, INSTALL = 10;
        private final int type;
        private final int color;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        IconView(Context c, int type, int color) {
            super(c);
            this.type = type;
            this.color = color;
        }

        @Override
        protected void onDraw(Canvas c) {
            float size = Math.min(getWidth() - getPaddingLeft() - getPaddingRight(),
                    getHeight() - getPaddingTop() - getPaddingBottom());
            if (size <= 0) return;
            c.translate(getPaddingLeft(), getPaddingTop());
            c.scale(size / 24f, size / 24f);

            p.setColor(color);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2f);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            Path path = new Path();

            switch (type) {
                case HOME:
                    path.moveTo(3, 11);
                    path.lineTo(12, 3);
                    path.lineTo(21, 11);
                    c.drawPath(path, p);
                    path.reset();
                    path.moveTo(5, 10);
                    path.lineTo(5, 20);
                    path.lineTo(10, 20);
                    path.lineTo(10, 14);
                    path.lineTo(14, 14);
                    path.lineTo(14, 20);
                    path.lineTo(19, 20);
                    path.lineTo(19, 10);
                    c.drawPath(path, p);
                    break;
                case REFRESH:
                    c.drawArc(new RectF(5, 5, 19, 19), 20, 290, false, p);
                    p.setStyle(Paint.Style.FILL);
                    path.moveTo(20.5f, 3f);
                    path.lineTo(20.5f, 10f);
                    path.lineTo(13.5f, 10f);
                    path.close();
                    c.drawPath(path, p);
                    break;
                case DOTS:
                    p.setStyle(Paint.Style.FILL);
                    c.drawCircle(12, 5, 2f, p);
                    c.drawCircle(12, 12, 2f, p);
                    c.drawCircle(12, 19, 2f, p);
                    break;
                case PLUS:
                    c.drawLine(12, 5, 12, 19, p);
                    c.drawLine(5, 12, 19, 12, p);
                    break;
                case MINUS:
                    c.drawLine(5, 12, 19, 12, p);
                    break;
                case CLOSE:
                    c.drawLine(6, 6, 18, 18, p);
                    c.drawLine(18, 6, 6, 18, p);
                    break;
                case CLOSEALL:
                    c.drawRoundRect(new RectF(3, 3, 21, 21), 4, 4, p);
                    c.drawLine(8.5f, 8.5f, 15.5f, 15.5f, p);
                    c.drawLine(15.5f, 8.5f, 8.5f, 15.5f, p);
                    break;
                case DOWNLOAD:
                    c.drawLine(12, 4, 12, 15, p);
                    path.moveTo(7, 10);
                    path.lineTo(12, 15);
                    path.lineTo(17, 10);
                    c.drawPath(path, p);
                    c.drawLine(5, 20, 19, 20, p);
                    break;
                case INSTALL:
                    c.drawRoundRect(new RectF(5.5f, 2.5f, 18.5f, 21.5f), 3, 3, p);
                    c.drawLine(12, 7, 12, 14, p);
                    path.moveTo(8.5f, 11);
                    path.lineTo(12, 14.5f);
                    path.lineTo(15.5f, 11);
                    c.drawPath(path, p);
                    c.drawLine(10.5f, 18.5f, 13.5f, 18.5f, p);
                    break;
                case TRASH:
                    c.drawLine(4, 7, 20, 7, p);
                    path.moveTo(6, 7);
                    path.lineTo(6, 20);
                    path.lineTo(18, 20);
                    path.lineTo(18, 7);
                    c.drawPath(path, p);
                    path.reset();
                    path.moveTo(9, 7);
                    path.lineTo(9, 4);
                    path.lineTo(15, 4);
                    path.lineTo(15, 7);
                    c.drawPath(path, p);
                    c.drawLine(10, 11, 10, 16, p);
                    c.drawLine(14, 11, 14, 16, p);
                    break;
                case SEARCH:
                default:
                    c.drawCircle(10.5f, 10.5f, 6.5f, p);
                    c.drawLine(15.5f, 15.5f, 21, 21, p);
                    break;
            }
        }
    }

    /** Colourful icy illustration for the home screen. */
    static class IceScene extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float[] snow = {0.08f, 0.12f, 0.25f, 0.2f, 0.4f, 0.08f, 0.55f, 0.22f, 0.7f, 0.1f,
                0.9f, 0.35f, 0.05f, 0.45f, 0.35f, 0.38f, 0.62f, 0.45f, 0.95f, 0.12f, 0.18f, 0.3f};

        IceScene(Context c) {
            super(c);
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight(), u = h / 100f;
            final int BG = 0xFFF3F6FF;

            p.setShader(new LinearGradient(0, 0, 0, h, new int[]{0xFF8FE3FF, 0xFFC4B5FF, 0xFFFFD9EC},
                    new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);

            // sun
            p.setColor(0x66FFF59D);
            c.drawCircle(w * 0.8f, h * 0.28f, 17 * u, p);
            p.setColor(0xFFFFE57F);
            c.drawCircle(w * 0.8f, h * 0.28f, 10 * u, p);

            // back mountains
            Path m = new Path();
            m.moveTo(0, h * 0.85f);
            m.lineTo(w * 0.18f, h * 0.42f);
            m.lineTo(w * 0.36f, h * 0.72f);
            m.lineTo(w * 0.55f, h * 0.34f);
            m.lineTo(w * 0.8f, h * 0.74f);
            m.lineTo(w * 0.92f, h * 0.52f);
            m.lineTo(w, h * 0.7f);
            m.lineTo(w, h * 0.85f);
            m.close();
            p.setColor(0xFF7FB2FF);
            c.drawPath(m, p);

            // snow caps
            p.setColor(Color.WHITE);
            Path cap = new Path();
            cap.moveTo(w * 0.18f, h * 0.42f);
            cap.lineTo(w * 0.14f, h * 0.53f);
            cap.lineTo(w * 0.22f, h * 0.53f);
            cap.close();
            cap.moveTo(w * 0.55f, h * 0.34f);
            cap.lineTo(w * 0.49f, h * 0.48f);
            cap.lineTo(w * 0.61f, h * 0.48f);
            cap.close();
            c.drawPath(cap, p);

            // ground (same colour as page background so it blends)
            p.setColor(BG);
            c.drawRect(0, h * 0.84f, w, h, p);
            c.drawOval(new RectF(-w * 0.1f, h * 0.78f, w * 0.65f, h * 1.15f), p);
            c.drawOval(new RectF(w * 0.45f, h * 0.8f, w * 1.1f, h * 1.15f), p);

            // iceberg
            Path ice = new Path();
            ice.moveTo(w * 0.1f, h * 0.9f);
            ice.lineTo(w * 0.2f, h * 0.55f);
            ice.lineTo(w * 0.28f, h * 0.64f);
            ice.lineTo(w * 0.36f, h * 0.48f);
            ice.lineTo(w * 0.47f, h * 0.9f);
            ice.close();
            p.setColor(0xFFB2EBF2);
            c.drawPath(ice, p);
            Path face = new Path();
            face.moveTo(w * 0.36f, h * 0.48f);
            face.lineTo(w * 0.47f, h * 0.9f);
            face.lineTo(w * 0.31f, h * 0.9f);
            face.close();
            p.setColor(0xFF4DD0E1);
            c.drawPath(face, p);

            // penguin
            float cx = w * 0.7f, by = h * 0.9f;
            p.setColor(0xFF263238);
            c.drawOval(new RectF(cx - 10 * u, by - 36 * u, cx + 10 * u, by), p);
            c.drawOval(new RectF(cx - 14 * u, by - 26 * u, cx - 9 * u, by - 12 * u), p);
            c.drawOval(new RectF(cx + 9 * u, by - 26 * u, cx + 14 * u, by - 12 * u), p);
            p.setColor(Color.WHITE);
            c.drawOval(new RectF(cx - 6.5f * u, by - 26 * u, cx + 6.5f * u, by - 2 * u), p);
            p.setColor(0xFFFF4081);
            c.drawRoundRect(new RectF(cx - 9 * u, by - 23 * u, cx + 9 * u, by - 20 * u), u, u, p);
            p.setColor(Color.WHITE);
            c.drawCircle(cx - 3.2f * u, by - 30 * u, 1.8f * u, p);
            c.drawCircle(cx + 3.2f * u, by - 30 * u, 1.8f * u, p);
            p.setColor(0xFF263238);
            c.drawCircle(cx - 3.2f * u, by - 30 * u, 0.9f * u, p);
            c.drawCircle(cx + 3.2f * u, by - 30 * u, 0.9f * u, p);
            p.setColor(0xFFFFA726);
            Path beak = new Path();
            beak.moveTo(cx - 2.4f * u, by - 27 * u);
            beak.lineTo(cx + 2.4f * u, by - 27 * u);
            beak.lineTo(cx, by - 24 * u);
            beak.close();
            c.drawPath(beak, p);
            c.drawOval(new RectF(cx - 8 * u, by - 1.5f * u, cx - 1 * u, by + 1.5f * u), p);
            c.drawOval(new RectF(cx + 1 * u, by - 1.5f * u, cx + 8 * u, by + 1.5f * u), p);

            // snow
            p.setColor(0xFFFFFFFF);
            for (int i = 0; i + 1 < snow.length; i += 2) {
                c.drawCircle(snow[i] * w, snow[i + 1] * h, (1 + (i % 3)) * u * 0.7f, p);
            }
        }
    }
}
