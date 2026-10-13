package com.icecool.browser;

import android.Manifest;
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
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int BLUE = 0xFF1A73E8;
    private static final int INK = 0xFF3C4043;
    private static final int MAX_TABS = 20;

    private FrameLayout root, webContainer, tabSwitcher;
    private LinearLayout tabGrid;
    private ScrollView homeView;
    private EditText etSearchUrl, etHomeSearch;
    private TextView btnTabCounter, switcherTitle;
    private ProgressBar progressBar;
    private TabManager tabManager;
    private PopupWindow menuPopup;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private int currentZoomLevel = 100;

    private final Map<TabManager.TabItem, Bitmap> thumbs = new HashMap<>();
    private final Map<String, Bitmap> faviconCache = Collections.synchronizedMap(new HashMap<String, Bitmap>());

    // ------------------------------------------------------------------ lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root);

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

        tabManager = TabManager.getInstance();
        buildUi();
        checkPermissions();

        if (tabManager.getTabs().isEmpty()) {
            createNewTab("");
        } else {
            renderActiveTab();
        }
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
        IconView iv = new IconView(this, type, INK);
        iv.setPadding(dp(9), dp(9), dp(9), dp(9));
        iv.setBackground(ripple(true));
        iv.setOnClickListener(l);
        return iv;
    }

    private void buildUi() {
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        root.addView(main, new FrameLayout.LayoutParams(MATCH, MATCH));

        main.addView(buildTopBar());

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        progressBar.setProgressTintList(ColorStateList.valueOf(BLUE));
        main.addView(progressBar, new LinearLayout.LayoutParams(MATCH, dp(3)));

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

        bar.addView(iconButton(IconView.HOME, v -> navigateHome()), new LinearLayout.LayoutParams(dp(40), dp(40)));
        bar.addView(iconButton(IconView.REFRESH, v -> refreshCurrentTab()), new LinearLayout.LayoutParams(dp(40), dp(40)));

        LinearLayout pill = new LinearLayout(this);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setBackground(roundBg(0xFFEEF1F8, 20, 0, 0));
        pill.setPadding(dp(12), 0, dp(8), 0);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(0, dp(40), 1f);
        plp.setMargins(dp(4), 0, dp(6), 0);

        pill.addView(new IconView(this, IconView.SEARCH, 0xFF5F6368), new LinearLayout.LayoutParams(dp(18), dp(18)));
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
        btnTabCounter.setTextColor(INK);
        btnTabCounter.setTextSize(12);
        btnTabCounter.setTypeface(Typeface.DEFAULT_BOLD);
        btnTabCounter.setBackground(roundBg(Color.TRANSPARENT, 7, INK, 2));
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
        sv.setBackgroundColor(0xFFF6FAFF);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(0, 0, 0, dp(24));

        col.addView(new IceScene(this), new LinearLayout.LayoutParams(MATCH, dp(220)));

        SpannableStringBuilder sb = new SpannableStringBuilder("ICE\u00B7cool");
        sb.setSpan(new ForegroundColorSpan(0xFF00B4D8), 0, 3, 0);
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, 3, 0);
        sb.setSpan(new ForegroundColorSpan(0xFFFF8A65), 3, 4, 0);
        sb.setSpan(new ForegroundColorSpan(0xFF0D47A1), 4, 8, 0);
        sb.setSpan(new StyleSpan(Typeface.ITALIC), 4, 8, 0);
        TextView title = new TextView(this);
        title.setText(sb);
        title.setTextSize(42);
        title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        title.setLetterSpacing(0.06f);
        col.addView(title, new LinearLayout.LayoutParams(WRAP, WRAP));

        TextView tag = new TextView(this);
        tag.setText("Stay chill. Browse fast.");
        tag.setTextColor(0xFF5F6368);
        tag.setTextSize(13);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(WRAP, WRAP);
        tlp.setMargins(0, dp(2), 0, dp(18));
        col.addView(tag, tlp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(roundBg(Color.WHITE, 28, 0xFFDADCE0, 1));
        card.setElevation(dp(3));
        card.setPadding(dp(18), 0, dp(14), 0);
        card.addView(new IconView(this, IconView.SEARCH, BLUE), new LinearLayout.LayoutParams(dp(22), dp(22)));
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
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(MATCH, dp(52));
        cardLp.setMargins(dp(24), 0, dp(24), dp(22));
        col.addView(card, cardLp);

        String[] names = {"YouTube", "Instagram", "Facebook", "X", "WhatsApp", "Google", "GitHub", "Wikipedia"};
        String[] urls = {"https://www.youtube.com", "https://www.instagram.com", "https://www.facebook.com",
                "https://x.com", "https://web.whatsapp.com", "https://www.google.com", "https://github.com",
                "https://www.wikipedia.org"};
        String[] domains = {"youtube.com", "instagram.com", "facebook.com", "x.com", "whatsapp.com",
                "google.com", "github.com", "wikipedia.org"};

        for (int r = 0; r < 2; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int c = 0; c < 4; c++) {
                int i = r * 4 + c;
                row.addView(buildShortcut(names[i], urls[i], domains[i]), new LinearLayout.LayoutParams(0, WRAP, 1f));
            }
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(MATCH, WRAP);
            rlp.setMargins(dp(16), 0, dp(16), 0);
            col.addView(row, rlp);
        }

        sv.addView(col, new ViewGroup.LayoutParams(MATCH, WRAP));
        return sv;
    }

    private View buildShortcut(String name, String url, String domain) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(0, dp(8), 0, dp(8));

        FrameLayout circle = new FrameLayout(this);
        circle.setBackground(ovalBg(Color.WHITE, 0xFFE3E7EF, 1));
        circle.setElevation(dp(2));

        TextView letter = new TextView(this);
        letter.setText(name.substring(0, 1));
        letter.setTextColor(BLUE);
        letter.setTextSize(20);
        letter.setTypeface(Typeface.DEFAULT_BOLD);
        letter.setGravity(Gravity.CENTER);
        circle.addView(letter, new FrameLayout.LayoutParams(MATCH, MATCH));

        ImageView icon = new ImageView(this);
        circle.addView(icon, new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER));
        tile.addView(circle, new LinearLayout.LayoutParams(dp(56), dp(56)));

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
        sw.setBackgroundColor(0xFFF1F3FA);
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
        plus.setBackground(roundBg(BLUE, 16, 0, 0));
        plus.setOnClickListener(v -> {
            createNewTab("");
            hideTabSwitcher();
        });
        header.addView(plus, new LinearLayout.LayoutParams(dp(48), dp(44)));

        switcherTitle = new TextView(this);
        switcherTitle.setTextSize(18);
        switcherTitle.setTypeface(Typeface.DEFAULT_BOLD);
        switcherTitle.setTextColor(0xFF202124);
        switcherTitle.setPadding(dp(16), 0, 0, 0);
        header.addView(switcherTitle, new LinearLayout.LayoutParams(0, WRAP, 1f));

        header.addView(iconButton(IconView.CLOSEALL, v -> {
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
        card.setBackground(roundBg(active ? 0xFFD3E3FD : 0xFFE3E8F3, 20, BLUE, active ? 2 : 0));
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
            ph.setTextSize(isHome ? 28 : 14);
            ph.setTextColor(isHome ? 0xFF00B4D8 : 0xFF5F6368);
            ph.setText(isHome ? "\u2744\n" + "ICE\u00B7cool" : hostOf(tab.url));
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
        if (tabSwitcher.getVisibility() != View.VISIBLE) return;
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

        menu.addView(menuRow(IconView.PLUS, "New tab", INK, v -> {
            menuPopup.dismiss();
            createNewTab("");
        }));
        menu.addView(menuRow(IconView.CLOSEALL, "Close all tabs", INK, v -> {
            menuPopup.dismiss();
            closeAllTabs();
        }));
        menu.addView(menuRow(IconView.DOWNLOAD, "Downloads", INK, v -> {
            menuPopup.dismiss();
            try {
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
            } catch (Exception e) {
                toast("Downloads app nahi mili");
            }
        }));
        menu.addView(menuRow(IconView.DOWNLOAD, "Download this page", INK, v -> {
            menuPopup.dismiss();
            TabManager.TabItem t = currentTab();
            if (t == null || t.url.isEmpty()) {
                toast("Pehle koi page kholo");
            } else {
                startDownload(t.url, t.webView.getSettings().getUserAgentString(), null, null);
            }
        }));

        // Zoom row
        LinearLayout zoom = new LinearLayout(this);
        zoom.setOrientation(LinearLayout.HORIZONTAL);
        zoom.setGravity(Gravity.CENTER_VERTICAL);
        zoom.setPadding(dp(16), 0, dp(10), 0);
        zoom.addView(new IconView(this, IconView.SEARCH, INK), new LinearLayout.LayoutParams(dp(24), dp(24)));
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

        menuPopup = new PopupWindow(wrapper, dp(280), WRAP, true);
        menuPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        menuPopup.showAsDropDown(anchor, anchor.getWidth() - dp(280) + dp(8), -anchor.getHeight() + dp(4));
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
        t.setTextColor(color == INK ? 0xFF202124 : color);
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
        progressBar.setVisibility(View.GONE);

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
                    progressBar.setProgress(5);
                    progressBar.setVisibility(View.VISIBLE);
                    if (!etSearchUrl.hasFocus() && t != null && !t.url.isEmpty()) etSearchUrl.setText(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                TabManager.TabItem t = findTab(view);
                if (t != null && !t.url.isEmpty()) t.url = url;
                if (isActive(view)) {
                    progressBar.setVisibility(View.GONE);
                    if (!etSearchUrl.hasFocus() && t != null && !t.url.isEmpty()) etSearchUrl.setText(url);
                }
                CookieManager.getInstance().flush();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (isActive(view)) {
                    progressBar.setProgress(newProgress);
                    progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
                }
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
            Bitmap b = null;
            try {
                URL u = new URL("https://www.google.com/s2/favicons?domain=" + domain + "&sz=128");
                HttpURLConnection con = (HttpURLConnection) u.openConnection();
                con.setConnectTimeout(6000);
                con.setReadTimeout(6000);
                InputStream is = con.getInputStream();
                b = BitmapFactory.decodeStream(is);
                is.close();
            } catch (Exception ignored) {
            }
            final Bitmap fb = b;
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
                DOWNLOAD = 6, TRASH = 7, SEARCH = 8, CLOSEALL = 9;
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

    /** Cute icy illustration for the home screen. */
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

            p.setShader(new LinearGradient(0, 0, 0, h, 0xFFBDE7FF, 0xFFF6FAFF, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);

            // sun
            p.setColor(0x55FFF59D);
            c.drawCircle(w * 0.8f, h * 0.28f, 16 * u, p);
            p.setColor(0xFFFFF3B0);
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
            p.setColor(0xFF9ED8F5);
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

            // ground
            p.setColor(0xFFEAF6FF);
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
            p.setColor(0xFFCDEFFF);
            c.drawPath(ice, p);
            Path face = new Path();
            face.moveTo(w * 0.36f, h * 0.48f);
            face.lineTo(w * 0.47f, h * 0.9f);
            face.lineTo(w * 0.31f, h * 0.9f);
            face.close();
            p.setColor(0xFF8FD3F4);
            c.drawPath(face, p);

            // penguin
            float cx = w * 0.7f, by = h * 0.9f;
            p.setColor(0xFF263238);
            c.drawOval(new RectF(cx - 10 * u, by - 36 * u, cx + 10 * u, by), p);
            c.drawOval(new RectF(cx - 14 * u, by - 26 * u, cx - 9 * u, by - 12 * u), p);
            c.drawOval(new RectF(cx + 9 * u, by - 26 * u, cx + 14 * u, by - 12 * u), p);
            p.setColor(Color.WHITE);
            c.drawOval(new RectF(cx - 6.5f * u, by - 26 * u, cx + 6.5f * u, by - 2 * u), p);
            p.setColor(0xFF1A73E8);
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
