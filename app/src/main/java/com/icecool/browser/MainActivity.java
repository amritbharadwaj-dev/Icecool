package com.icecool.browser;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
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
import android.widget.HorizontalScrollView;
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

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.content.ContentValues;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Base64;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.widget.CompoundButton;
import android.widget.Switch;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int BLUE = 0xFF2979FF;
    private int INK = 0xFF3C4043;
    private static final int MAX_TABS = 25;
    private static final int[] ZOOM_STEPS = {50, 67, 75, 80, 90, 100, 110, 125, 150, 175, 200, 250, 300};

    private static final String EXTRA_MODE = "ice_mode";
    private static final String EXTRA_NAME = "ice_name";
    private static final String EXTRA_THEME = "ice_theme";
    private static final String MODE_APP = "app";
    private static final String MODE_WEB = "web";

    private static final int[] PASTELS = {0xFFE3F2FD, 0xFFFCE4EC, 0xFFE8F5E9, 0xFFFFF3E0, 0xFFF3E5F5, 0xFFE0F7FA};

    // ---- 5 Ice Cool home themes (different shapes + colours) ----
    private static final String[] THEME_NAMES = {"Glacier", "Sunset Peaks", "Aurora Night", "Mint Forest", "Ocean Breeze"};
    private static final int[][] THEME_ICE = {{0xFF00E5FF, 0xFF2979FF}, {0xFFFF9100, 0xFFFF4081},
            {0xFF69F0AE, 0xFF00B8D4}, {0xFF00C853, 0xFF00BFA5}, {0xFF40C4FF, 0xFF304FFE}};
    private static final int[][] THEME_COOL = {{0xFF7C4DFF, 0xFFFF4081, 0xFFFF9100}, {0xFFFF5252, 0xFFFF4081, 0xFF7C4DFF},
            {0xFFB388FF, 0xFFFF80AB, 0xFF69F0AE}, {0xFF00BFA5, 0xFF76FF03, 0xFFFFD600}, {0xFF304FFE, 0xFF00B8D4, 0xFF64FFDA}};
    private static final int[][] THEME_BG = {{0xFFF3F6FF, 0xFFFFF1F7}, {0xFFFFF4EA, 0xFFFFEAF2},
            {0xFFEAF1FF, 0xFFF3EAFF}, {0xFFEFFAF3, 0xFFF4FFE9}, {0xFFEAF6FF, 0xFFE6F0FF}};

    private static final String[] DEF_NAMES = {"YouTube", "Instagram", "Facebook", "X", "WhatsApp", "Telegram",
            "Snapchat", "Reddit", "LinkedIn", "Pinterest", "Amazon", "Flipkart", "Google", "GitHub", "Wikipedia", "Netflix"};
    private static final String[] DEF_URLS = {"https://www.youtube.com", "https://www.instagram.com",
            "https://www.facebook.com", "https://x.com", "https://web.whatsapp.com", "https://web.telegram.org",
            "https://www.snapchat.com", "https://www.reddit.com", "https://www.linkedin.com",
            "https://www.pinterest.com", "https://www.amazon.in", "https://www.flipkart.com",
            "https://www.google.com", "https://github.com", "https://www.wikipedia.org", "https://www.netflix.com"};
    private static final String[] DEF_DOMAINS = {"youtube.com", "instagram.com", "facebook.com", "x.com",
            "whatsapp.com", "telegram.org", "snapchat.com", "reddit.com", "linkedin.com", "pinterest.com",
            "amazon.in", "flipkart.com", "google.com", "github.com", "wikipedia.org", "netflix.com"};
    private static final int[] DEF_RINGS = {0xFFFF1744, 0xFFE1306C, 0xFF1877F2, 0xFF212121, 0xFF25D366, 0xFF229ED9,
            0xFFFFC400, 0xFFFF4500, 0xFF0A66C2, 0xFFE60023, 0xFFFF9900, 0xFF2874F0, 0xFF4285F4, 0xFF6E5494,
            0xFF9E9E9E, 0xFFE50914};

    // ---- ad blocker data ----
    private static final Set<String> AD_HOSTS = new HashSet<>(Arrays.asList(
            "doubleclick.net", "googlesyndication.com", "googleadservices.com", "adservice.google.com",
            "2mdn.net", "adnxs.com", "adsrvr.org", "advertising.com", "adform.net", "adcolony.com",
            "applovin.com", "unityads.unity3d.com", "mopub.com", "taboola.com", "outbrain.com", "criteo.com",
            "criteo.net", "rubiconproject.com", "pubmatic.com", "openx.net", "casalemedia.com", "indexww.com",
            "sovrn.com", "smartadserver.com", "teads.tv", "adroll.com", "amazon-adsystem.com", "media.net",
            "bidswitch.net", "contextweb.com", "yieldmo.com", "moatads.com", "adsafeprotected.com",
            "scorecardresearch.com", "quantserve.com", "zedo.com", "revcontent.com", "mgid.com",
            "propellerads.com", "popads.net", "popcash.net", "exoclick.com", "juicyads.com", "trafficjunky.com",
            "adsterra.com", "clickadu.com", "hilltopads.net", "a-ads.com", "inmobi.com", "vungle.com",
            "chartboost.com", "ironsrc.com", "supersonicads.com", "startappservice.com", "adcash.com",
            "adtng.com", "adskeeper.com", "adspyglass.com", "ad-maven.com", "admaven.com", "monetag.com",
            "pushails.com", "richpush.co", "evadav.com", "onclickads.net", "onclickmega.com", "tsyndicate.com",
            "trafficfactory.biz", "realsrv.com", "juicyads.com", "exosrv.com", "syndication.exoclick.com",
            "ads.yahoo.com", "adserver.yahoo.com", "gemini.yahoo.com", "analytics.yahoo.com",
            "ads.twitter.com", "ads-api.twitter.com", "static.ads-twitter.com", "ads.linkedin.com",
            "px.ads.linkedin.com", "ads.tiktok.com", "analytics.tiktok.com", "ads.pinterest.com",
            "ct.pinterest.com", "ads.reddit.com", "ads.youtube.com", "pagead2.googlesyndication.com",
            "tpc.googlesyndication.com", "securepubads.g.doubleclick.net", "stats.g.doubleclick.net",
            "googletagservices.com", "google-analytics.com", "adsymptotic.com", "serving-sys.com",
            "bluekai.com", "demdex.net", "krxd.net", "everesttech.net", "tapad.com", "turn.com",
            "rlcdn.com", "liadm.com", "lijit.com", "33across.com", "gumgum.com", "sharethrough.com",
            "triplelift.com", "spotxchange.com", "springserve.com", "freewheel.tv", "tremorhub.com",
            "adition.com", "improvedigital.com", "undertone.com", "kargo.com", "nativo.com", "zemanta.com",
            "mathtag.com", "adblade.com", "plugrush.com", "bidvertiser.com", "yllix.com", "ero-advertising.com",
            "hotjar.com", "mouseflow.com", "crazyegg.com", "branch.io.invalid", "adjust.io.invalid",
            "adgebra.in", "colombiaonline.com", "vserv.mobi", "inmobi.net", "vdo.ai", "connatix.com",
            "primis.tech", "minute.ly", "adsco.re", "clickaine.com", "popunderjs.com", "propu.sh"));

    private static final String[] AD_PATTERNS = {"/pagead/", "/adsbygoogle", "googleads.g.doubleclick",
            "/adserver/", "/ad-server", "youtube.com/api/stats/ads", "youtube.com/ptracking",
            "youtube.com/pagead", "/get_midroll_info", "adformat=", "/ads/js/", "/adframe"};

    private static final String AD_CSS =
            "ins.adsbygoogle,.adsbygoogle,iframe[src*=\"doubleclick.net\"],iframe[src*=\"googlesyndication\"],"
                    + "iframe[id^=\"google_ads\"],iframe[id^=\"aswift\"],div[id^=\"google_ads\"],div[id^=\"div-gpt-ad\"],"
                    + "div[id*=\"google_ads_iframe\"],[data-ad-slot],[data-ad-client],[data-google-query-id],"
                    + "a[href*=\"doubleclick.net\"],a[href*=\"googleadservices\"],.ad-banner,.ad-container,.ad-slot,"
                    + ".ad-wrapper,.ad-unit,.ad-box,.adbox,.advert,.advertisement,.advertise,.banner-ad,.banner_ad,"
                    + ".sponsored-ad,.sticky-ad,.top-ad,.bottom-ad,.google-ad,.gpt-ad,.dfp-ad,.taboola,"
                    + ".trc_related_container,[id^=\"taboola-\"],.outbrain,.OUTBRAIN,[id^=\"outbrain\"],#ad,#ads,"
                    + "#adbanner,#ad-banner,#ad-container,#adContainer,.adContainer,[aria-label=\"Advertisement\"],"
                    + "[aria-label=\"advertisement\"],[class*=\"adsbox\"],ytd-display-ad-renderer,ytd-ad-slot-renderer,"
                    + "ytd-in-feed-ad-layout-renderer,ytd-promoted-sparkles-web-renderer,ytd-promoted-video-renderer,"
                    + "ytd-banner-promo-renderer,ytd-statement-banner-renderer,ytd-player-legacy-desktop-watch-ads-renderer,"
                    + "#masthead-ad,#player-ads,.ytp-ad-overlay-container,.ytp-ad-module,.video-ads,.ytp-ad-image-overlay,"
                    + "ytm-promoted-sparkles-web-renderer,ytm-companion-ad-renderer,ad-slot-renderer";

    // Injected into every page while the ad blocker is on (hides ad boxes + skips YouTube ads)
    private static final String AD_JS =
            "(function(){try{"
                    + "if(!document.getElementById('__ice_ab')){var st=document.createElement('style');st.id='__ice_ab';"
                    + "st.textContent='" + AD_CSS + "{display:none!important;visibility:hidden!important;height:0!important;"
                    + "min-height:0!important;max-height:0!important;margin:0!important;padding:0!important;}';"
                    + "(document.head||document.documentElement).appendChild(st);}"
                    + "window.adsbygoogle=window.adsbygoogle||[];try{window.adsbygoogle.push=function(){};}catch(e){}"
                    + "if(/(^|\\.)youtube\\.com$/.test(location.hostname)&&!window.__iceYT){window.__iceYT=1;"
                    + "try{var jp=JSON.parse;JSON.parse=function(t,r){var o=jp.call(this,t,r);try{if(o&&typeof o==='object'){"
                    + "if(o.adPlacements)o.adPlacements=[];if(o.playerAds)o.playerAds=false;if(o.adSlots)o.adSlots=[];"
                    + "if(o.playerResponse&&o.playerResponse.adPlacements)o.playerResponse.adPlacements=[];}}catch(e){}return o;};}catch(e){}"
                    + "var muted=false;setInterval(function(){try{var p=document.querySelector('.html5-video-player');"
                    + "var v=document.querySelector('video.html5-main-video')||document.querySelector('video');"
                    + "if(p&&p.classList.contains('ad-showing')){if(v){if(!muted){muted=true;v.dataset.iceWasMuted=v.muted?'1':'0';}"
                    + "v.muted=true;try{v.playbackRate=16;}catch(e){}if(isFinite(v.duration)&&v.duration>0){v.currentTime=Math.max(0,v.duration-0.05);}}"
                    + "var b=document.querySelector('.ytp-ad-skip-button,.ytp-ad-skip-button-modern,.ytp-skip-ad-button,.ytp-ad-skip-button-container button');"
                    + "if(b)b.click();}else if(muted){muted=false;if(v){v.playbackRate=1;if(v.dataset.iceWasMuted==='0')v.muted=false;}}"
                    + "var c=document.querySelector('.ytp-ad-overlay-close-button');if(c)c.click();}catch(e){}},250);}"
                    + "}catch(e){}})();";

    private boolean isAdUrl(Uri u) {
        if (u == null) return false;
        String host = u.getHost();
        if (host == null) return false;
        String h = host.toLowerCase(Locale.ROOT);
        while (h.contains(".")) {
            if (AD_HOSTS.contains(h)) return true;
            h = h.substring(h.indexOf('.') + 1);
        }
        String full = u.toString().toLowerCase(Locale.ROOT);
        for (String p : AD_PATTERNS) {
            if (full.contains(p)) return true;
        }
        return false;
    }

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
                    + "info.manifest=true;info.name=m.short_name||m.name||'';"
                    + "if(m.theme_color&&!info.theme)info.theme=m.theme_color;if(m.background_color)info.bg=m.background_color;"
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
    private LinearLayout tabGrid, tabStrip;
    private HorizontalScrollView tabStripScroll;
    private View homeView;
    private EditText etSearchUrl, etHomeSearch;
    private TextView btnTabCounter, switcherTitle;
    private ProgressBar progressBar;
    private View accentLine;
    private TabManager tabManager;
    private PopupWindow menuPopup;
    private SharedPreferences prefs;
    private boolean privateMode = false;
    private volatile boolean adBlockEnabled = false;
    private boolean darkMode = false;
    private LinearLayout findBar;
    private EditText etFind;
    private TextView findCount;
    private LinearLayout urlPill;
    private IconView urlStar;
    private PopupWindow urlPopup;
    private Dialog appearanceDialog;
    private boolean homeImgMode = false;
    private Bitmap homeBitmap;
    private static final int REQ_IMAGE = 4711;
    private final Set<TabManager.TabItem> readingTabs = new HashSet<>();
    private final AtomicInteger blockedCount = new AtomicInteger();
    private FrameLayout panelOverlay;
    private LinearLayout panelList;
    private TextView panelTitle;
    private int panelMode = 0; // 1 = downloads, 2 = bookmarks, 3 = about
    private final List<DlItem> downloads = new ArrayList<>();
    private final Map<DlItem, TextView> dlSubViews = new HashMap<>();
    private final Map<DlItem, ProgressBar> dlBars = new HashMap<>();
    private final Map<String, DlItem> blobTokens = Collections.synchronizedMap(new HashMap<String, DlItem>());
    private LinearLayout shortcutsBox;
    private final Map<String, Integer> privateZoom = new HashMap<>();
    // theme colours (light by default, dark in private window)
    private int cText = 0xFF202124, cStrip = 0xFFDCE1EC, cBar = Color.WHITE, cPill = 0xFFEEF1FF,
            cActive = Color.WHITE, cInactive = 0xFF5F6368, cMenuBg = Color.WHITE,
            cSwA = 0xFFEAF2FF, cSwB = 0xFFFBEFFA, cTitle = 0xFF283593, cCard = 0;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private boolean appMode = false;
    private WebView appWebView;
    private String pendingToken = null;
    private String mobileUA = null;
    private String desktopUA = null;
    private boolean stripRefreshPending = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Map<TabManager.TabItem, Bitmap> thumbs = new HashMap<>();
    private final Map<String, Bitmap> faviconCache = Collections.synchronizedMap(new HashMap<String, Bitmap>());
    private final Set<TabManager.TabItem> desktopTabs = new HashSet<>();
    private final Set<TabManager.TabItem> popupTabs = new HashSet<>();
    private final Set<TabManager.TabItem> lazyTabs = new HashSet<>();

    // ------------------------------------------------------------------ lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root);
        tabManager = TabManager.getInstance();
        prefs = getSharedPreferences("icecool", MODE_PRIVATE);
        privateMode = isPrivateMode();
        if (privateMode) {
            // blocks screenshots + screen recording + hides content in recents
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            initPrivateTheme();
            try {
                setTaskDescription(new ActivityManager.TaskDescription("Private window"));
            } catch (Throwable ignored) {
            }
        }
        computeUserAgents();

        Window w = getWindow();
        w.setStatusBarColor(Color.WHITE);
        w.setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            w.getDecorView().setSystemUiVisibility(
                    w.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        adBlockEnabled = privateMode || prefs.getBoolean("adblock", false);
        darkMode = !privateMode && prefs.getBoolean("dark", false);
        if (darkMode) applyPalette(true);
        if (!privateMode) loadDownloads();
        if (privateMode || darkMode) applyBarColor(cBar);
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
        if (!privateMode && tabManager.getTabs().isEmpty()) restoreTabs();
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
    protected void onStop() {
        super.onStop();
        if (!appMode && !privateMode) saveTabs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (privateMode && isFinishing()) {
            wipePrivateData();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        if (appMode && appWebView != null) {
            try {
                root.removeView(appWebView);
                appWebView.destroy();
            } catch (Throwable ignored) {
            }
        }
    }

    protected boolean isPrivateMode() {
        return false;
    }

    private void initPrivateTheme() {
        applyPalette(true);
    }

    private void applyPalette(boolean dark) {
        if (dark) {
            INK = 0xFFE8EAED;
            cText = 0xFFE8EAED;
            cStrip = 0xFF202124;
            cBar = 0xFF35363A;
            cPill = 0xFF202124;
            cActive = 0xFF35363A;
            cInactive = 0xFF9AA0A6;
            cMenuBg = 0xFF35363A;
            cSwA = 0xFF202124;
            cSwB = 0xFF2B2C2F;
            cTitle = 0xFFE8EAED;
            cCard = 0xFF3C4043;
        } else {
            INK = 0xFF3C4043;
            cText = 0xFF202124;
            cStrip = 0xFFDCE1EC;
            cBar = Color.WHITE;
            cPill = 0xFFEEF1FF;
            cActive = Color.WHITE;
            cInactive = 0xFF5F6368;
            cMenuBg = Color.WHITE;
            cSwA = 0xFFEAF2FF;
            cSwB = 0xFFFBEFFA;
            cTitle = 0xFF283593;
            cCard = 0;
        }
    }

    private void openPrivateWindow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            toast("Private window ke liye Android 9 ya usse naya chahiye");
            return;
        }
        Intent i = new Intent(this, PrivateActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void wipePrivateData() {
        try {
            CookieManager cm = CookieManager.getInstance();
            cm.removeAllCookies(null);
            cm.flush();
        } catch (Throwable ignored) {
        }
        try {
            WebStorage.getInstance().deleteAllData();
        } catch (Throwable ignored) {
        }
        try {
            for (TabManager.TabItem t : tabManager.getTabs()) {
                if (t.webView != null) {
                    t.webView.clearCache(true);
                    t.webView.clearHistory();
                    t.webView.clearFormData();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void exitPrivateWindow() {
        wipePrivateData();
        for (TabManager.TabItem t : new ArrayList<>(tabManager.getTabs())) destroyTab(t);
        tabManager.clearAllTabs();
        finishAndRemoveTask();
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

    private void computeUserAgents() {
        String def = WebSettings.getDefaultUserAgent(this);
        mobileUA = def.replace("; wv", "").replace(" Version/4.0", "");
        Matcher m = Pattern.compile("Chrome/([\\d.]+)").matcher(def);
        String ver = m.find() ? m.group(1) : "120.0.0.0";
        desktopUA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + ver + " Safari/537.36";
    }

    // ------------------------------------------------------------------ save / restore tabs (tabs never vanish)

    private void saveTabs() {
        try {
            JSONArray arr = new JSONArray();
            for (TabManager.TabItem t : tabManager.getTabs()) {
                String u = t.url == null ? "" : t.url;
                if (u.startsWith("about:")) u = "";
                arr.put(u);
            }
            prefs.edit().putString("tabs", arr.toString()).putInt("active", tabManager.getActiveTabIdx()).apply();
        } catch (Exception ignored) {
        }
    }

    private void restoreTabs() {
        try {
            String js = prefs.getString("tabs", null);
            if (js == null) return;
            JSONArray arr = new JSONArray(js);
            if (arr.length() == 0) return;
            for (int i = 0; i < arr.length() && i < MAX_TABS; i++) {
                String u = arr.optString(i, "");
                WebView wv = new WebView(this);
                setupWebView(wv);
                TabManager.TabItem tab = new TabManager.TabItem(u, u.isEmpty() ? "New Tab" : u, wv);
                tabManager.addTab(tab);
                if (!u.isEmpty()) lazyTabs.add(tab); // loads only when you open it (fast start)
            }
            int active = prefs.getInt("active", 0);
            int max = tabManager.getTabs().size() - 1;
            tabManager.setActiveTabIdx(Math.max(0, Math.min(active, max)));
        } catch (Exception ignored) {
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

    // ------------------------------------------------------------------ install / pin shortcuts

    /** Called from JS inside the page. Only accepted while a check is pending (token must match). */
    public class PwaBridge {
        @JavascriptInterface
        public void onBlobData(final String token, final String dataUrl) {
            final DlItem d = blobTokens.remove(token);
            if (d == null) return;
            d.payload = dataUrl;
            new Thread(() -> downloadWorker(d)).start();
        }

        @JavascriptInterface
        public void onBlobFail(final String token) {
            final DlItem d = blobTokens.remove(token);
            if (d == null) return;
            runOnUiThread(() -> finishDownload(d, false));
        }

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
                ? "Ye site PWA hai (manifest + service worker mila). Home screen icon se ye full screen me, bina URL bar ke, app jaisi khulegi."
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

    private void pinTab(TabManager.TabItem tab) {
        if (tab == null || tab.url == null || tab.url.isEmpty()) {
            toast("Ye khali tab hai, pehle koi site kholo");
            return;
        }
        String host = hostOf(tab.url);
        String name = (tab.title != null && !tab.title.isEmpty() && !tab.title.equals(tab.url)) ? tab.title : host;
        createShortcut(name, tab.url, "", false, "", host, false, "");
    }

    private void createShortcut(final String name, final String url, final String iconUrl,
                                final boolean app, final String theme, final String host,
                                final boolean maskable, final String bg) {
        toast("Shortcut ban raha hai...");
        new Thread(() -> {
            Bitmap src = iconUrl.isEmpty() ? null : downloadBitmap(iconUrl);
            if (src == null && !host.isEmpty()) {
                src = downloadBitmap("https://www.google.com/s2/favicons?domain=" + host + "&sz=256");
            }
            final Bitmap icon = composeRound(src, maskable, bg, name);
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
                    .setIcon(IconCompat.createWithBitmap(icon))
                    .setIntent(i)
                    .build();
            ShortcutManagerCompat.requestPinShortcut(this, info, null);
            toast("Home screen par add karne ka popup confirm karo");
        } catch (Exception e) {
            toast("Shortcut nahi ban paya");
        }
    }

    /** Round (circle) icon with transparent corners. */
    private Bitmap composeRound(Bitmap src, boolean maskable, String bgCss, String name) {
        int s = 256;
        Bitmap out = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Path clip = new Path();
        clip.addCircle(s / 2f, s / 2f, s / 2f, Path.Direction.CW);
        c.save();
        c.clipPath(clip);
        if (src != null) {
            int corner = src.getPixel(0, 0);
            boolean opaque = Color.alpha(corner) >= 250;
            Integer parsed = parseCssColor(bgCss);
            float scale;
            if (opaque) {
                c.drawColor(corner);
                scale = maskable ? 1f : 0.86f;
            } else {
                c.drawColor(parsed != null ? parsed : Color.WHITE);
                scale = maskable ? 0.9f : 0.62f;
            }
            int t = (int) (s * scale);
            Bitmap sc = Bitmap.createScaledBitmap(src, t, t, true);
            c.drawBitmap(sc, (s - t) / 2f, (s - t) / 2f, null);
        } else {
            p.setShader(new LinearGradient(0, 0, s, s, 0xFF00E5FF, 0xFF7C4DFF, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, s, s, p);
            p.setShader(null);
            p.setColor(Color.WHITE);
            p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(140);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            String letter = name == null || name.isEmpty() ? "I" : name.substring(0, 1).toUpperCase();
            c.drawText(letter, s / 2f, s / 2f - (p.descent() + p.ascent()) / 2f, p);
        }
        c.restore();
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

    // ------------------------------------------------------------------ UI helpers

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

        main.addView(buildTabStripBar());
        main.addView(buildTopBar());

        FrameLayout line = new FrameLayout(this);
        accentLine = new View(this);
        accentLine.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                privateMode ? new int[]{0xFF35363A, 0xFF35363A}
                        : new int[]{0xFF00E5FF, 0xFF2979FF, 0xFF7C4DFF, 0xFFFF4081}));
        line.addView(accentLine, new FrameLayout.LayoutParams(MATCH, MATCH));
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        progressBar.setProgressTintList(ColorStateList.valueOf(BLUE));
        line.addView(progressBar, new FrameLayout.LayoutParams(MATCH, MATCH));
        main.addView(line, new LinearLayout.LayoutParams(MATCH, dp(3)));
        main.addView(buildFindBar());

        webContainer = new FrameLayout(this);
        main.addView(webContainer, new LinearLayout.LayoutParams(MATCH, 0, 1f));

        homeView = privateMode ? buildPrivateHome() : buildHomeRoot();
        tabSwitcher = buildTabSwitcher();
        tabSwitcher.setVisibility(View.GONE);
        root.addView(tabSwitcher, new FrameLayout.LayoutParams(MATCH, MATCH));
        panelOverlay = buildPanelOverlay();
        panelOverlay.setVisibility(View.GONE);
        root.addView(panelOverlay, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    // ------------------------------------------------------------------ Chrome-like tab strip (top)

    private View buildTabStripBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.BOTTOM);
        bar.setBackgroundColor(cStrip);
        bar.setPadding(dp(6), dp(6), dp(4), 0);
        bar.setLayoutParams(new LinearLayout.LayoutParams(MATCH, dp(44)));

        tabStripScroll = new HorizontalScrollView(this);
        tabStripScroll.setHorizontalScrollBarEnabled(false);
        tabStripScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        tabStrip = new LinearLayout(this);
        tabStrip.setOrientation(LinearLayout.HORIZONTAL);
        tabStrip.setGravity(Gravity.BOTTOM);
        tabStripScroll.addView(tabStrip, new ViewGroup.LayoutParams(WRAP, MATCH));
        bar.addView(tabStripScroll, new LinearLayout.LayoutParams(0, MATCH, 1f));

        if (!privateMode) {
            IconView mode = new IconView(this, darkMode ? IconView.SUN : IconView.MOON,
                    darkMode ? 0xFFFFC107 : 0xFF8A93A6);
            mode.setPadding(dp(8), dp(8), dp(8), dp(8));
            mode.setBackground(ripple(true));
            mode.setOnClickListener(v -> toggleDarkMode());
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(dp(34), dp(34));
            mlp.setMargins(dp(2), 0, dp(2), dp(2));
            bar.addView(mode, mlp);
        }

        IconView plus = new IconView(this, IconView.PLUS, INK);
        plus.setPadding(dp(7), dp(7), dp(7), dp(7));
        plus.setBackground(ripple(true));
        plus.setOnClickListener(v -> createNewTab(""));
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(dp(34), dp(34));
        plp.setMargins(dp(4), 0, dp(2), dp(2));
        bar.addView(plus, plp);
        return bar;
    }

    private void scheduleStripRefresh() {
        if (tabStrip == null || stripRefreshPending) return;
        stripRefreshPending = true;
        mainHandler.postDelayed(() -> {
            stripRefreshPending = false;
            refreshTabStrip(false);
        }, 300);
    }

    private void refreshTabStrip(boolean scrollToActive) {
        if (tabStrip == null) return;
        tabStrip.removeAllViews();
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        int active = tabManager.getActiveTabIdx();
        for (int i = 0; i < tabs.size(); i++) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(168), dp(36));
            lp.setMargins(dp(1), 0, dp(1), 0);
            tabStrip.addView(buildStripTab(tabs.get(i), i == active), lp);
        }
        if (scrollToActive) {
            final int a = active;
            tabStripScroll.post(() -> {
                View v = tabStrip.getChildAt(a);
                if (v != null) tabStripScroll.smoothScrollTo(Math.max(0, v.getLeft() - dp(40)), 0);
            });
        }
    }

    private View buildStripTab(final TabManager.TabItem tab, boolean active) {
        boolean isHome = tab.url == null || tab.url.isEmpty();
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setPadding(dp(10), 0, dp(4), 0);
        float r = dp(12);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(active ? cActive : 0x00FFFFFF);
        bg.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        chip.setBackground(bg);

        ImageView fav = new ImageView(this);
        fav.setBackground(ovalBg(isHome ? 0xFF8AB4F8 : 0xFFFFFFFF, 0, 0));
        fav.setPadding(dp(1), dp(1), dp(1), dp(1));
        chip.addView(fav, new LinearLayout.LayoutParams(dp(16), dp(16)));

        TextView t = new TextView(this);
        t.setText(isHome ? "New tab" : (tab.title != null && !tab.title.isEmpty() ? tab.title : tab.url));
        t.setTextSize(12);
        t.setTextColor(active ? cText : cInactive);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        t.setPadding(dp(8), 0, dp(4), 0);
        chip.addView(t, new LinearLayout.LayoutParams(0, WRAP, 1f));

        IconView close = new IconView(this, IconView.CLOSE, INK);
        close.setPadding(dp(6), dp(6), dp(6), dp(6));
        close.setOnClickListener(v -> {
            int idx = tabManager.getTabs().indexOf(tab);
            if (idx >= 0) closeTab(idx);
        });
        chip.addView(close, new LinearLayout.LayoutParams(dp(24), dp(24)));

        chip.setOnClickListener(v -> {
            int idx = tabManager.getTabs().indexOf(tab);
            if (idx >= 0) {
                tabManager.setActiveTabIdx(idx);
                renderActiveTab();
            }
        });
        chip.setOnLongClickListener(v -> {
            showTabMenu(v, tab);
            return true;
        });

        if (!isHome) {
            String host = hostOf(tab.url);
            if (!host.isEmpty()) loadFavicon(host, fav, null);
        }
        return chip;
    }

    // ------------------------------------------------------------------ top bar

    private View buildTopBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(cBar);
        bar.setPadding(dp(6), 0, dp(6), 0);
        bar.setElevation(dp(2));
        bar.setLayoutParams(new LinearLayout.LayoutParams(MATCH, dp(54)));

        bar.addView(iconButton(IconView.HOME, 0xFF2979FF, v -> navigateHome()), new LinearLayout.LayoutParams(dp(40), dp(40)));
        if (getResources().getConfiguration().screenWidthDp >= 600) {
            bar.addView(iconButton(IconView.BACK, v -> goBackPage()), new LinearLayout.LayoutParams(dp(40), dp(40)));
            View fwd = iconButton(IconView.BACK, v -> goForwardPage());
            fwd.setRotation(180);
            bar.addView(fwd, new LinearLayout.LayoutParams(dp(40), dp(40)));
        }
        bar.addView(iconButton(IconView.REFRESH, 0xFF00ACC1, v -> refreshCurrentTab()), new LinearLayout.LayoutParams(dp(40), dp(40)));

        LinearLayout pill = new LinearLayout(this);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setBackground(roundBg(cPill, 20, 0, 0));
        pill.setPadding(dp(12), 0, dp(8), 0);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(0, dp(40), 1f);
        plp.setMargins(dp(4), 0, dp(6), 0);

        pill.addView(new IconView(this, IconView.SEARCH, 0xFF7C4DFF), new LinearLayout.LayoutParams(dp(18), dp(18)));
        etSearchUrl = new EditText(this);
        etSearchUrl.setBackground(null);
        etSearchUrl.setHint(privateMode ? "Search privately or type URL" : "Search Google or type URL");
        etSearchUrl.setHintTextColor(0xFF70757A);
        etSearchUrl.setTextColor(cText);
        etSearchUrl.setTextSize(14);
        etSearchUrl.setSingleLine(true);
        etSearchUrl.setSelectAllOnFocus(true);
        etSearchUrl.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        etSearchUrl.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        etSearchUrl.setPadding(dp(8), 0, 0, 0);
        etSearchUrl.setOnEditorActionListener((v, actionId, event) -> handleSearch(etSearchUrl, actionId, event));
        etSearchUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showUrlPopup();
            else dismissUrlPopup();
        });
        etSearchUrl.setOnClickListener(v -> showUrlPopup());
        pill.addView(etSearchUrl, new LinearLayout.LayoutParams(0, MATCH, 1f));
        urlStar = new IconView(this, IconView.STAR, 0xFF9AA0A6);
        urlStar.setPadding(dp(5), dp(5), dp(5), dp(5));
        urlStar.setVisibility(View.GONE);
        urlStar.setOnClickListener(v -> {
            toggleBookmark(currentTab());
            updateUrlStar();
        });
        pill.addView(urlStar, new LinearLayout.LayoutParams(dp(30), dp(30)));
        urlPill = pill;
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
            dismissUrlPopup();
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

    // ------------------------------------------------------------------ home screen

    private int homeTheme() {
        int t = prefs.getInt("home_theme", 0);
        return (t < 0 || t > 4) ? 0 : t;
    }

    private Bitmap loadHomeImage() {
        if (privateMode || !prefs.getBoolean("home_image", false)) return null;
        try {
            File f = new File(getFilesDir(), "home_bg.jpg");
            if (!f.exists()) return null;
            return BitmapFactory.decodeFile(f.getAbsolutePath());
        } catch (Throwable e) {
            return null;
        }
    }

    private View buildHomeRoot() {
        FrameLayout rootF = new FrameLayout(this);
        int theme = homeTheme();
        int[] bg = darkMode ? new int[]{0xFF202124, 0xFF17181A} : THEME_BG[theme];
        homeBitmap = loadHomeImage();
        homeImgMode = homeBitmap != null;
        if (homeImgMode) {
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageBitmap(homeBitmap);
            rootF.addView(iv, new FrameLayout.LayoutParams(MATCH, MATCH));
            View dim = new View(this);
            dim.setBackgroundColor(darkMode ? 0x88000000 : 0x44000000);
            rootF.addView(dim, new FrameLayout.LayoutParams(MATCH, MATCH));
        } else {
            rootF.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, bg));
        }
        rootF.addView(buildHomeView(theme, bg[0]), new FrameLayout.LayoutParams(MATCH, MATCH));

        IconView pen = new IconView(this, IconView.PENCIL, Color.WHITE);
        pen.setPadding(dp(14), dp(14), dp(14), dp(14));
        pen.setBackground(ovalBg(0xDD3C4043, 0, 0));
        pen.setElevation(dp(6));
        pen.setOnClickListener(v -> showAppearanceSheet(false));
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(dp(52), dp(52), Gravity.BOTTOM | Gravity.END);
        plp.setMargins(0, 0, dp(18), dp(18));
        rootF.addView(pen, plp);
        return rootF;
    }

    private View makePill(int icon, int iconColor, String label, View.OnClickListener l) {
        LinearLayout pill = new LinearLayout(this);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER);
        pill.setBackground(roundBg(darkMode ? 0xFF3C4043 : Color.WHITE, 20, darkMode ? 0xFF5F6368 : 0xFFD6DCFF, 1.5f));
        pill.setElevation(dp(2));
        pill.setPadding(dp(12), 0, dp(14), 0);
        pill.addView(new IconView(this, icon, iconColor), new LinearLayout.LayoutParams(dp(18), dp(18)));
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(cText);
        t.setPadding(dp(8), 0, 0, 0);
        pill.addView(t, new LinearLayout.LayoutParams(WRAP, WRAP));
        pill.setOnClickListener(l);
        return pill;
    }

    private ScrollView buildHomeView(int theme, int groundColor) {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackgroundColor(Color.TRANSPARENT);

        boolean wide = getResources().getConfiguration().screenWidthDp >= 700;
        int[] iceC = THEME_ICE[theme];
        int[] coolC = THEME_COOL[theme];

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(0, 0, 0, dp(96));

        if (homeImgMode) {
            col.addView(new View(this), new LinearLayout.LayoutParams(MATCH, dp(70)));
        } else {
            col.addView(new IceScene(this, theme, groundColor), new LinearLayout.LayoutParams(MATCH, dp(200)));
        }

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView ice = new TextView(this);
        ice.setText("ICE");
        ice.setTextSize(54);
        ice.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        ice.setLetterSpacing(0.1f);
        ice.setIncludeFontPadding(false);
        ice.setShadowLayer(dp(10), 0, dp(3), (iceC[0] & 0x00FFFFFF) | 0x66000000);
        applyGradient(ice, iceC);
        titleRow.addView(ice, new LinearLayout.LayoutParams(WRAP, WRAP));

        TextView flake = new TextView(this);
        flake.setText("\u2744");
        flake.setTextSize(20);
        flake.setTextColor(coolC[1]);
        flake.setPadding(dp(6), 0, dp(2), 0);
        titleRow.addView(flake, new LinearLayout.LayoutParams(WRAP, WRAP));

        TextView cool = new TextView(this);
        cool.setText("cool");
        cool.setTextSize(52);
        cool.setTypeface(Typeface.create("cursive", Typeface.BOLD_ITALIC));
        cool.setIncludeFontPadding(false);
        cool.setPadding(0, 0, dp(6), 0);
        cool.setShadowLayer(dp(10), 0, dp(3), (coolC[1] & 0x00FFFFFF) | 0x66000000);
        applyGradient(cool, coolC);
        titleRow.addView(cool, new LinearLayout.LayoutParams(WRAP, WRAP));
        col.addView(titleRow, new LinearLayout.LayoutParams(WRAP, WRAP));

        TextView tag = new TextView(this);
        tag.setText("Stay chill  \u2022  Browse fast");
        tag.setTextColor(homeImgMode ? Color.WHITE : coolC[0]);
        tag.setTextSize(13);
        tag.setTypeface(Typeface.DEFAULT_BOLD);
        tag.setLetterSpacing(0.05f);
        tag.setBackground(roundBg((coolC[0] & 0x00FFFFFF) | 0x33000000, 16, 0, 0));
        tag.setPadding(dp(16), dp(6), dp(16), dp(6));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(WRAP, WRAP);
        tlp.setMargins(0, dp(10), 0, dp(18));
        col.addView(tag, tlp);

        // smaller, rounded search bar
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(roundBg(darkMode ? 0xFF3C4043 : Color.WHITE, 23, darkMode ? 0xFF5F6368 : 0xFFD6DCFF, 1.5f));
        card.setElevation(dp(4));
        card.setPadding(dp(16), 0, dp(12), 0);
        card.addView(new IconView(this, IconView.SEARCH, 0xFF2979FF), new LinearLayout.LayoutParams(dp(20), dp(20)));
        etHomeSearch = new EditText(this);
        etHomeSearch.setBackground(null);
        etHomeSearch.setHint("Search Google or type URL");
        etHomeSearch.setHintTextColor(0xFF80868B);
        etHomeSearch.setTextColor(cText);
        etHomeSearch.setTextSize(15);
        etHomeSearch.setSingleLine(true);
        etHomeSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        etHomeSearch.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        etHomeSearch.setPadding(dp(10), 0, 0, 0);
        etHomeSearch.setOnEditorActionListener((v, actionId, event) -> handleSearch(etHomeSearch, actionId, event));
        card.addView(etHomeSearch, new LinearLayout.LayoutParams(0, MATCH, 1f));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(wide ? dp(520) : MATCH, dp(46));
        cardLp.setMargins(wide ? 0 : dp(36), 0, wide ? 0 : dp(36), dp(14));
        col.addView(card, cardLp);

        // two small round buttons: AI Mode + Private window
        LinearLayout pills = new LinearLayout(this);
        pills.setOrientation(LinearLayout.HORIZONTAL);
        pills.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams pl1 = new LinearLayout.LayoutParams(0, dp(38), 1f);
        pl1.setMargins(dp(5), 0, dp(5), 0);
        LinearLayout.LayoutParams pl2 = new LinearLayout.LayoutParams(0, dp(38), 1f);
        pl2.setMargins(dp(5), 0, dp(5), 0);
        pills.addView(makePill(IconView.SPARK, 0xFF4285F4, "AI Mode",
                v -> loadUrlInCurrentTab("https://www.google.com/search?udm=50")), pl1);
        pills.addView(makePill(IconView.PRIVATE, 0xFF8E24AA, "Private window", v -> openPrivateWindow()), pl2);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(wide ? dp(520) : MATCH, WRAP);
        plp.setMargins(wide ? 0 : dp(31), 0, wide ? 0 : dp(31), dp(22));
        col.addView(pills, plp);

        shortcutsBox = new LinearLayout(this);
        shortcutsBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams sbLp = new LinearLayout.LayoutParams(MATCH, WRAP);
        sbLp.setMargins(dp(16), 0, dp(16), 0);
        col.addView(shortcutsBox, sbLp);
        rebuildShortcuts();

        sv.addView(col, new ViewGroup.LayoutParams(MATCH, WRAP));
        return sv;
    }

    // ---- shortcuts shown on the browser home (pinned ones first, user can remove them) ----

    private List<String[]> loadPinned() {
        List<String[]> out = new ArrayList<>();
        try {
            String js = prefs.getString("pinned", null);
            if (js == null) return out;
            JSONArray arr = new JSONArray(js);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(new String[]{o.optString("n"), o.optString("u")});
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private void savePinned(List<String[]> list) {
        try {
            JSONArray arr = new JSONArray();
            for (String[] p : list) {
                JSONObject o = new JSONObject();
                o.put("n", p[0]);
                o.put("u", p[1]);
                arr.put(o);
            }
            prefs.edit().putString("pinned", arr.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private void pinToBrowserHome(TabManager.TabItem tab) {
        if (privateMode) return;
        if (tab == null || tab.url == null || tab.url.isEmpty()) {
            toast("Ye khali tab hai, pehle koi site kholo");
            return;
        }
        List<String[]> list = loadPinned();
        for (String[] p : list) {
            if (p[1].equals(tab.url)) {
                toast("Ye pehle se Ice Cool home me pinned hai");
                return;
            }
        }
        if (list.size() >= 24) {
            toast("Max 24 pin ho sakte hain, pehle koi hata do");
            return;
        }
        String host = hostOf(tab.url);
        String name = (tab.title != null && !tab.title.isEmpty() && !tab.title.equals(tab.url)) ? tab.title : host;
        list.add(0, new String[]{name, tab.url});
        savePinned(list);
        rebuildShortcuts();
        toast("Ice Cool home me pin ho gaya (hatane ke liye long-press)");
    }

    private void removePinned(String url) {
        List<String[]> list = loadPinned();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)[1].equals(url)) {
                list.remove(i);
                break;
            }
        }
        savePinned(list);
        rebuildShortcuts();
    }

    private void rebuildShortcuts() {
        if (shortcutsBox == null) return;
        shortcutsBox.removeAllViews();
        List<View> tiles = new ArrayList<>();
        for (final String[] p : loadPinned()) {
            View tile = buildShortcut(p[0], p[1], hostOf(p[1]), 0xFF7C4DFF);
            tile.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Remove from home?")
                        .setMessage(p[0])
                        .setPositiveButton("Remove", (d, w) -> removePinned(p[1]))
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
            tiles.add(tile);
        }
        Set<String> hidden = hiddenDefaults();
        for (int i = 0; i < DEF_NAMES.length; i++) {
            if (hidden.contains(DEF_URLS[i])) continue;
            final int idx = i;
            View dtile = buildShortcut(DEF_NAMES[i], DEF_URLS[i], DEF_DOMAINS[i], DEF_RINGS[i]);
            dtile.setOnLongClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Remove from home?")
                        .setMessage(DEF_NAMES[idx])
                        .setPositiveButton("Remove", (dd, ww) -> hideDefault(DEF_URLS[idx]))
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });
            tiles.add(dtile);
        }
        LinearLayout row = null;
        for (int i = 0; i < tiles.size(); i++) {
            if (i % 4 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                shortcutsBox.addView(row, new LinearLayout.LayoutParams(MATCH, WRAP));
            }
            row.addView(tiles.get(i), new LinearLayout.LayoutParams(0, WRAP, 1f));
        }
        if (row != null) {
            int rem = (4 - (tiles.size() % 4)) % 4;
            for (int k = 0; k < rem; k++) row.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        }
    }

    // ---- private window welcome page (dark, Chrome-incognito style) ----

    private ScrollView buildPrivateHome() {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackgroundColor(0xFF202124);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(dp(24), dp(40), dp(24), dp(40));

        col.addView(new PrivateBadge(this), new LinearLayout.LayoutParams(dp(96), dp(96)));

        TextView title = new TextView(this);
        title.setText("Welcome to private window");
        title.setTextSize(26);
        title.setTextColor(0xFFE8EAED);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(WRAP, WRAP);
        tlp.setMargins(0, dp(22), 0, dp(14));
        col.addView(title, tlp);

        boolean wide = getResources().getConfiguration().screenWidthDp >= 600;
        int contentW = wide ? dp(640) : MATCH;

        TextView para = new TextView(this);
        para.setText("Now you can browse privately, and other people who use this device won't see your activity. "
                + "However, files you download will be saved.");
        para.setTextSize(14);
        para.setTextColor(0xFFBDC1C6);
        para.setLineSpacing(0, 1.25f);
        col.addView(para, new LinearLayout.LayoutParams(contentW, WRAP));

        LinearLayout cols = new LinearLayout(this);
        cols.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        LinearLayout.LayoutParams a = wide ? new LinearLayout.LayoutParams(0, WRAP, 1f)
                : new LinearLayout.LayoutParams(MATCH, WRAP);
        LinearLayout.LayoutParams b = wide ? new LinearLayout.LayoutParams(0, WRAP, 1f)
                : new LinearLayout.LayoutParams(MATCH, WRAP);
        a.topMargin = dp(22);
        b.topMargin = dp(22);
        cols.addView(infoBlock("Ice Cool won't save the following information:",
                new String[]{"Your browsing history", "Cookies and site data", "Information entered in forms"}), a);
        cols.addView(infoBlock("Your activity might still be visible to:",
                new String[]{"Websites you visit", "Your employer or school", "Your internet service provider"}), b);
        col.addView(cols, new LinearLayout.LayoutParams(contentW, WRAP));

        TextView note = new TextView(this);
        note.setText("Ad blocker is on by default in a private window, so ads and trackers won't load. "
                + "Screenshots and screen recording are blocked. "
                + "Closing all private tabs (or pressing back) deletes everything from this session.");
        note.setTextSize(13);
        note.setTextColor(0xFF8AB4F8);
        note.setLineSpacing(0, 1.2f);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(contentW, WRAP);
        nlp.topMargin = dp(26);
        col.addView(note, nlp);

        sv.addView(col, new ViewGroup.LayoutParams(MATCH, WRAP));
        return sv;
    }

    private View infoBlock(String heading, String[] items) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, 0, dp(12), dp(8));
        TextView h = new TextView(this);
        h.setText(heading);
        h.setTextSize(14);
        h.setTextColor(0xFFE8EAED);
        box.addView(h, new LinearLayout.LayoutParams(MATCH, WRAP));
        for (String it : items) {
            TextView t = new TextView(this);
            t.setText("\u2022  " + it);
            t.setTextSize(14);
            t.setTextColor(0xFFBDC1C6);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);
            lp.topMargin = dp(6);
            box.addView(t, lp);
        }
        return box;
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
        label.setTextColor(homeImgMode ? Color.WHITE : cText);
        label.setTextSize(11);
        if (homeImgMode) label.setShadowLayer(dp(3), 0, dp(1), 0xFF000000);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setMaxWidth(dp(76));
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(WRAP, WRAP);
        llp.topMargin = dp(6);
        tile.addView(label, llp);

        tile.setOnClickListener(v -> loadUrlInCurrentTab(url));
        loadFavicon(domain, icon, letter);
        return tile;
    }

    // ------------------------------------------------------------------ tab switcher (grid)

    private FrameLayout buildTabSwitcher() {
        FrameLayout sw = new FrameLayout(this);
        sw.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{cSwA, cSwB}));
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
        switcherTitle.setTextColor(cTitle);
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
        if (tabGrid == null) return;
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

    private View buildTabCard(final TabManager.TabItem tab, int index, boolean active) {
        boolean isHome = tab.url == null || tab.url.isEmpty();

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundBg(cCard != 0 ? cCard : PASTELS[index % PASTELS.length], 20, BLUE, active ? 2.5f : 0));
        card.setElevation(dp(active ? 4 : 1));
        card.setPadding(dp(8), dp(6), dp(8), dp(8));
        card.setOnClickListener(v -> {
            int idx = tabManager.getTabs().indexOf(tab);
            if (idx >= 0) tabManager.setActiveTabIdx(idx);
            hideTabSwitcher();
            renderActiveTab();
        });
        card.setOnLongClickListener(v -> {
            showTabMenu(v, tab);
            return true;
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
        t.setTextColor(cText);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        t.setPadding(dp(8), 0, dp(2), 0);
        head.addView(t, new LinearLayout.LayoutParams(0, WRAP, 1f));

        IconView more = new IconView(this, IconView.DOTS, INK);
        more.setPadding(dp(6), dp(6), dp(6), dp(6));
        more.setOnClickListener(v -> showTabMenu(v, tab));
        head.addView(more, new LinearLayout.LayoutParams(dp(26), dp(26)));

        IconView close = new IconView(this, IconView.CLOSE, INK);
        close.setPadding(dp(4), dp(4), dp(4), dp(4));
        close.setOnClickListener(v -> {
            int idx = tabManager.getTabs().indexOf(tab);
            if (idx >= 0) closeTab(idx);
        });
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

    // ------------------------------------------------------------------ menus

    private LinearLayout newMenuBox() {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackground(roundBg(cMenuBg, 20, 0, 0));
        menu.setElevation(dp(8));
        menu.setPadding(0, dp(8), 0, dp(8));
        return menu;
    }

    private void openPopup(View anchor, LinearLayout menu, int widthDp, boolean alignRight) {
        dismissMenu();
        ScrollView sv = new ScrollView(this);
        sv.setClipToPadding(false);
        sv.setPadding(dp(10), dp(10), dp(10), dp(14));
        sv.addView(menu, new ViewGroup.LayoutParams(MATCH, WRAP));
        menu.measure(View.MeasureSpec.makeMeasureSpec(dp(widthDp) - dp(20), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int popH = Math.min(menu.getMeasuredHeight() + dp(26),
                getResources().getDisplayMetrics().heightPixels - dp(130));
        menuPopup = new PopupWindow(sv, dp(widthDp), popH, true);
        menuPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (alignRight) {
            menuPopup.showAsDropDown(anchor, anchor.getWidth() - dp(widthDp) + dp(8), -anchor.getHeight() + dp(4));
        } else {
            menuPopup.showAsDropDown(anchor, 0, -dp(8));
        }
    }

    private void dismissMenu() {
        if (menuPopup != null && menuPopup.isShowing()) menuPopup.dismiss();
    }

    private void showMenu(View anchor) {
        LinearLayout menu = newMenuBox();
        final TabManager.TabItem cur = currentTab();

        menu.addView(menuRow(IconView.PLUS, "New tab", 0xFF2979FF, v -> {
            dismissMenu();
            createNewTab("");
        }));
        if (privateMode) {
            menu.addView(menuRow(IconView.CLOSE, "Close private window", 0xFFD93025, v -> {
                dismissMenu();
                exitPrivateWindow();
            }));
        } else {
            menu.addView(menuRow(IconView.PRIVATE, "New private window", 0xFF8E24AA, v -> {
                dismissMenu();
                openPrivateWindow();
            }));
        }
        menu.addView(menuRow(IconView.CLOSEALL, "Close all tabs", 0xFFF4511E, v -> {
            dismissMenu();
            closeAllTabs();
        }));
        menu.addView(menuRow(IconView.DOWNLOAD, "My downloads", 0xFF0F9D58, v -> {
            dismissMenu();
            showPanel(1);
        }));
        menu.addView(menuRow(IconView.DOWNLOAD, "Download this page", 0xFF00ACC1, v -> {
            dismissMenu();
            TabManager.TabItem t = currentTab();
            if (t == null || t.url.isEmpty()) {
                toast("Pehle koi page kholo");
            } else {
                startDownload(t.url, t.webView.getSettings().getUserAgentString(), null, "text/html");
            }
        }));
        if (!privateMode) {
            menu.addView(menuRow(IconView.LIST, "My bookmarks", 0xFFFFA000, v -> {
                dismissMenu();
                showPanel(2);
            }));
            boolean marked = cur != null && cur.url != null && !cur.url.isEmpty() && isBookmarked(cur.url);
            menu.addView(menuRow(marked ? IconView.STARF : IconView.STAR,
                    marked ? "Remove bookmark" : "Add bookmark", 0xFFFFB300, v -> {
                        dismissMenu();
                        toggleBookmark(currentTab());
                    }));
        }
        if (!privateMode) {
            menu.addView(menuRow(IconView.INSTALL, "Install & create shortcut", 0xFF7C4DFF, v -> {
                dismissMenu();
                startInstallFlow();
            }));
            menu.addView(menuRow(IconView.PIN, "Pin to Ice Cool home", 0xFFE91E63, v -> {
                dismissMenu();
                pinToBrowserHome(currentTab());
            }));
        }

        // Ad blocker toggle
        String abSub = privateMode ? "ON by default in private window \u2022 no ads will load here"
                : (adBlockEnabled ? blockedCount.get() + " ads & trackers blocked" : "Blocks ads, popups & trackers");
        menu.addView(menuSwitchRow(IconView.SHIELD, "Ad blocker", abSub, 0xFF00C853, adBlockEnabled, (b, on) -> {
            adBlockEnabled = on;
            if (!privateMode) prefs.edit().putBoolean("adblock", on).apply();
            toast(on ? "Ad blocker ON" : "Ad blocker OFF");
            TabManager.TabItem t = currentTab();
            if (t != null && t.url != null && !t.url.isEmpty() && t.webView != null) t.webView.reload();
        }));

        menu.addView(menuRow(IconView.SEARCH, "Find in page", 0xFF5C6BC0, v -> {
            dismissMenu();
            showFindBar();
        }));
        menu.addView(menuRow(IconView.SHARE, "Share page", 0xFF2979FF, v -> {
            dismissMenu();
            shareCurrentPage();
        }));

        // Reading mode
        final boolean reading = cur != null && readingTabs.contains(cur);
        menu.addView(menuSwitchRow(IconView.BOOK, "Reading mode", "Yellow page \u2022 no ads \u2022 your font", 0xFFFFA000,
                reading, (b, on) -> {
                    toggleReading(currentTab(), on);
                    dismissMenu();
                }));
        menu.addView(menuRow(IconView.FONT, "Reading text style", 0xFFFB8C00, v -> {
            dismissMenu();
            showReadingStyleDialog();
        }));

        // Desktop site (per tab)
        final boolean desktop = cur != null && desktopTabs.contains(cur);
        menu.addView(menuCheckRow(IconView.DESKTOP, "Desktop site", 0xFF455A64, desktop, v -> {
            dismissMenu();
            toggleDesktop(currentTab());
        }));

        // Page zoom row (per site, whole page like Chrome)
        LinearLayout zoom = new LinearLayout(this);
        zoom.setOrientation(LinearLayout.HORIZONTAL);
        zoom.setGravity(Gravity.CENTER_VERTICAL);
        zoom.setPadding(dp(16), 0, dp(10), 0);
        zoom.addView(new IconView(this, IconView.SEARCH, 0xFFFFA000), new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView zl = new TextView(this);
        zl.setText("Zoom");
        zl.setTextSize(16);
        zl.setTextColor(cText);
        zl.setPadding(dp(16), 0, 0, 0);
        zoom.addView(zl, new LinearLayout.LayoutParams(0, WRAP, 1f));
        final TextView zv = new TextView(this);
        int shownZoom = (cur != null && !cur.url.isEmpty()) ? zoomFor(hostOf(cur.url)) : 100;
        zv.setText(shownZoom + "%");
        zv.setTextSize(15);
        zv.setGravity(Gravity.CENTER);
        zv.setTextColor(cText);

        IconView minus = new IconView(this, IconView.MINUS, INK);
        minus.setPadding(dp(8), dp(8), dp(8), dp(8));
        minus.setBackground(ripple(true));
        minus.setOnClickListener(v -> changeSiteZoom(-1, zv));
        IconView plusZ = new IconView(this, IconView.PLUS, INK);
        plusZ.setPadding(dp(8), dp(8), dp(8), dp(8));
        plusZ.setBackground(ripple(true));
        plusZ.setOnClickListener(v -> changeSiteZoom(1, zv));
        zoom.addView(minus, new LinearLayout.LayoutParams(dp(36), dp(36)));
        zoom.addView(zv, new LinearLayout.LayoutParams(dp(52), WRAP));
        zoom.addView(plusZ, new LinearLayout.LayoutParams(dp(36), dp(36)));
        menu.addView(zoom, new LinearLayout.LayoutParams(MATCH, dp(48)));

        View div = new View(this);
        div.setBackgroundColor(0xFFE8EAED);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(MATCH, dp(1));
        dlp.setMargins(dp(16), dp(4), dp(16), dp(4));
        menu.addView(div, dlp);

        if (!privateMode) {
            menu.addView(menuRow(IconView.TRASH, "Clear all cookies & data", 0xFFD93025, v -> {
                dismissMenu();
                confirmClearData();
            }));
        }

        menu.addView(menuRow(IconView.INFO, "About us", 0xFF5C6BC0, v -> {
            dismissMenu();
            showPanel(3);
        }));

        openPopup(anchor, menu, 300, true);
    }

    private void showTabMenu(View anchor, final TabManager.TabItem tab) {
        LinearLayout menu = newMenuBox();
        if (!privateMode) {
            menu.addView(menuRow(IconView.PIN, "Pin to Ice Cool home", 0xFFE91E63, v -> {
                dismissMenu();
                pinToBrowserHome(tab);
            }));
            menu.addView(menuRow(IconView.INSTALL, "Add to phone home screen", 0xFF7C4DFF, v -> {
                dismissMenu();
                pinTab(tab);
            }));
        }
        menu.addView(menuRow(IconView.TRASH, "Clear this site's data & cookies", 0xFFD93025, v -> {
            dismissMenu();
            clearSiteData(tab);
        }));
        menu.addView(menuRow(IconView.COPY, "Duplicate tab", 0xFF00ACC1, v -> {
            dismissMenu();
            createNewTab(tab.url == null ? "" : tab.url);
        }));
        menu.addView(menuRow(IconView.CLOSEALL, "Close other tabs", 0xFFF4511E, v -> {
            dismissMenu();
            closeOtherTabs(tab);
        }));
        menu.addView(menuRow(IconView.CLOSE, "Close tab", 0xFF455A64, v -> {
            dismissMenu();
            int idx = tabManager.getTabs().indexOf(tab);
            if (idx >= 0) closeTab(idx);
        }));
        openPopup(anchor, menu, 290, false);
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
        t.setTextColor(color == 0xFFD93025 ? color : cText);
        t.setPadding(dp(16), 0, 0, 0);
        r.addView(t, new LinearLayout.LayoutParams(0, WRAP, 1f));
        r.setOnClickListener(l);
        r.setLayoutParams(new LinearLayout.LayoutParams(MATCH, dp(48)));
        return r;
    }

    private View menuSwitchRow(int icon, String label, String sub, int color, boolean checked,
                               CompoundButton.OnCheckedChangeListener l) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(16), dp(6), dp(12), dp(6));
        r.setBackground(ripple(false));
        r.addView(new IconView(this, icon, color), new LinearLayout.LayoutParams(dp(24), dp(24)));
        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setPadding(dp(16), 0, dp(8), 0);
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(16);
        t.setTextColor(cText);
        tx.addView(t, new LinearLayout.LayoutParams(WRAP, WRAP));
        TextView st = new TextView(this);
        st.setText(sub);
        st.setTextSize(11.5f);
        st.setTextColor(0xFF80868B);
        tx.addView(st, new LinearLayout.LayoutParams(WRAP, WRAP));
        r.addView(tx, new LinearLayout.LayoutParams(0, WRAP, 1f));
        final Switch sw = new Switch(this);
        sw.setChecked(checked);
        sw.setOnCheckedChangeListener(l);
        r.addView(sw, new LinearLayout.LayoutParams(WRAP, WRAP));
        r.setOnClickListener(v -> sw.toggle());
        r.setLayoutParams(new LinearLayout.LayoutParams(MATCH, WRAP));
        return r;
    }

    private View menuCheckRow(int icon, String label, int color, boolean checked, View.OnClickListener l) {
        LinearLayout r = (LinearLayout) menuRow(icon, label, color, l);
        TextView box = new TextView(this);
        box.setGravity(Gravity.CENTER);
        box.setTextSize(14);
        box.setTypeface(Typeface.DEFAULT_BOLD);
        if (checked) {
            box.setText("\u2713");
            box.setTextColor(Color.WHITE);
            box.setBackground(roundBg(BLUE, 6, BLUE, 2));
        } else {
            box.setBackground(roundBg(Color.TRANSPARENT, 6, 0xFF9AA0A6, 2));
        }
        r.addView(box, new LinearLayout.LayoutParams(dp(22), dp(22)));
        return r;
    }

    // ------------------------------------------------------------------ page zoom (whole page, per site)

    private int zoomFor(String host) {
        if (privateMode) {
            Integer z = privateZoom.get(host);
            return z == null ? 100 : z;
        }
        return prefs.getInt("zoom_" + host, 100);
    }

    private void setZoomFor(String host, int z) {
        if (privateMode) privateZoom.put(host, z);
        else prefs.edit().putInt("zoom_" + host, z).apply();
    }

    private void applyZoom(WebView v, String url) {
        if (v == null || url == null || url.isEmpty()) return;
        int z = zoomFor(hostOf(url));
        String val = z == 100 ? "" : String.format(Locale.US, "%.2f", z / 100f);
        v.evaluateJavascript("(function(){var d=document.documentElement;if(d){d.style.zoom='" + val + "';}})();", null);
    }

    private void changeSiteZoom(int dir, TextView label) {
        TabManager.TabItem t = currentTab();
        if (t == null || t.url == null || t.url.isEmpty()) {
            toast("Pehle koi website kholo");
            return;
        }
        String host = hostOf(t.url);
        int cur = zoomFor(host);
        int idx = 0;
        for (int i = 0; i < ZOOM_STEPS.length; i++) {
            if (ZOOM_STEPS[i] <= cur) idx = i;
        }
        idx = Math.max(0, Math.min(ZOOM_STEPS.length - 1, idx + dir));
        int z = ZOOM_STEPS[idx];
        setZoomFor(host, z);
        label.setText(z + "%");
        for (TabManager.TabItem other : tabManager.getTabs()) {
            if (other.url != null && hostOf(other.url).equals(host)) applyZoom(other.webView, other.url);
        }
    }

    private void toggleDesktop(TabManager.TabItem tab) {
        if (tab == null || tab.webView == null) return;
        WebSettings s = tab.webView.getSettings();
        if (desktopTabs.contains(tab)) {
            desktopTabs.remove(tab);
            s.setUserAgentString(mobileUA);
        } else {
            desktopTabs.add(tab);
            s.setUserAgentString(desktopUA);
        }
        if (tab.url != null && !tab.url.isEmpty()) tab.webView.reload();
    }

    // ------------------------------------------------------------------ clear data

    private void confirmClearData() {
        new AlertDialog.Builder(this)
                .setTitle("Clear all data?")
                .setMessage("Cookies, cache, history aur sab sites ka data delete ho jayega.")
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

    private void clearSiteData(TabManager.TabItem tab) {
        if (tab == null || tab.url == null || tab.url.isEmpty()) {
            toast("Is tab me koi site nahi hai");
            return;
        }
        String host = hostOf(tab.url);
        if (host.isEmpty()) return;
        CookieManager cm = CookieManager.getInstance();
        String ck = cm.getCookie(tab.url);
        List<String> domains = new ArrayList<>();
        String h = host;
        while (h.contains(".")) {
            domains.add(h);
            h = h.substring(h.indexOf('.') + 1);
        }
        if (ck != null) {
            for (String part : ck.split(";")) {
                String name = part.trim();
                int eq = name.indexOf('=');
                if (eq > 0) name = name.substring(0, eq);
                if (name.isEmpty()) continue;
                cm.setCookie("https://" + host, name + "=; Max-Age=0; Path=/");
                for (String d : domains) {
                    cm.setCookie("https://" + host, name + "=; Max-Age=0; Path=/; Domain=" + d);
                    cm.setCookie("https://" + host, name + "=; Max-Age=0; Path=/; Domain=." + d);
                }
            }
        }
        cm.flush();
        WebStorage ws = WebStorage.getInstance();
        ws.deleteOrigin("https://" + host);
        ws.deleteOrigin("http://" + host);
        if (tab.webView != null) {
            tab.webView.clearCache(true);
            tab.webView.clearFormData();
            tab.webView.reload();
        }
        toast("Is site ka data & cookies clear ho gaye");
    }

    // ------------------------------------------------------------------ extra comfort features

    private WebView activeWebView() {
        if (appMode) return appWebView;
        TabManager.TabItem t = currentTab();
        return t == null ? null : t.webView;
    }

    private void goBackPage() {
        TabManager.TabItem t = currentTab();
        if (t != null && t.url != null && !t.url.isEmpty() && t.webView != null && t.webView.canGoBack()) t.webView.goBack();
    }

    private void goForwardPage() {
        TabManager.TabItem t = currentTab();
        if (t != null && t.url != null && !t.url.isEmpty() && t.webView != null && t.webView.canGoForward()) t.webView.goForward();
    }

    private void shareCurrentPage() {
        TabManager.TabItem t = currentTab();
        if (t == null || t.url == null || t.url.isEmpty()) {
            toast("Pehle koi page kholo");
            return;
        }
        try {
            Intent sh = new Intent(Intent.ACTION_SEND);
            sh.setType("text/plain");
            sh.putExtra(Intent.EXTRA_SUBJECT, t.title == null ? t.url : t.title);
            sh.putExtra(Intent.EXTRA_TEXT, t.url);
            startActivity(Intent.createChooser(sh, "Share page"));
        } catch (Exception e) {
            toast("Share nahi ho paya");
        }
    }

    private void closeOtherTabs(TabManager.TabItem keep) {
        for (TabManager.TabItem t : new ArrayList<>(tabManager.getTabs())) {
            if (t != keep) destroyTab(t);
        }
        tabManager.clearAllTabs();
        tabManager.addTab(keep);
        tabManager.setActiveTabIdx(0);
        renderActiveTab();
        refreshTabGrid();
        toast("Baaki tabs band ho gaye");
    }

    private View buildFindBar() {
        findBar = new LinearLayout(this);
        findBar.setOrientation(LinearLayout.HORIZONTAL);
        findBar.setGravity(Gravity.CENTER_VERTICAL);
        findBar.setBackgroundColor(cBar);
        findBar.setPadding(dp(14), dp(2), dp(4), dp(2));
        findBar.setVisibility(View.GONE);

        etFind = new EditText(this);
        etFind.setHint("Find in page");
        etFind.setHintTextColor(0xFF80868B);
        etFind.setTextColor(cText);
        etFind.setTextSize(15);
        etFind.setSingleLine(true);
        etFind.setBackground(null);
        etFind.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        etFind.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence cs, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(android.text.Editable e) {
                WebView w = activeWebView();
                if (w == null) return;
                String q = e.toString();
                if (q.isEmpty()) {
                    w.clearMatches();
                    findCount.setText("0/0");
                } else {
                    w.findAllAsync(q);
                }
            }
        });
        etFind.setOnEditorActionListener((v, a, ev) -> {
            WebView w = activeWebView();
            if (w != null) w.findNext(true);
            return true;
        });
        findBar.addView(etFind, new LinearLayout.LayoutParams(0, dp(44), 1f));

        findCount = new TextView(this);
        findCount.setText("0/0");
        findCount.setTextSize(12);
        findCount.setTextColor(0xFF80868B);
        findCount.setPadding(dp(6), 0, dp(6), 0);
        findBar.addView(findCount, new LinearLayout.LayoutParams(WRAP, WRAP));

        View up = iconButton(IconView.CHEVRON, v -> {
            WebView w = activeWebView();
            if (w != null) w.findNext(false);
        });
        up.setRotation(-90);
        findBar.addView(up, new LinearLayout.LayoutParams(dp(40), dp(40)));
        View down = iconButton(IconView.CHEVRON, v -> {
            WebView w = activeWebView();
            if (w != null) w.findNext(true);
        });
        down.setRotation(90);
        findBar.addView(down, new LinearLayout.LayoutParams(dp(40), dp(40)));
        findBar.addView(iconButton(IconView.CLOSE, v -> hideFindBar()), new LinearLayout.LayoutParams(dp(40), dp(40)));
        return findBar;
    }

    private void showFindBar() {
        TabManager.TabItem t = currentTab();
        if (t == null || t.url == null || t.url.isEmpty() || findBar == null) {
            toast("Pehle koi page kholo");
            return;
        }
        findBar.setVisibility(View.VISIBLE);
        etFind.setText("");
        etFind.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(etFind, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideFindBar() {
        if (findBar == null || findBar.getVisibility() != View.VISIBLE) return;
        WebView w = activeWebView();
        if (w != null) w.clearMatches();
        findBar.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etFind.getWindowToken(), 0);
    }

    // ------------------------------------------------------------------ dark / light mode

    private void toggleDarkMode() {
        if (privateMode) return;
        darkMode = !darkMode;
        prefs.edit().putBoolean("dark", darkMode).apply();
        applyPalette(darkMode);
        applyBarColor(darkMode ? cBar : Color.WHITE);
        if (Build.VERSION.SDK_INT >= 29) {
            for (TabManager.TabItem t : tabManager.getTabs()) {
                if (t.webView == null) continue;
                try {
                    t.webView.getSettings().setForceDark(darkMode ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
                } catch (Throwable ignored) {
                }
            }
        }
        dismissMenu();
        root.removeAllViews();
        buildUi();
        renderActiveTab();
        toast(darkMode ? "Dark mode ON" : "Light mode ON");
    }

    // ------------------------------------------------------------------ URL popup (share / copy / edit)

    private void dismissUrlPopup() {
        if (urlPopup != null && urlPopup.isShowing()) urlPopup.dismiss();
    }

    private void updateUrlStar() {
        if (urlStar == null) return;
        TabManager.TabItem t = currentTab();
        boolean show = !privateMode && t != null && t.url != null && !t.url.isEmpty();
        urlStar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            boolean marked = isBookmarked(t.url);
            urlStar.set(marked ? IconView.STARF : IconView.STAR, marked ? 0xFFFFB300 : 0xFF9AA0A6);
        }
    }

    private void showUrlPopup() {
        final TabManager.TabItem t = currentTab();
        if (t == null || t.url == null || t.url.isEmpty() || urlPill == null) return;
        if (urlPopup != null && urlPopup.isShowing()) return;
        if (tabSwitcher != null && tabSwitcher.getVisibility() == View.VISIBLE) return;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(roundBg(cMenuBg, 22, 0, 0));
        card.setElevation(dp(8));
        card.setPadding(dp(14), dp(10), dp(8), dp(10));

        ImageView fav = new ImageView(this);
        fav.setBackground(ovalBg(0xFFEEF1FF, 0, 0));
        fav.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.addView(fav, new LinearLayout.LayoutParams(dp(40), dp(40)));
        String host = hostOf(t.url);
        if (!host.isEmpty()) loadFavicon(host, fav, null);

        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setPadding(dp(12), 0, dp(6), 0);
        TextView title = new TextView(this);
        title.setText(t.title != null && !t.title.isEmpty() ? t.title : host);
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(cText);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        tx.addView(title, new LinearLayout.LayoutParams(MATCH, WRAP));
        TextView url = new TextView(this);
        url.setText(t.url);
        url.setTextSize(12);
        url.setTextColor(0xFF1E8E3E);
        url.setSingleLine(true);
        url.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        tx.addView(url, new LinearLayout.LayoutParams(MATCH, WRAP));
        card.addView(tx, new LinearLayout.LayoutParams(0, WRAP, 1f));

        card.addView(iconButton(IconView.SHARE, BLUE, v -> {
            dismissUrlPopup();
            try {
                Intent sh = new Intent(Intent.ACTION_SEND);
                sh.setType("text/plain");
                sh.putExtra(Intent.EXTRA_TEXT, t.url);
                startActivity(Intent.createChooser(sh, "Share link"));
            } catch (Exception e) {
                toast("Share nahi ho paya");
            }
        }), new LinearLayout.LayoutParams(dp(40), dp(40)));
        card.addView(iconButton(IconView.COPY, 0xFF00ACC1, v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("URL", t.url));
            toast("Link copy ho gaya");
            dismissUrlPopup();
        }), new LinearLayout.LayoutParams(dp(40), dp(40)));
        card.addView(iconButton(IconView.PENCIL, 0xFFFB8C00, v -> {
            dismissUrlPopup();
            etSearchUrl.requestFocus();
            etSearchUrl.selectAll();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etSearchUrl, InputMethodManager.SHOW_IMPLICIT);
        }), new LinearLayout.LayoutParams(dp(40), dp(40)));

        FrameLayout wrap = new FrameLayout(this);
        wrap.setPadding(dp(6), dp(2), dp(6), dp(10));
        wrap.addView(card, new FrameLayout.LayoutParams(MATCH, WRAP));
        int sw = getResources().getDisplayMetrics().widthPixels;
        int width = Math.min(Math.max(urlPill.getWidth() + dp(12), dp(320)), sw - dp(12));
        urlPopup = new PopupWindow(wrap, width, WRAP, false);
        urlPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        try {
            urlPopup.showAsDropDown(urlPill, 0, dp(2));
        } catch (Throwable ignored) {
        }
    }

    // ------------------------------------------------------------------ reading mode

    private static final String[] READ_FONT_NAMES = {"Default", "Serif", "Sans-serif", "Monospace", "Handwriting"};
    private static final String[] READ_FONT_CSS = {"", "Georgia,serif", "Roboto,Arial,sans-serif", "Courier,monospace", "cursive"};

    private String readingJs(boolean on) {
        if (!on) return "(function(){var e=document.getElementById('__ice_read');if(e)e.remove();})();";
        int font = prefs.getInt("read_font", 1);
        if (font < 0 || font >= READ_FONT_CSS.length) font = 1;
        int size = prefs.getInt("read_size", 110);
        String css = "html{background:#F4ECD8!important;-webkit-text-size-adjust:" + size + "%!important}"
                + "body{background:#F4ECD8!important;color:#3B2F1E!important;line-height:1.7!important}"
                + "body *:not(img):not(video):not(svg):not(canvas):not(picture):not(iframe){background-color:transparent!important;"
                + "color:#3B2F1E!important;text-shadow:none!important;box-shadow:none!important}"
                + "a,a *{color:#8A4B0F!important}"
                + "aside,footer,[role=\"complementary\"],[class*=\"sidebar\"],[id*=\"sidebar\"]{display:none!important}"
                + "img,video{filter:sepia(.25) contrast(.95)}"
                + AD_CSS + "{display:none!important}";
        if (!READ_FONT_CSS[font].isEmpty()) {
            css += "body *:not([class*=\"icon\"]):not([class*=\"fa-\"]):not(i):not(svg):not(code):not(pre){font-family:"
                    + READ_FONT_CSS[font] + "!important}";
        }
        return "(function(){var e=document.getElementById('__ice_read');if(!e){e=document.createElement('style');"
                + "e.id='__ice_read';(document.head||document.documentElement).appendChild(e);}"
                + "e.textContent='" + css + "';})();";
    }

    private void applyReading(WebView view) {
        TabManager.TabItem t = findTab(view);
        if (t != null && readingTabs.contains(t)) {
            view.evaluateJavascript(readingJs(true), null);
            view.evaluateJavascript(AD_JS, null);
        }
    }

    private void toggleReading(TabManager.TabItem tab, boolean on) {
        if (tab == null || tab.url == null || tab.url.isEmpty() || tab.webView == null) {
            toast("Pehle koi page kholo");
            return;
        }
        if (on) {
            readingTabs.add(tab);
            tab.webView.evaluateJavascript(readingJs(true), null);
            tab.webView.evaluateJavascript(AD_JS, null);
            toast("Reading mode ON");
        } else {
            readingTabs.remove(tab);
            tab.webView.evaluateJavascript(readingJs(false), null);
            toast("Reading mode OFF");
        }
    }

    private void reapplyReadingAll() {
        for (TabManager.TabItem t : readingTabs) {
            if (t.webView != null) t.webView.evaluateJavascript(readingJs(true), null);
        }
    }

    private void showReadingStyleDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(10), dp(22), 0);

        TextView fl = new TextView(this);
        fl.setText("Font style");
        fl.setTextSize(13);
        fl.setTypeface(Typeface.DEFAULT_BOLD);
        fl.setTextColor(0xFF80868B);
        box.addView(fl, new LinearLayout.LayoutParams(WRAP, WRAP));

        final RadioGroup rg = new RadioGroup(this);
        for (int i = 0; i < READ_FONT_NAMES.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(READ_FONT_NAMES[i]);
            rb.setId(1000 + i);
            rb.setTextSize(16);
            rb.setTypeface(i == 1 ? Typeface.SERIF : i == 2 ? Typeface.SANS_SERIF : i == 3 ? Typeface.MONOSPACE
                    : i == 4 ? Typeface.create("cursive", Typeface.NORMAL) : Typeface.DEFAULT);
            rg.addView(rb);
        }
        rg.check(1000 + prefs.getInt("read_font", 1));
        rg.setOnCheckedChangeListener((g, id) -> {
            prefs.edit().putInt("read_font", id - 1000).apply();
            reapplyReadingAll();
        });
        box.addView(rg, new LinearLayout.LayoutParams(MATCH, WRAP));

        TextView sl = new TextView(this);
        sl.setText("Text size");
        sl.setTextSize(13);
        sl.setTypeface(Typeface.DEFAULT_BOLD);
        sl.setTextColor(0xFF80868B);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(WRAP, WRAP);
        slp.topMargin = dp(14);
        box.addView(sl, slp);

        LinearLayout sizeRow = new LinearLayout(this);
        sizeRow.setOrientation(LinearLayout.HORIZONTAL);
        sizeRow.setGravity(Gravity.CENTER_VERTICAL);
        final TextView val = new TextView(this);
        val.setText(prefs.getInt("read_size", 110) + "%");
        val.setTextSize(16);
        val.setGravity(Gravity.CENTER);
        TextView minus = new TextView(this);
        minus.setText("A\u2212");
        TextView plus = new TextView(this);
        plus.setText("A+");
        for (TextView b : new TextView[]{minus, plus}) {
            b.setTextSize(18);
            b.setTypeface(Typeface.DEFAULT_BOLD);
            b.setTextColor(BLUE);
            b.setGravity(Gravity.CENTER);
            b.setBackground(roundBg(0x1A2979FF, 12, 0, 0));
        }
        minus.setOnClickListener(v -> {
            int z = Math.max(80, prefs.getInt("read_size", 110) - 10);
            prefs.edit().putInt("read_size", z).apply();
            val.setText(z + "%");
            reapplyReadingAll();
        });
        plus.setOnClickListener(v -> {
            int z = Math.min(200, prefs.getInt("read_size", 110) + 10);
            prefs.edit().putInt("read_size", z).apply();
            val.setText(z + "%");
            reapplyReadingAll();
        });
        sizeRow.addView(minus, new LinearLayout.LayoutParams(dp(64), dp(44)));
        sizeRow.addView(val, new LinearLayout.LayoutParams(dp(80), WRAP));
        sizeRow.addView(plus, new LinearLayout.LayoutParams(dp(64), dp(44)));
        LinearLayout.LayoutParams srp = new LinearLayout.LayoutParams(WRAP, WRAP);
        srp.topMargin = dp(6);
        box.addView(sizeRow, srp);

        new AlertDialog.Builder(this)
                .setTitle("Reading text style")
                .setView(box)
                .setPositiveButton("Done", null)
                .show();
    }

    // ------------------------------------------------------------------ appearance (themes / image / pencil button)

    private Set<String> hiddenDefaults() {
        Set<String> out = new HashSet<>();
        try {
            String js = prefs.getString("hidden_defaults", null);
            if (js == null) return out;
            JSONArray arr = new JSONArray(js);
            for (int i = 0; i < arr.length(); i++) out.add(arr.optString(i));
        } catch (Exception ignored) {
        }
        return out;
    }

    private void hideDefault(String url) {
        Set<String> h = hiddenDefaults();
        h.add(url);
        JSONArray arr = new JSONArray();
        for (String u : h) arr.put(u);
        prefs.edit().putString("hidden_defaults", arr.toString()).apply();
        rebuildShortcuts();
    }

    private void restoreDefaults() {
        prefs.edit().remove("hidden_defaults").apply();
        rebuildShortcuts();
        toast("Hataye hue shortcuts wapas aa gaye");
    }

    private void rebuildHome() {
        homeView = privateMode ? buildPrivateHome() : buildHomeRoot();
        renderActiveTab();
    }

    private void setHomeTheme(int t) {
        prefs.edit().putInt("home_theme", t).putBoolean("home_image", false).apply();
        rebuildHome();
    }

    private View scenePreview(int theme) {
        FrameLayout f = new FrameLayout(this);
        f.addView(new IceScene(this, theme, 0xFFF3F6FF), new FrameLayout.LayoutParams(MATCH, MATCH));
        f.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(14));
            }
        });
        f.setClipToOutline(true);
        return f;
    }

    private View appearanceRow(View leading, String label, boolean chevron, boolean checked, View.OnClickListener l) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(8), 0, dp(8));
        r.setBackground(ripple(false));
        r.addView(leading, new LinearLayout.LayoutParams(dp(84), dp(54)));
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(17);
        t.setTextColor(cText);
        t.setPadding(dp(16), 0, dp(8), 0);
        r.addView(t, new LinearLayout.LayoutParams(0, WRAP, 1f));
        if (chevron) r.addView(new IconView(this, IconView.CHEVRON, 0xFF80868B), new LinearLayout.LayoutParams(dp(20), dp(20)));
        if (checked) {
            TextView ck = new TextView(this);
            ck.setText("\u2713");
            ck.setTextSize(24);
            ck.setTypeface(Typeface.DEFAULT_BOLD);
            ck.setTextColor(0xFF2E7D32);
            r.addView(ck, new LinearLayout.LayoutParams(WRAP, WRAP));
        }
        r.setOnClickListener(l);
        return r;
    }

    private void showAppearanceSheet(final boolean themesPage) {
        if (appearanceDialog != null && appearanceDialog.isShowing()) appearanceDialog.dismiss();
        final Dialog dlg = new Dialog(this);
        appearanceDialog = dlg;
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        float rad = dp(28);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(darkMode ? 0xFF35363A : 0xFFFBF8F1);
        bg.setCornerRadii(new float[]{rad, rad, rad, rad, 0, 0, 0, 0});
        box.setBackground(bg);
        box.setPadding(dp(20), dp(12), dp(20), dp(24));

        View handle = new View(this);
        handle.setBackground(roundBg(0xFFBDBDBD, 3, 0, 0));
        LinearLayout.LayoutParams hl = new LinearLayout.LayoutParams(dp(40), dp(4));
        hl.gravity = Gravity.CENTER_HORIZONTAL;
        box.addView(handle, hl);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.addView(iconButton(IconView.BACK, v -> {
            if (themesPage) showAppearanceSheet(false);
            else dlg.dismiss();
        }), new LinearLayout.LayoutParams(dp(44), dp(44)));
        TextView title = new TextView(this);
        title.setText(themesPage ? "Ice Cool themes" : "Appearance");
        title.setTextSize(22);
        title.setTextColor(cText);
        title.setPadding(dp(10), 0, 0, 0);
        head.addView(title, new LinearLayout.LayoutParams(0, WRAP, 1f));
        LinearLayout.LayoutParams headLp = new LinearLayout.LayoutParams(MATCH, WRAP);
        headLp.topMargin = dp(8);
        box.addView(head, headLp);

        boolean hasImage = prefs.getBoolean("home_image", false);
        int theme = homeTheme();

        if (!themesPage) {
            box.addView(appearanceRow(scenePreview(0), "Ice Cool default", false, !hasImage && theme == 0, v -> {
                dlg.dismiss();
                setHomeTheme(0);
            }));

            FrameLayout up = new FrameLayout(this);
            up.setBackground(roundBg(0xFF2E7D6B, 14, 0, 0));
            up.addView(new IconView(this, IconView.UPLOAD, Color.WHITE), new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.CENTER));
            box.addView(appearanceRow(up, "Upload an image", true, hasImage, v -> {
                dlg.dismiss();
                pickHomeImage();
            }));

            FrameLayout th = new FrameLayout(this);
            GradientDrawable tg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{0xFF00E5FF, 0xFF7C4DFF, 0xFFFF4081});
            tg.setCornerRadius(dp(14));
            th.setBackground(tg);
            th.addView(new IconView(this, IconView.SPARK, Color.WHITE), new FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER));
            box.addView(appearanceRow(th, "Ice Cool themes", true, !hasImage && theme > 0, v -> showAppearanceSheet(true)));

            if (!hiddenDefaults().isEmpty()) {
                FrameLayout rs = new FrameLayout(this);
                rs.setBackground(roundBg(0xFF5C6BC0, 14, 0, 0));
                rs.addView(new IconView(this, IconView.REFRESH, Color.WHITE), new FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER));
                box.addView(appearanceRow(rs, "Restore removed shortcuts", false, false, v -> {
                    dlg.dismiss();
                    restoreDefaults();
                }));
            }
        } else {
            for (int i = 0; i < 5; i++) {
                final int idx = i;
                box.addView(appearanceRow(scenePreview(i), THEME_NAMES[i], false, !hasImage && theme == i, v -> {
                    dlg.dismiss();
                    setHomeTheme(idx);
                }));
            }
        }

        dlg.setContentView(box);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setGravity(Gravity.BOTTOM);
            w.setLayout(MATCH, WRAP);
            w.setDimAmount(0.45f);
        }
        dlg.show();
    }

    private void pickHomeImage() {
        try {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(i, REQ_IMAGE);
        } catch (Exception e) {
            toast("File picker nahi mila");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            final Uri u = data.getData();
            toast("Image laga raha hoon...");
            new Thread(() -> {
                final boolean ok = saveHomeImage(u);
                runOnUiThread(() -> {
                    if (ok) {
                        prefs.edit().putBoolean("home_image", true).apply();
                        rebuildHome();
                        toast("Home background set ho gaya");
                    } else {
                        toast("Image nahi lag payi");
                    }
                });
            }).start();
        }
    }

    private boolean saveHomeImage(Uri uri) {
        InputStream in = null;
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            in = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            int sample = 1;
            while (Math.max(o.outWidth, o.outHeight) / sample > 1600) sample *= 2;
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sample;
            in = getContentResolver().openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            if (b == null) return false;
            FileOutputStream fo = new FileOutputStream(new File(getFilesDir(), "home_bg.jpg"));
            b.compress(Bitmap.CompressFormat.JPEG, 88, fo);
            fo.close();
            return true;
        } catch (Throwable e) {
            return false;
        } finally {
            try {
                if (in != null) in.close();
            } catch (Throwable ignored) {
            }
        }
    }

    // ------------------------------------------------------------------ panels: My downloads / Bookmarks / About us

    private FrameLayout buildPanelOverlay() {
        FrameLayout f = new FrameLayout(this);
        f.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{cSwA, cSwB}));
        f.setClickable(true);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        f.addView(col, new FrameLayout.LayoutParams(MATCH, MATCH));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(10), dp(8), dp(6));
        panelTitle = new TextView(this);
        panelTitle.setTextSize(20);
        panelTitle.setTypeface(Typeface.DEFAULT_BOLD);
        panelTitle.setTextColor(cTitle);
        header.addView(panelTitle, new LinearLayout.LayoutParams(0, WRAP, 1f));
        header.addView(iconButton(IconView.CLOSE, v -> hidePanel()), new LinearLayout.LayoutParams(dp(44), dp(44)));
        col.addView(header, new LinearLayout.LayoutParams(MATCH, WRAP));

        ScrollView sv = new ScrollView(this);
        panelList = new LinearLayout(this);
        panelList.setOrientation(LinearLayout.VERTICAL);
        panelList.setPadding(dp(12), dp(4), dp(12), dp(28));
        sv.addView(panelList, new ViewGroup.LayoutParams(MATCH, WRAP));
        col.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        return f;
    }

    private void showPanel(int mode) {
        if (panelOverlay == null) return;
        panelMode = mode;
        panelTitle.setText(mode == 1 ? "My downloads" : mode == 2 ? "Bookmarks" : "About Ice Cool");
        refreshPanel();
        panelOverlay.setAlpha(0f);
        panelOverlay.setVisibility(View.VISIBLE);
        panelOverlay.animate().alpha(1f).setDuration(150).start();
    }

    private void hidePanel() {
        if (panelOverlay == null || panelOverlay.getVisibility() != View.VISIBLE) return;
        panelMode = 0;
        panelOverlay.animate().alpha(0f).setDuration(120).withEndAction(() -> panelOverlay.setVisibility(View.GONE)).start();
    }

    private void refreshPanel() {
        if (panelList == null) return;
        panelList.removeAllViews();
        dlSubViews.clear();
        dlBars.clear();
        if (panelMode == 1) buildDownloadsPanel();
        else if (panelMode == 2) buildBookmarksPanel();
        else if (panelMode == 3) buildAboutPanel();
    }

    private TextView emptyText(String msg) {
        TextView t = new TextView(this);
        t.setText(msg);
        t.setTextSize(14);
        t.setTextColor(0xFF80868B);
        t.setGravity(Gravity.CENTER);
        t.setLineSpacing(0, 1.2f);
        t.setPadding(dp(24), dp(60), dp(24), dp(24));
        return t;
    }

    private int cardColor() {
        return privateMode ? 0xFF3C4043 : Color.WHITE;
    }

    // ---- My downloads ----

    private void buildDownloadsPanel() {
        if (downloads.isEmpty()) {
            panelList.addView(emptyText("No downloads yet.\nFiles you download will show here. You can open, share or download them again."));
            return;
        }
        for (DlItem d : new ArrayList<>(downloads)) panelList.addView(buildDownloadRow(d));
    }

    private String dlSubtitle(DlItem d) {
        if (d.status == 0) {
            String got = Formatter.formatShortFileSize(this, d.done);
            if (d.size > 0) {
                return (int) (d.done * 100 / d.size) + "%  \u2022  " + got + " of " + Formatter.formatShortFileSize(this, d.size);
            }
            return "Downloading...  " + got;
        }
        if (d.status == 2) return "Failed  \u2022  tap the refresh icon to try again";
        String when = new SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(new Date(d.time));
        String size = Formatter.formatShortFileSize(this, d.size > 0 ? d.size : d.done);
        String host = hostOf(d.url);
        return size + "  \u2022  " + when + (host.isEmpty() ? "" : "  \u2022  " + host);
    }

    private int colorForFile(String mime, String ext) {
        String m = mime == null ? "" : mime;
        String e = ext.toLowerCase(Locale.ROOT);
        if (m.startsWith("image/")) return 0xFF8E24AA;
        if (m.startsWith("video/")) return 0xFFE53935;
        if (m.startsWith("audio/")) return 0xFFFB8C00;
        if (m.contains("pdf") || e.equals("pdf")) return 0xFFD32F2F;
        if (e.equals("apk")) return 0xFF43A047;
        if (e.equals("zip") || e.equals("rar") || e.equals("7z") || e.equals("gz")) return 0xFFF9A825;
        if (m.startsWith("text/") || e.equals("doc") || e.equals("docx") || e.equals("xls") || e.equals("xlsx")
                || e.equals("ppt") || e.equals("pptx")) return 0xFF1E88E5;
        return 0xFF607D8B;
    }

    private View buildDownloadRow(final DlItem d) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(roundBg(cardColor(), 18, 0, 0));
        row.setElevation(dp(2));
        row.setPadding(dp(12), dp(10), dp(6), dp(10));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(MATCH, WRAP);
        rlp.setMargins(dp(4), dp(5), dp(4), dp(5));
        row.setLayoutParams(rlp);

        String ext = extOf(d.name);
        TextView badge = new TextView(this);
        badge.setText(ext.isEmpty() ? "FILE" : ext.toUpperCase(Locale.ROOT));
        badge.setTextSize(10);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextColor(Color.WHITE);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(ovalBg(colorForFile(d.mime, ext), 0, 0));
        row.addView(badge, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.setPadding(dp(12), 0, dp(4), 0);
        TextView name = new TextView(this);
        name.setText(d.name);
        name.setTextSize(14);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextColor(cText);
        name.setMaxLines(2);
        name.setEllipsize(TextUtils.TruncateAt.END);
        mid.addView(name, new LinearLayout.LayoutParams(MATCH, WRAP));
        TextView sub = new TextView(this);
        sub.setText(dlSubtitle(d));
        sub.setTextSize(12);
        sub.setTextColor(d.status == 2 ? 0xFFD93025 : 0xFF80868B);
        mid.addView(sub, new LinearLayout.LayoutParams(MATCH, WRAP));
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setProgressTintList(ColorStateList.valueOf(BLUE));
        if (d.status == 0) {
            if (d.size > 0) {
                bar.setIndeterminate(false);
                bar.setMax(1000);
                bar.setProgress((int) (d.done * 1000 / d.size));
            } else {
                bar.setIndeterminate(true);
            }
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(MATCH, dp(4));
            blp.topMargin = dp(6);
            mid.addView(bar, blp);
        }
        dlSubViews.put(d, sub);
        dlBars.put(d, bar);
        row.addView(mid, new LinearLayout.LayoutParams(0, WRAP, 1f));

        if (d.status == 0) {
            row.addView(iconButton(IconView.CLOSE, 0xFFD93025, v -> {
                d.cancel = true;
                toast("Cancel ho raha hai...");
            }), new LinearLayout.LayoutParams(dp(38), dp(38)));
        } else {
            if (d.status == 1) {
                row.addView(iconButton(IconView.SHARE, 0xFF2979FF, v -> shareDownload(d)), new LinearLayout.LayoutParams(dp(38), dp(38)));
            }
            row.addView(iconButton(IconView.REFRESH, 0xFF00ACC1, v -> redownload(d)), new LinearLayout.LayoutParams(dp(38), dp(38)));
            row.addView(iconButton(IconView.TRASH, 0xFFD93025, v -> confirmDeleteDownload(d)), new LinearLayout.LayoutParams(dp(38), dp(38)));
        }
        row.setOnClickListener(v -> openDownload(d));
        return row;
    }

    private void openDownload(DlItem d) {
        if (d.status != 1 || d.uri.isEmpty()) {
            toast(d.status == 0 ? "Download chal raha hai" : "File nahi hai, refresh dabake dobara download karo");
            return;
        }
        try {
            Uri u = Uri.parse(d.uri);
            if ("file".equals(u.getScheme())) {
                toast("File Downloads folder me hai: " + d.name);
                return;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(u, d.mime.isEmpty() ? "*/*" : d.mime);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            toast("Ye file kholne wala app nahi mila ya file delete ho gayi");
        }
    }

    private void shareDownload(DlItem d) {
        if (d.uri.isEmpty()) return;
        try {
            Uri u = Uri.parse(d.uri);
            if ("file".equals(u.getScheme())) {
                toast("Share ke liye Android 10+ chahiye, file Downloads me hai");
                return;
            }
            Intent s = new Intent(Intent.ACTION_SEND);
            s.setType(d.mime.isEmpty() ? "*/*" : d.mime);
            s.putExtra(Intent.EXTRA_STREAM, u);
            s.setClipData(ClipData.newRawUri(d.name, u));
            s.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(s, "Share " + d.name));
        } catch (Exception e) {
            toast("Share nahi ho paya");
        }
    }

    private void redownload(DlItem old) {
        if (old.url.startsWith("blob:")) {
            toast("Ye file page se bani thi, usi page se dobara download karo");
            return;
        }
        DlItem d = new DlItem();
        d.id = System.currentTimeMillis();
        d.time = d.id;
        d.url = old.url;
        d.ua = old.ua;
        d.referer = old.referer;
        d.mime = old.mime;
        d.name = old.name;
        if (old.status == 2) downloads.remove(old);
        downloads.add(0, d);
        saveDownloads();
        toast("Dobara download shuru: " + d.name);
        WebView src = appMode ? appWebView : (currentTab() != null ? currentTab().webView : null);
        refreshPanel();
        runDownload(d, src);
    }

    private void confirmDeleteDownload(final DlItem d) {
        new AlertDialog.Builder(this)
                .setTitle(d.name)
                .setItems(new String[]{"Remove from list", "Delete file too"}, (dlg, which) -> {
                    if (which == 1) deleteDownloadedFile(d);
                    downloads.remove(d);
                    saveDownloads();
                    refreshPanel();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDownloadedFile(DlItem d) {
        if (d.uri.isEmpty()) return;
        try {
            Uri u = Uri.parse(d.uri);
            if ("file".equals(u.getScheme())) {
                // noinspection ResultOfMethodCallIgnored
                new File(u.getPath()).delete();
            } else {
                getContentResolver().delete(u, null, null);
            }
        } catch (Throwable ignored) {
        }
    }

    // ---- Bookmarks ----

    private List<String[]> loadBookmarks() {
        List<String[]> out = new ArrayList<>();
        try {
            String js = prefs.getString("bookmarks", null);
            if (js == null) return out;
            JSONArray arr = new JSONArray(js);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(new String[]{o.optString("t"), o.optString("u")});
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private void saveBookmarks(List<String[]> list) {
        try {
            JSONArray arr = new JSONArray();
            for (String[] b : list) {
                JSONObject o = new JSONObject();
                o.put("t", b[0]);
                o.put("u", b[1]);
                arr.put(o);
            }
            prefs.edit().putString("bookmarks", arr.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private boolean isBookmarked(String url) {
        for (String[] b : loadBookmarks()) {
            if (b[1].equals(url)) return true;
        }
        return false;
    }

    private void toggleBookmark(TabManager.TabItem tab) {
        if (privateMode) return;
        if (tab == null || tab.url == null || tab.url.isEmpty()) {
            toast("Pehle koi page kholo");
            return;
        }
        List<String[]> list = loadBookmarks();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)[1].equals(tab.url)) {
                list.remove(i);
                saveBookmarks(list);
                toast("Bookmark hata diya");
                return;
            }
        }
        String title = (tab.title != null && !tab.title.isEmpty()) ? tab.title : hostOf(tab.url);
        list.add(0, new String[]{title, tab.url});
        saveBookmarks(list);
        toast("Bookmark save ho gaya");
    }

    private void buildBookmarksPanel() {
        List<String[]> list = loadBookmarks();
        if (list.isEmpty()) {
            panelList.addView(emptyText("No bookmarks yet.\nOpen a page and use Menu \u2192 Add bookmark."));
            return;
        }
        for (final String[] b : list) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(roundBg(cardColor(), 18, 0, 0));
            row.setElevation(dp(2));
            row.setPadding(dp(12), dp(10), dp(6), dp(10));
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(MATCH, WRAP);
            rlp.setMargins(dp(4), dp(5), dp(4), dp(5));
            row.setLayoutParams(rlp);

            ImageView fav = new ImageView(this);
            fav.setBackground(ovalBg(0xFFEEF1FF, 0, 0));
            fav.setPadding(dp(8), dp(8), dp(8), dp(8));
            row.addView(fav, new LinearLayout.LayoutParams(dp(42), dp(42)));
            String host = hostOf(b[1]);
            if (!host.isEmpty()) loadFavicon(host, fav, null);

            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            mid.setPadding(dp(12), 0, dp(4), 0);
            TextView t = new TextView(this);
            t.setText(b[0]);
            t.setTextSize(14);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setTextColor(cText);
            t.setSingleLine(true);
            t.setEllipsize(TextUtils.TruncateAt.END);
            mid.addView(t, new LinearLayout.LayoutParams(MATCH, WRAP));
            TextView u = new TextView(this);
            u.setText(b[1]);
            u.setTextSize(12);
            u.setTextColor(0xFF80868B);
            u.setSingleLine(true);
            u.setEllipsize(TextUtils.TruncateAt.END);
            mid.addView(u, new LinearLayout.LayoutParams(MATCH, WRAP));
            row.addView(mid, new LinearLayout.LayoutParams(0, WRAP, 1f));

            row.addView(iconButton(IconView.TRASH, 0xFFD93025, v -> {
                List<String[]> cur = loadBookmarks();
                for (int i = 0; i < cur.size(); i++) {
                    if (cur.get(i)[1].equals(b[1])) {
                        cur.remove(i);
                        break;
                    }
                }
                saveBookmarks(cur);
                refreshPanel();
            }), new LinearLayout.LayoutParams(dp(38), dp(38)));
            row.setOnClickListener(v -> {
                hidePanel();
                loadUrlInCurrentTab(b[1]);
            });
            panelList.addView(row);
        }
    }

    // ---- About us ----

    private TextView aboutText(String text, int color, float sizeSp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(sizeSp);
        t.setTextColor(color);
        t.setLineSpacing(0, 1.22f);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void addAboutCard(String heading, int accent, String... paragraphs) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundBg(cardColor(), 18, 0, 0));
        card.setElevation(dp(2));
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(MATCH, WRAP);
        clp.setMargins(dp(4), dp(6), dp(4), dp(6));

        TextView h = aboutText(heading, accent, 16, true);
        card.addView(h, new LinearLayout.LayoutParams(MATCH, WRAP));
        int body = privateMode ? 0xFFBDC1C6 : 0xFF5F6368;
        for (String p : paragraphs) {
            TextView t = aboutText(p, body, 13.5f, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);
            lp.topMargin = dp(8);
            card.addView(t, lp);
        }
        panelList.addView(card, clp);
    }

    private void buildAboutPanel() {
        // developer card
        LinearLayout dev = new LinearLayout(this);
        dev.setOrientation(LinearLayout.HORIZONTAL);
        dev.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable dbg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF00B8D4, 0xFF2979FF, 0xFF7C4DFF});
        dbg.setCornerRadius(dp(22));
        dev.setBackground(dbg);
        dev.setElevation(dp(4));
        dev.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(MATCH, WRAP);
        dlp.setMargins(dp(4), dp(6), dp(4), dp(6));

        TextView av = new TextView(this);
        av.setText("AB");
        av.setTextSize(22);
        av.setTypeface(Typeface.DEFAULT_BOLD);
        av.setTextColor(0xFF2979FF);
        av.setGravity(Gravity.CENTER);
        av.setBackground(ovalBg(Color.WHITE, 0, 0));
        dev.addView(av, new LinearLayout.LayoutParams(dp(64), dp(64)));

        LinearLayout who = new LinearLayout(this);
        who.setOrientation(LinearLayout.VERTICAL);
        who.setPadding(dp(16), 0, 0, 0);
        who.addView(aboutText("DEVELOPER", 0xCCFFFFFF, 11, true), new LinearLayout.LayoutParams(WRAP, WRAP));
        who.addView(aboutText("Amrit Bharadwaj", Color.WHITE, 22, true), new LinearLayout.LayoutParams(WRAP, WRAP));
        who.addView(aboutText("Creator & designer of Ice Cool\nGitHub: amritbharadwaj-dev", 0xE6FFFFFF, 12.5f, false),
                new LinearLayout.LayoutParams(WRAP, WRAP));
        dev.addView(who, new LinearLayout.LayoutParams(0, WRAP, 1f));
        panelList.addView(dev, dlp);

        addAboutCard("About Ice Cool", 0xFF2979FF,
                "Ice Cool is a fast, clean and colourful browser built on the Android System WebView (Chromium engine). "
                        + "It gives you a Chrome-like experience: a tab strip with up to 25 tabs, a tab grid, per-site page zoom, "
                        + "desktop site mode, bookmarks, an in-app download manager, a built-in ad blocker, a reading mode, dark mode, 5 home themes and a private window.",
                "You can pin any site to the Ice Cool home page, or add it to your phone's home screen. Sites that are "
                        + "installable web apps (manifest + service worker) open full screen, without an address bar.",
                "Version 1.0");

        addAboutCard("What Ice Cool saves by default", 0xFF00ACC1,
                "In a normal window Ice Cool keeps: cookies and site data (so you stay logged in), cache (so pages load faster), "
                        + "your open tabs (restored the next time you open the app), bookmarks, pinned home shortcuts, your downloads list, "
                        + "per-site zoom and the ad blocker setting.",
                "Ice Cool has no separate browsing-history page, no password manager and no account or sync. Everything stays on "
                        + "your phone. The developer cannot see your data.",
                "One thing to know: site icons shown on tabs, bookmarks and home are fetched from Google's favicon service, which "
                        + "tells Google the site name. Private windows never use it.");

        addAboutCard("Why do we save cookies?", 0xFFFFA000,
                "Cookies are tiny files a website stores on your device. They remember who you are and what you chose: your login, "
                        + "language, shopping cart and settings, so you don't have to sign in again on every visit.",
                "Without cookies most sites would forget you on every page. Cookies can also be used for tracking, which is why "
                        + "the ad blocker and the private window exist.");

        addAboutCard("Clear cookies & data in one go", 0xFFE53935,
                "Menu \u2192 Clear all cookies & data removes cookies, site data, cache and form data for every site at once.",
                "To clear only one site: long-press its tab (or tap the \u22EE on the tab card) \u2192 Clear this site's data & cookies.",
                "Or simply use a Private window, which deletes everything when you close it.");

        addAboutCard("Private window", 0xFF8E24AA,
                "Menu \u2192 New private window opens a separate dark window. Ice Cool won't save its browsing history, cookies and "
                        + "site data, or information typed in forms. Third-party cookies are blocked and the cache is switched off.",
                "Screenshots and screen recording are blocked, and the window shows blank in the recent apps list.",
                "Closing the last private tab, choosing Close private window, or pressing back on the last tab deletes the "
                        + "whole session. If you only leave the app, the private tabs stay until you come back. If you swipe it away "
                        + "from recent apps, everything is cleared.",
                "Ad blocker is ON by default in a private window, so no ads will load there. Files you download are still saved "
                        + "to your Downloads folder, like Chrome.",
                "Private browsing does not make you invisible: websites, your school or employer, and your internet provider can "
                        + "still see your activity.");

        addAboutCard("Ad blocker", 0xFF00C853,
                "Turn it on from the menu. It blocks requests to well-known ad and tracking servers, stops ad popups and redirects, "
                        + "hides ad boxes on pages and tries to skip YouTube video ads.",
                "No ad blocker can stop every ad. Some ads are served from the same place as the content, and sites change often, "
                        + "so a few ads may still slip through. If a site misbehaves, switch the ad blocker off for a moment.");

        addAboutCard("Downloads", 0xFF0F9D58,
                "Menu \u2192 My downloads lists everything you've downloaded. Tap a file to open it, use the share icon to send it, "
                        + "the refresh icon to download it again, and the bin to remove it. Files are saved in your phone's "
                        + "Downloads folder.");

        TextView foot = aboutText("Made with \u2744 by Amrit Bharadwaj", 0xFF80868B, 12, false);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, dp(14), 0, dp(10));
        panelList.addView(foot, new LinearLayout.LayoutParams(MATCH, WRAP));
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

    private TabManager.TabItem openTab(String url, boolean load) {
        if (tabManager.getTabs().size() >= MAX_TABS) {
            toast("Max " + MAX_TABS + " tabs allowed");
            return null;
        }
        WebView webView = new WebView(this);
        setupWebView(webView);

        TabManager.TabItem tab = new TabManager.TabItem(url, url.isEmpty() ? "New Tab" : url, webView);
        tabManager.addTab(tab);
        tabManager.setActiveTabIdx(tabManager.getTabs().size() - 1);

        if (load && !url.isEmpty()) webView.loadUrl(url);
        renderActiveTab();
        return tab;
    }

    private void createNewTab(String url) {
        openTab(url, true);
    }

    private void destroyTab(TabManager.TabItem t) {
        thumbs.remove(t);
        desktopTabs.remove(t);
        popupTabs.remove(t);
        lazyTabs.remove(t);
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
            if (privateMode) {
                exitPrivateWindow();
                return;
            }
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
        if (privateMode) {
            exitPrivateWindow();
            return;
        }
        for (TabManager.TabItem t : new ArrayList<>(tabManager.getTabs())) destroyTab(t);
        thumbs.clear();
        tabManager.clearAllTabs();
        createNewTab("");
    }

    private void captureCurrentThumb() {
        if (webContainer == null) return;
        for (int i = 0; i < webContainer.getChildCount(); i++) {
            View c = webContainer.getChildAt(i);
            if (c instanceof WebView && c.getWidth() > 0 && c.getHeight() > 0) {
                TabManager.TabItem t = findTab((WebView) c);
                if (t == null) continue;
                try {
                    Bitmap b = Bitmap.createBitmap(c.getWidth() * 4 / 10, c.getHeight() * 4 / 10, Bitmap.Config.RGB_565);
                    Canvas cv = new Canvas(b);
                    cv.scale(0.4f, 0.4f);
                    c.draw(cv);
                    thumbs.put(t, b);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private void renderActiveTab() {
        dismissUrlPopup();
        hideFindBar();
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
            tab.webView.setVisibility(View.VISIBLE);
            try {
                tab.webView.onResume();
            } catch (Throwable ignored) {
            }
            if (lazyTabs.remove(tab)) tab.webView.loadUrl(tab.url); // restored tab: load on first open
            tab.webView.requestLayout();
            etSearchUrl.setText(tab.url);
        }
        refreshTabStrip(true);
        updateUrlStar();
    }

    private void loadUrlInCurrentTab(String url) {
        TabManager.TabItem tab = currentTab();
        if (tab == null) {
            createNewTab(url);
            return;
        }
        tab.url = url;
        tab.title = url;
        lazyTabs.remove(tab);
        thumbs.remove(tab);
        tab.webView.loadUrl(url);
        renderActiveTab();
    }

    /** Home never wipes a page tab: it opens (or reuses) a separate empty home tab, like Chrome's new tab. */
    private void navigateHome() {
        TabManager.TabItem cur = currentTab();
        if (cur != null && (cur.url == null || cur.url.isEmpty())) {
            renderActiveTab();
            return;
        }
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        for (int i = 0; i < tabs.size(); i++) {
            String u = tabs.get(i).url;
            if (u == null || u.isEmpty()) {
                tabManager.setActiveTabIdx(i);
                renderActiveTab();
                return;
            }
        }
        createNewTab("");
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
        s.setSupportMultipleWindows(!appMode);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setCacheMode(privateMode ? WebSettings.LOAD_NO_CACHE : WebSettings.LOAD_DEFAULT);
        if (privateMode) s.setSaveFormData(false);
        if (mobileUA != null) s.setUserAgentString(mobileUA);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                s.setForceDark((privateMode || darkMode) ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
            } catch (Throwable ignored) {
            }
        }

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, !privateMode);

        webView.addJavascriptInterface(new PwaBridge(), "IceBridge");
        webView.setFindListener((active, matches, done) -> {
            if (findCount != null && isActive(webView)) {
                findCount.setText(matches == 0 ? "0/0" : (active + 1) + "/" + matches);
            }
        });
        webView.setDownloadListener((url, ua, cd, mime, len) -> startDownload(url, ua, cd, mime));

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (adBlockEnabled && !request.isForMainFrame() && isAdUrl(request.getUrl())) {
                    blockedCount.incrementAndGet();
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (adBlockEnabled && isAdUrl(Uri.parse(url))) {
                    blockedCount.incrementAndGet();
                    return true; // blocks ad redirects / popunders
                }
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
                if (adBlockEnabled) view.evaluateJavascript(AD_JS, null);
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
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                applyZoom(view, url);
                applyReading(view);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                TabManager.TabItem t = findTab(view);
                if (t != null && !t.url.isEmpty()) t.url = url;
                applyZoom(view, url);
                if (isActive(view)) {
                    showProgress(100);
                    if (t != null && !t.url.isEmpty() && etSearchUrl != null && !etSearchUrl.hasFocus()) {
                        etSearchUrl.setText(url);
                    }
                }
                CookieManager.getInstance().flush();
                if (adBlockEnabled) view.evaluateJavascript(AD_JS, null);
                applyReading(view);
                if (isActive(view)) updateUrlStar();
                if (appMode && view == appWebView) view.evaluateJavascript(THEME_JS, null);
                scheduleStripRefresh();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (isActive(view)) showProgress(newProgress);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                String h = hostOf(view.getUrl() == null ? "" : view.getUrl());
                if (icon != null && !h.isEmpty()) {
                    faviconCache.put(h, icon);
                    scheduleStripRefresh();
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                TabManager.TabItem t = findTab(view);
                if (t != null && title != null && !title.isEmpty()) {
                    t.title = title;
                    scheduleStripRefresh();
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                if (appMode || !isUserGesture) return false; // blocks popup ads
                TabManager.TabItem nt = openTab("about:blank", false);
                if (nt == null) return false;
                popupTabs.add(nt);
                WebView.WebViewTransport tr = (WebView.WebViewTransport) resultMsg.obj;
                tr.setWebView(nt.webView);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public void onCloseWindow(WebView window) {
                TabManager.TabItem t = findTab(window);
                if (t != null) {
                    int idx = tabManager.getTabs().indexOf(t);
                    if (idx >= 0) closeTab(idx);
                }
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

    // ------------------------------------------------------------------ own download manager

    private static class DlItem {
        long id;
        String name = "";
        String url = "";
        String mime = "";
        String uri = "";
        String ua = "";
        String referer = "";
        String payload = null; // data: URL for blob downloads
        long size = -1;
        volatile long done = 0;
        volatile int status = 0; // 0 running, 1 done, 2 failed
        volatile boolean cancel = false;
        long time;
    }

    private void loadDownloads() {
        try {
            String js = prefs.getString("downloads", null);
            if (js == null) return;
            JSONArray arr = new JSONArray(js);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                DlItem d = new DlItem();
                d.id = o.optLong("id");
                d.name = o.optString("n");
                d.url = o.optString("u");
                d.mime = o.optString("m");
                d.uri = o.optString("f");
                d.ua = o.optString("a");
                d.referer = o.optString("r");
                d.size = o.optLong("s", -1);
                d.done = o.optLong("d");
                d.status = o.optInt("st");
                d.time = o.optLong("t");
                if (d.status == 0) d.status = 2; // was interrupted
                downloads.add(d);
            }
        } catch (Exception ignored) {
        }
    }

    private void saveDownloads() {
        if (privateMode) return;
        try {
            JSONArray arr = new JSONArray();
            for (DlItem d : downloads) {
                JSONObject o = new JSONObject();
                o.put("id", d.id);
                o.put("n", d.name);
                o.put("u", d.url);
                o.put("m", d.mime);
                o.put("f", d.uri);
                o.put("a", d.ua);
                o.put("r", d.referer);
                o.put("s", d.size);
                o.put("d", d.done);
                o.put("st", d.status);
                o.put("t", d.time);
                arr.put(o);
            }
            prefs.edit().putString("downloads", arr.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private String sanitizeName(String n) {
        if (n == null) n = "";
        n = n.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return n.isEmpty() ? "download" : n;
    }

    private String extOf(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || name.length() - i - 1 > 5) return "";
        return name.substring(i + 1);
    }

    private void startDownload(String url, String ua, String cd, String mime) {
        if (url == null || url.isEmpty()) return;
        DlItem d = new DlItem();
        d.id = System.currentTimeMillis();
        d.time = d.id;
        d.url = url;
        d.ua = ua == null ? "" : ua;
        d.mime = mime == null ? "" : mime;
        d.name = sanitizeName(URLUtil.guessFileName(url, cd, mime));
        WebView src = appMode ? appWebView : (currentTab() != null ? currentTab().webView : null);
        d.referer = (src != null && src.getUrl() != null) ? src.getUrl() : "";
        downloads.add(0, d);
        saveDownloads();
        toast("Download shuru: " + d.name);
        runDownload(d, src);
    }

    private void runDownload(final DlItem d, WebView src) {
        d.status = 0;
        d.done = 0;
        d.cancel = false;
        postPanelRefresh();
        if (d.url.startsWith("blob:")) {
            if (src == null) {
                finishDownload(d, false);
                return;
            }
            final String token = UUID.randomUUID().toString();
            blobTokens.put(token, d);
            String js = "(function(){var T='" + token + "';function bad(){try{IceBridge.onBlobFail(T);}catch(e){}}"
                    + "fetch('" + d.url.replace("'", "\\'") + "').then(function(r){return r.blob();}).then(function(b){"
                    + "var rd=new FileReader();rd.onloadend=function(){try{IceBridge.onBlobData(T,rd.result);}catch(e){bad();}};"
                    + "rd.onerror=bad;rd.readAsDataURL(b);}).catch(bad);})();";
            src.evaluateJavascript(js, null);
            return;
        }
        new Thread(() -> downloadWorker(d)).start();
    }

    private void downloadWorker(final DlItem d) {
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection con = null;
        Uri outUri = null;
        File outFile = null;
        boolean ok = false;
        try {
            String mime = d.mime == null ? "" : d.mime;
            long total = -1;
            String source = d.payload != null ? d.payload : d.url;
            if (source.startsWith("data:")) {
                int comma = source.indexOf(',');
                String meta = source.substring(5, comma);
                String payload = source.substring(comma + 1);
                byte[] bytes = meta.contains(";base64") ? Base64.decode(payload, Base64.DEFAULT)
                        : Uri.decode(payload).getBytes("UTF-8");
                String m = meta.split(";")[0];
                if (!m.isEmpty()) mime = m;
                in = new ByteArrayInputStream(bytes);
                total = bytes.length;
            } else {
                String cur = d.url;
                for (int hop = 0; hop < 6; hop++) {
                    con = (HttpURLConnection) new URL(cur).openConnection();
                    con.setInstanceFollowRedirects(false);
                    con.setConnectTimeout(15000);
                    con.setReadTimeout(30000);
                    if (!d.ua.isEmpty()) con.setRequestProperty("User-Agent", d.ua);
                    String ck = CookieManager.getInstance().getCookie(cur);
                    if (ck != null) con.setRequestProperty("Cookie", ck);
                    if (!d.referer.isEmpty()) con.setRequestProperty("Referer", d.referer);
                    int code = con.getResponseCode();
                    String loc = con.getHeaderField("Location");
                    if (code >= 300 && code < 400 && loc != null) {
                        cur = new URL(new URL(cur), loc).toString();
                        con.disconnect();
                        continue;
                    }
                    break;
                }
                int code = con.getResponseCode();
                if (code < 200 || code >= 300) throw new IOException("HTTP " + code);
                String ct = con.getContentType();
                if (ct != null) {
                    String m = ct.split(";")[0].trim();
                    if (!m.isEmpty() && (mime.isEmpty() || mime.equals("application/octet-stream"))) mime = m;
                }
                String cd = con.getHeaderField("Content-Disposition");
                if (cd != null) d.name = sanitizeName(URLUtil.guessFileName(cur, cd, mime));
                total = con.getContentLengthLong();
                in = con.getInputStream();
            }
            d.mime = mime.isEmpty() ? "application/octet-stream" : mime;
            if (d.name.endsWith(".bin") && !d.mime.equals("application/octet-stream")) {
                String e = MimeTypeMap.getSingleton().getExtensionFromMimeType(d.mime);
                if (e != null) d.name = d.name.substring(0, d.name.length() - 4) + "." + e;
            }
            d.size = total;

            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Downloads.DISPLAY_NAME, d.name);
                cv.put(MediaStore.Downloads.MIME_TYPE, d.mime);
                cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                cv.put(MediaStore.Downloads.IS_PENDING, 1);
                outUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                if (outUri == null) throw new IOException("Cannot create file");
                out = getContentResolver().openOutputStream(outUri);
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                dir.mkdirs();
                outFile = new File(dir, d.name);
                int n = 1;
                while (outFile.exists()) {
                    String e = extOf(d.name);
                    String base = e.isEmpty() ? d.name : d.name.substring(0, d.name.length() - e.length() - 1);
                    outFile = new File(dir, base + " (" + (n++) + ")" + (e.isEmpty() ? "" : "." + e));
                }
                out = new FileOutputStream(outFile);
            }

            byte[] buf = new byte[32768];
            int read;
            long done = 0;
            long last = 0;
            while ((read = in.read(buf)) != -1) {
                if (d.cancel) throw new IOException("cancelled");
                out.write(buf, 0, read);
                done += read;
                d.done = done;
                long now = SystemClock.uptimeMillis();
                if (now - last > 350) {
                    last = now;
                    postPanelTick();
                }
            }
            out.flush();
            out.close();
            out = null;
            if (outUri != null) {
                ContentValues cv2 = new ContentValues();
                cv2.put(MediaStore.Downloads.IS_PENDING, 0);
                getContentResolver().update(outUri, cv2, null, null);
                d.uri = outUri.toString();
            } else if (outFile != null) {
                d.uri = Uri.fromFile(outFile).toString();
            }
            ok = true;
        } catch (Throwable e) {
            if (outUri != null) {
                try {
                    getContentResolver().delete(outUri, null, null);
                } catch (Throwable ignored) {
                }
            }
            if (outFile != null) {
                // noinspection ResultOfMethodCallIgnored
                outFile.delete();
            }
        } finally {
            try {
                if (in != null) in.close();
            } catch (Throwable ignored) {
            }
            try {
                if (out != null) out.close();
            } catch (Throwable ignored) {
            }
            if (con != null) con.disconnect();
            d.payload = null;
        }
        final boolean result = ok;
        runOnUiThread(() -> finishDownload(d, result));
    }

    private void finishDownload(DlItem d, boolean ok) {
        d.status = ok ? 1 : 2;
        if (d.cancel) downloads.remove(d);
        saveDownloads();
        refreshPanelIfDownloads();
        if (d.cancel) toast("Download cancel ho gaya");
        else if (ok) toast("Download complete: " + d.name + " (Downloads folder me)");
        else toast("Download fail hua: " + d.name + " \u2014 My downloads se dobara try karo");
    }

    private void refreshPanelIfDownloads() {
        if (panelMode == 1 && panelOverlay != null && panelOverlay.getVisibility() == View.VISIBLE) refreshPanel();
    }

    private void postPanelRefresh() {
        runOnUiThread(this::refreshPanelIfDownloads);
    }

    private void postPanelTick() {
        runOnUiThread(() -> {
            if (panelMode != 1) return;
            for (Map.Entry<DlItem, TextView> e : dlSubViews.entrySet()) {
                DlItem d = e.getKey();
                e.getValue().setText(dlSubtitle(d));
                ProgressBar bar = dlBars.get(d);
                if (bar != null && d.size > 0) {
                    bar.setIndeterminate(false);
                    bar.setMax(1000);
                    bar.setProgress((int) (d.done * 1000 / d.size));
                }
            }
        });
    }

    private void loadFavicon(final String domain, final ImageView iv, final View fallback) {
        Bitmap cached = faviconCache.get(domain);
        if (cached != null) {
            iv.setImageBitmap(cached);
            if (fallback != null) fallback.setVisibility(View.GONE);
            return;
        }
        if (privateMode) return;
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
        if (menuPopup != null && menuPopup.isShowing()) {
            menuPopup.dismiss();
            return;
        }
        if (findBar != null && findBar.getVisibility() == View.VISIBLE) {
            hideFindBar();
            return;
        }
        if (urlPopup != null && urlPopup.isShowing()) {
            dismissUrlPopup();
            etSearchUrl.clearFocus();
            return;
        }
        if (panelOverlay != null && panelOverlay.getVisibility() == View.VISIBLE) {
            hidePanel();
            return;
        }
        if (tabSwitcher.getVisibility() == View.VISIBLE) {
            hideTabSwitcher();
            return;
        }
        TabManager.TabItem t = currentTab();
        if (t != null && !t.url.isEmpty() && t.webView != null) {
            if (t.webView.canGoBack()) {
                t.webView.goBack();
                return;
            }
            if (popupTabs.contains(t) && tabManager.getTabs().size() > 1) {
                closeTab(tabManager.getActiveTabIdx());
                return;
            }
        }
        if (privateMode) {
            if (tabManager.getTabs().size() > 1) closeTab(tabManager.getActiveTabIdx());
            else exitPrivateWindow();
            return;
        }
        super.onBackPressed();
    }

    // ------------------------------------------------------------------ custom views

    /** Simple vector-style icons drawn with Canvas (no drawable files needed). */
    static class IconView extends View {
        static final int HOME = 0, REFRESH = 1, DOTS = 2, PLUS = 3, CLOSE = 4, MINUS = 5,
                DOWNLOAD = 6, TRASH = 7, SEARCH = 8, CLOSEALL = 9, INSTALL = 10, DESKTOP = 11, PIN = 12, PRIVATE = 13, STAR = 14, STARF = 15, SHIELD = 16, INFO = 17, LIST = 18, SHARE = 19, BACK = 20, CHEVRON = 21, UPLOAD = 22, PENCIL = 23, MOON = 24, SUN = 25,
                BOOK = 26, FONT = 27, SPARK = 28, COPY = 29;
        private int type;
        private int color;

        void set(int newType, int newColor) {
            type = newType;
            color = newColor;
            invalidate();
        }
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
                case PRIVATE:
                    path.moveTo(7, 11);
                    path.lineTo(9, 4.5f);
                    path.lineTo(15, 4.5f);
                    path.lineTo(17, 11);
                    c.drawPath(path, p);
                    c.drawLine(3.5f, 11, 20.5f, 11, p);
                    c.drawCircle(8, 16.5f, 2.6f, p);
                    c.drawCircle(16, 16.5f, 2.6f, p);
                    c.drawLine(10.6f, 16, 13.4f, 16, p);
                    break;
                case BACK:
                    c.drawLine(20, 12, 5, 12, p);
                    path.moveTo(11, 6);
                    path.lineTo(5, 12);
                    path.lineTo(11, 18);
                    c.drawPath(path, p);
                    break;
                case CHEVRON:
                    path.moveTo(9, 5);
                    path.lineTo(16, 12);
                    path.lineTo(9, 19);
                    c.drawPath(path, p);
                    break;
                case UPLOAD:
                    c.drawLine(12, 16, 12, 5, p);
                    path.moveTo(7, 10);
                    path.lineTo(12, 5);
                    path.lineTo(17, 10);
                    c.drawPath(path, p);
                    path.reset();
                    path.moveTo(5, 15);
                    path.lineTo(5, 20);
                    path.lineTo(19, 20);
                    path.lineTo(19, 15);
                    c.drawPath(path, p);
                    break;
                case PENCIL:
                    path.moveTo(4, 20);
                    path.lineTo(5.2f, 15);
                    path.lineTo(16.2f, 4);
                    path.lineTo(20, 7.8f);
                    path.lineTo(9, 18.8f);
                    path.close();
                    c.drawPath(path, p);
                    c.drawLine(13.5f, 6.7f, 17.3f, 10.5f, p);
                    break;
                case MOON: {
                    Path a = new Path();
                    a.addCircle(12, 12, 8.5f, Path.Direction.CW);
                    Path b = new Path();
                    b.addCircle(16.5f, 8.5f, 7f, Path.Direction.CW);
                    a.op(b, Path.Op.DIFFERENCE);
                    p.setStyle(Paint.Style.FILL);
                    c.drawPath(a, p);
                    break;
                }
                case SUN:
                    c.drawCircle(12, 12, 4.2f, p);
                    for (int i = 0; i < 8; i++) {
                        double ang = Math.PI / 4 * i;
                        float cs = (float) Math.cos(ang);
                        float sn = (float) Math.sin(ang);
                        c.drawLine(12 + 7.2f * cs, 12 + 7.2f * sn, 12 + 9.6f * cs, 12 + 9.6f * sn, p);
                    }
                    break;
                case BOOK:
                    path.moveTo(12, 6);
                    path.cubicTo(10, 4.5f, 6.5f, 4.3f, 3, 5);
                    path.lineTo(3, 18.5f);
                    path.cubicTo(6.5f, 17.8f, 10, 18, 12, 19.5f);
                    path.cubicTo(14, 18, 17.5f, 17.8f, 21, 18.5f);
                    path.lineTo(21, 5);
                    path.cubicTo(17.5f, 4.3f, 14, 4.5f, 12, 6);
                    c.drawPath(path, p);
                    c.drawLine(12, 6, 12, 19.5f, p);
                    break;
                case FONT:
                    path.moveTo(3, 19);
                    path.lineTo(8, 6);
                    path.lineTo(13, 19);
                    c.drawPath(path, p);
                    c.drawLine(4.8f, 14.5f, 11.2f, 14.5f, p);
                    c.drawCircle(17.4f, 16.2f, 2.6f, p);
                    c.drawLine(20f, 13.6f, 20f, 19f, p);
                    break;
                case COPY:
                    c.drawRoundRect(new RectF(8, 8, 20, 21), 2.5f, 2.5f, p);
                    path.moveTo(16, 8);
                    path.lineTo(16, 5.5f);
                    path.quadTo(16, 3.5f, 14, 3.5f);
                    path.lineTo(6.5f, 3.5f);
                    path.quadTo(4, 3.5f, 4, 6);
                    path.lineTo(4, 14.5f);
                    path.quadTo(4, 17, 6.5f, 17);
                    path.lineTo(8, 17);
                    c.drawPath(path, p);
                    break;
                case SPARK:
                    path.moveTo(12, 2.5f);
                    path.lineTo(14.4f, 9.6f);
                    path.lineTo(21.5f, 12);
                    path.lineTo(14.4f, 14.4f);
                    path.lineTo(12, 21.5f);
                    path.lineTo(9.6f, 14.4f);
                    path.lineTo(2.5f, 12);
                    path.lineTo(9.6f, 9.6f);
                    path.close();
                    p.setStyle(Paint.Style.FILL);
                    c.drawPath(path, p);
                    break;
                case STAR:
                case STARF: {
                    Path st = new Path();
                    st.moveTo(12f, 2.5f);
                    st.lineTo(14.35f, 8.76f);
                    st.lineTo(21.04f, 9.06f);
                    st.lineTo(15.8f, 13.24f);
                    st.lineTo(17.59f, 19.69f);
                    st.lineTo(12f, 16f);
                    st.lineTo(6.41f, 19.69f);
                    st.lineTo(8.2f, 13.24f);
                    st.lineTo(2.96f, 9.06f);
                    st.lineTo(9.65f, 8.76f);
                    st.close();
                    if (type == STARF) p.setStyle(Paint.Style.FILL_AND_STROKE);
                    c.drawPath(st, p);
                    break;
                }
                case SHIELD:
                    path.moveTo(12, 3);
                    path.lineTo(19, 6);
                    path.lineTo(19, 12);
                    path.cubicTo(19, 16.5f, 16, 19.8f, 12, 21);
                    path.cubicTo(8, 19.8f, 5, 16.5f, 5, 12);
                    path.lineTo(5, 6);
                    path.close();
                    c.drawPath(path, p);
                    path.reset();
                    path.moveTo(8.8f, 12f);
                    path.lineTo(11f, 14.2f);
                    path.lineTo(15.2f, 9.8f);
                    c.drawPath(path, p);
                    break;
                case INFO:
                    c.drawCircle(12, 12, 9, p);
                    c.drawLine(12, 11, 12, 17, p);
                    p.setStyle(Paint.Style.FILL);
                    c.drawCircle(12, 7.6f, 1.3f, p);
                    break;
                case LIST:
                    c.drawLine(9, 7, 20, 7, p);
                    c.drawLine(9, 12, 20, 12, p);
                    c.drawLine(9, 17, 20, 17, p);
                    p.setStyle(Paint.Style.FILL);
                    c.drawCircle(5, 7, 1.4f, p);
                    c.drawCircle(5, 12, 1.4f, p);
                    c.drawCircle(5, 17, 1.4f, p);
                    break;
                case SHARE:
                    c.drawCircle(18, 5, 2.3f, p);
                    c.drawCircle(6, 12, 2.3f, p);
                    c.drawCircle(18, 19, 2.3f, p);
                    c.drawLine(8.1f, 10.9f, 15.9f, 6.1f, p);
                    c.drawLine(8.1f, 13.1f, 15.9f, 17.9f, p);
                    break;
                case DESKTOP:
                    c.drawRoundRect(new RectF(3, 4, 21, 16), 2, 2, p);
                    c.drawLine(8, 20, 16, 20, p);
                    c.drawLine(12, 16, 12, 20, p);
                    break;
                case PIN:
                    c.drawCircle(12, 9, 5, p);
                    c.drawLine(12, 14, 12, 21, p);
                    p.setStyle(Paint.Style.FILL);
                    c.drawCircle(12, 9, 1.6f, p);
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

    /** Hat + glasses badge for the private window page. */
    static class PrivateBadge extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        PrivateBadge(Context c) {
            super(c);
        }

        @Override
        protected void onDraw(Canvas c) {
            float sc = Math.min(getWidth(), getHeight()) / 100f;
            c.scale(sc, sc);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFE8EAED);
            c.drawCircle(50, 50, 50, p);
            p.setColor(0xFF3C4043);
            Path crown = new Path();
            crown.moveTo(30, 44);
            crown.lineTo(36, 21);
            crown.lineTo(64, 21);
            crown.lineTo(70, 44);
            crown.close();
            c.drawPath(crown, p);
            c.drawRoundRect(new RectF(17, 42, 83, 50), 4, 4, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5f);
            c.drawCircle(36, 66, 9, p);
            c.drawCircle(64, 66, 9, p);
            c.drawLine(45, 64, 55, 64, p);
        }
    }

    /** Home illustrations: 5 Ice Cool themes (different shapes + colours). */
    static class IceScene extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int theme;
        private final int ground;
        private final float[] snow = {0.08f, 0.12f, 0.25f, 0.2f, 0.4f, 0.08f, 0.55f, 0.22f, 0.7f, 0.1f,
                0.9f, 0.35f, 0.05f, 0.45f, 0.35f, 0.38f, 0.62f, 0.45f, 0.95f, 0.12f, 0.18f, 0.3f};

        IceScene(Context c, int theme, int ground) {
            super(c);
            this.theme = theme;
            this.ground = ground;
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight(), u = h / 100f;
            switch (theme) {
                case 1:
                    sunset(c, w, h);
                    break;
                case 2:
                    aurora(c, w, h);
                    break;
                case 3:
                    forest(c, w, h);
                    break;
                case 4:
                    ocean(c, w, h);
                    break;
                default:
                    glacier(c, w, h);
                    break;
            }
            p.setStyle(Paint.Style.FILL);
            // soft fade so the picture melts into the page background
            p.setShader(new LinearGradient(0, h * 0.8f, 0, h, ground & 0x00FFFFFF, ground, Shader.TileMode.CLAMP));
            c.drawRect(0, h * 0.8f, w, h, p);
            p.setShader(null);
            if (theme != 2) {
                p.setColor(0xFFFFFFFF);
                for (int i = 0; i + 1 < snow.length; i += 2) {
                    c.drawCircle(snow[i] * w, snow[i + 1] * h, (1 + (i % 3)) * u * 0.7f, p);
                }
            }
        }

        private void sky(Canvas c, float w, float h, int a, int b, int d) {
            p.setShader(new LinearGradient(0, 0, 0, h, new int[]{a, b, d}, new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);
        }

        private void groundBlock(Canvas c, float w, float h) {
            p.setColor(ground);
            c.drawRect(0, h * 0.84f, w, h, p);
            c.drawOval(new RectF(-w * 0.1f, h * 0.78f, w * 0.65f, h * 1.15f), p);
            c.drawOval(new RectF(w * 0.45f, h * 0.8f, w * 1.1f, h * 1.15f), p);
        }

        private void pine(Canvas c, float x, float base, float ht, int color) {
            p.setColor(0xFF6D4C41);
            c.drawRect(x - ht * 0.04f, base - ht * 0.14f, x + ht * 0.04f, base, p);
            for (int k = 0; k < 3; k++) {
                float tip = base - ht + k * 0.26f * ht;
                float bot = tip + 0.46f * ht;
                float hw = (0.17f + 0.08f * k) * ht;
                Path t = new Path();
                t.moveTo(x, tip);
                t.lineTo(x - hw, bot);
                t.lineTo(x + hw, bot);
                t.close();
                p.setColor(color);
                c.drawPath(t, p);
                Path cap = new Path();
                float cy = tip + (bot - tip) * 0.42f;
                cap.moveTo(x, tip);
                cap.lineTo(x - hw * 0.42f, cy);
                cap.lineTo(x + hw * 0.42f, cy);
                cap.close();
                p.setColor(0xFFFFFFFF);
                c.drawPath(cap, p);
            }
        }

        // ---- theme 0: Glacier (iceberg + penguin) ----
        private void glacier(Canvas c, float w, float h) {
            float u = h / 100f;
            sky(c, w, h, 0xFF8FE3FF, 0xFFC4B5FF, 0xFFFFD9EC);
            p.setColor(0x66FFF59D);
            c.drawCircle(w * 0.8f, h * 0.28f, 17 * u, p);
            p.setColor(0xFFFFE57F);
            c.drawCircle(w * 0.8f, h * 0.28f, 10 * u, p);

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

            groundBlock(c, w, h);

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
        }

        // ---- theme 1: Sunset Peaks (big sun, rolling hills, snowman) ----
        private void sunset(Canvas c, float w, float h) {
            float u = h / 100f;
            sky(c, w, h, 0xFF7E57C2, 0xFFFF7043, 0xFFFFD180);
            p.setColor(0x44FFE082);
            c.drawCircle(w * 0.5f, h * 0.66f, 38 * u, p);
            p.setColor(0xFFFFF3C4);
            c.drawCircle(w * 0.5f, h * 0.66f, 27 * u, p);

            Path m1 = new Path();
            m1.moveTo(0, h * 0.8f);
            m1.quadTo(w * 0.22f, h * 0.42f, w * 0.45f, h * 0.7f);
            m1.quadTo(w * 0.7f, h * 0.95f, w, h * 0.55f);
            m1.lineTo(w, h);
            m1.lineTo(0, h);
            m1.close();
            p.setColor(0xFF6A1B9A);
            c.drawPath(m1, p);
            Path m2 = new Path();
            m2.moveTo(0, h * 0.9f);
            m2.quadTo(w * 0.3f, h * 0.62f, w * 0.6f, h * 0.86f);
            m2.quadTo(w * 0.82f, h * 1.0f, w, h * 0.78f);
            m2.lineTo(w, h);
            m2.lineTo(0, h);
            m2.close();
            p.setColor(0xFFD81B60);
            c.drawPath(m2, p);
            groundBlock(c, w, h);

            float cx = w * 0.26f, by = h * 0.92f;
            p.setColor(Color.WHITE);
            c.drawCircle(cx, by - 10 * u, 10 * u, p);
            c.drawCircle(cx, by - 26 * u, 7.5f * u, p);
            c.drawCircle(cx, by - 38.5f * u, 5.6f * u, p);
            p.setColor(0xFF37474F);
            c.drawRect(cx - 5 * u, by - 47 * u, cx + 5 * u, by - 43.5f * u, p);
            c.drawRect(cx - 3.2f * u, by - 54 * u, cx + 3.2f * u, by - 47 * u, p);
            c.drawCircle(cx - 2 * u, by - 39.5f * u, 0.8f * u, p);
            c.drawCircle(cx + 2 * u, by - 39.5f * u, 0.8f * u, p);
            c.drawCircle(cx, by - 26 * u, 0.9f * u, p);
            c.drawCircle(cx, by - 22 * u, 0.9f * u, p);
            p.setColor(0xFFFF7043);
            Path nose = new Path();
            nose.moveTo(cx, by - 38 * u);
            nose.lineTo(cx + 5.5f * u, by - 37 * u);
            nose.lineTo(cx, by - 36 * u);
            nose.close();
            c.drawPath(nose, p);
            p.setColor(0xFFE53935);
            c.drawRoundRect(new RectF(cx - 6 * u, by - 34 * u, cx + 6 * u, by - 31.5f * u), u, u, p);
            p.setColor(0xFF6D4C41);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.2f * u);
            c.drawLine(cx - 7 * u, by - 27 * u, cx - 15 * u, by - 33 * u, p);
            c.drawLine(cx + 7 * u, by - 27 * u, cx + 15 * u, by - 33 * u, p);
            p.setStyle(Paint.Style.FILL);
        }

        // ---- theme 2: Aurora Night (northern lights + igloo) ----
        private void ribbon(Canvas c, float w, float h, int color, float dy) {
            Path r = new Path();
            r.moveTo(0, h * (0.40f + dy));
            r.cubicTo(w * 0.3f, h * (0.12f + dy), w * 0.6f, h * (0.55f + dy), w, h * (0.25f + dy));
            r.lineTo(w, h * (0.42f + dy));
            r.cubicTo(w * 0.6f, h * (0.72f + dy), w * 0.3f, h * (0.34f + dy), 0, h * (0.58f + dy));
            r.close();
            p.setColor(color);
            c.drawPath(r, p);
        }

        private void aurora(Canvas c, float w, float h) {
            float u = h / 100f;
            sky(c, w, h, 0xFF0B1026, 0xFF1B2A5A, 0xFF35508A);
            p.setColor(0xFFFFFFFF);
            for (int i = 0; i + 1 < snow.length; i += 2) {
                c.drawCircle(snow[i] * w, snow[i + 1] * h * 0.9f, 0.7f * u + (i % 3) * 0.3f * u, p);
            }
            p.setColor(0xFFFFF8E1);
            c.drawCircle(w * 0.84f, h * 0.2f, 9 * u, p);
            p.setColor(0xFF16224A);
            c.drawCircle(w * 0.84f + 4 * u, h * 0.2f - 2 * u, 8 * u, p);

            ribbon(c, w, h, 0x8800E676, 0.0f);
            ribbon(c, w, h, 0x66B388FF, 0.12f);
            ribbon(c, w, h, 0x5518FFFF, 0.24f);

            Path m = new Path();
            m.moveTo(0, h * 0.86f);
            m.lineTo(w * 0.2f, h * 0.55f);
            m.lineTo(w * 0.4f, h * 0.8f);
            m.lineTo(w * 0.62f, h * 0.5f);
            m.lineTo(w * 0.85f, h * 0.82f);
            m.lineTo(w, h * 0.65f);
            m.lineTo(w, h * 0.86f);
            m.close();
            p.setColor(0xFF1E2F5C);
            c.drawPath(m, p);
            groundBlock(c, w, h);

            float cx = w * 0.7f, by = h * 0.9f;
            p.setColor(0xFFE3F2FD);
            c.drawArc(new RectF(cx - 17 * u, by - 34 * u, cx + 17 * u, by), 180, 180, true, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(0.9f * u);
            p.setColor(0xFF90CAF9);
            float[] ys = {5 * u, 10 * u, 14 * u};
            float[] hw = {16.2f * u, 13.7f * u, 9.6f * u};
            for (int i = 0; i < 3; i++) c.drawLine(cx - hw[i], by - ys[i], cx + hw[i], by - ys[i], p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFF37474F);
            c.drawArc(new RectF(cx - 5 * u, by - 14 * u, cx + 5 * u, by - 4 * u), 180, 180, true, p);
            c.drawRect(cx - 5 * u, by - 9 * u, cx + 5 * u, by, p);
        }

        // ---- theme 3: Mint Forest (snowy pines) ----
        private void forest(Canvas c, float w, float h) {
            float u = h / 100f;
            sky(c, w, h, 0xFFB2EBF2, 0xFFE0F7FA, 0xFFF1F8E9);
            p.setColor(0x66FFFFFF);
            c.drawCircle(w * 0.18f, h * 0.24f, 15 * u, p);
            p.setColor(0xFFFFFDE7);
            c.drawCircle(w * 0.18f, h * 0.24f, 9 * u, p);

            Path h1 = new Path();
            h1.moveTo(0, h * 0.7f);
            h1.quadTo(w * 0.25f, h * 0.4f, w * 0.5f, h * 0.68f);
            h1.quadTo(w * 0.78f, h * 0.9f, w, h * 0.5f);
            h1.lineTo(w, h);
            h1.lineTo(0, h);
            h1.close();
            p.setColor(0xFF80CBC4);
            c.drawPath(h1, p);
            Path h2 = new Path();
            h2.moveTo(0, h * 0.86f);
            h2.quadTo(w * 0.3f, h * 0.6f, w * 0.58f, h * 0.82f);
            h2.quadTo(w * 0.82f, h * 0.96f, w, h * 0.72f);
            h2.lineTo(w, h);
            h2.lineTo(0, h);
            h2.close();
            p.setColor(0xFF4DB6AC);
            c.drawPath(h2, p);
            groundBlock(c, w, h);

            pine(c, w * 0.12f, h * 0.9f, 34 * u, 0xFF2E7D32);
            pine(c, w * 0.22f, h * 0.92f, 46 * u, 0xFF388E3C);
            pine(c, w * 0.34f, h * 0.9f, 30 * u, 0xFF2E7D32);
            pine(c, w * 0.66f, h * 0.9f, 38 * u, 0xFF388E3C);
            pine(c, w * 0.78f, h * 0.93f, 50 * u, 0xFF2E7D32);
            pine(c, w * 0.9f, h * 0.9f, 32 * u, 0xFF43A047);
        }

        // ---- theme 4: Ocean Breeze (waves, ice floe, sailboat) ----
        private void ocean(Canvas c, float w, float h) {
            float u = h / 100f;
            sky(c, w, h, 0xFF81D4FA, 0xFFB3E5FC, 0xFFE1F5FE);
            p.setColor(0x66FFF59D);
            c.drawCircle(w * 0.8f, h * 0.25f, 16 * u, p);
            p.setColor(0xFFFFF176);
            c.drawCircle(w * 0.8f, h * 0.25f, 9 * u, p);

            Path pk = new Path();
            pk.moveTo(w * 0.05f, h * 0.58f);
            pk.lineTo(w * 0.2f, h * 0.4f);
            pk.lineTo(w * 0.32f, h * 0.58f);
            pk.close();
            pk.moveTo(w * 0.24f, h * 0.58f);
            pk.lineTo(w * 0.38f, h * 0.44f);
            pk.lineTo(w * 0.52f, h * 0.58f);
            pk.close();
            p.setColor(0xFFE1F5FE);
            c.drawPath(pk, p);

            p.setShader(new LinearGradient(0, h * 0.55f, 0, h, 0xFF4FC3F7, 0xFF0277BD, Shader.TileMode.CLAMP));
            c.drawRect(0, h * 0.55f, w, h, p);
            p.setShader(null);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.3f * u);
            p.setColor(0x88FFFFFF);
            for (int k = 0; k < 4; k++) {
                float y = h * (0.63f + 0.09f * k);
                Path wv = new Path();
                wv.moveTo(0, y);
                float seg = w / 8f;
                for (int i = 0; i < 8; i++) {
                    float x = i * seg;
                    wv.quadTo(x + seg / 2f, y + (i % 2 == 0 ? -3.5f : 3.5f) * u, x + seg, y);
                }
                c.drawPath(wv, p);
            }
            p.setStyle(Paint.Style.FILL);

            Path fl = new Path();
            fl.moveTo(w * 0.12f, h * 0.8f);
            fl.lineTo(w * 0.34f, h * 0.8f);
            fl.lineTo(w * 0.3f, h * 0.88f);
            fl.lineTo(w * 0.16f, h * 0.88f);
            fl.close();
            p.setColor(0xFFFFFFFF);
            c.drawPath(fl, p);
            Path fl2 = new Path();
            fl2.moveTo(w * 0.16f, h * 0.88f);
            fl2.lineTo(w * 0.3f, h * 0.88f);
            fl2.lineTo(w * 0.27f, h * 0.93f);
            fl2.lineTo(w * 0.19f, h * 0.93f);
            fl2.close();
            p.setColor(0xFFB3E5FC);
            c.drawPath(fl2, p);

            float bx = w * 0.66f, by = h * 0.74f;
            Path hull = new Path();
            hull.moveTo(bx - 12 * u, by);
            hull.lineTo(bx + 12 * u, by);
            hull.lineTo(bx + 8 * u, by + 6 * u);
            hull.lineTo(bx - 8 * u, by + 6 * u);
            hull.close();
            p.setColor(0xFFFF7043);
            c.drawPath(hull, p);
            Path sail1 = new Path();
            sail1.moveTo(bx, by - 26 * u);
            sail1.lineTo(bx, by - 2 * u);
            sail1.lineTo(bx + 13 * u, by - 2 * u);
            sail1.close();
            p.setColor(0xFFFFFFFF);
            c.drawPath(sail1, p);
            Path sail2 = new Path();
            sail2.moveTo(bx - 1.5f * u, by - 22 * u);
            sail2.lineTo(bx - 1.5f * u, by - 2 * u);
            sail2.lineTo(bx - 11 * u, by - 2 * u);
            sail2.close();
            p.setColor(0xFFFFE0B2);
            c.drawPath(sail2, p);
        }
    }
}
