package com.icecool.browser;

import android.Manifest;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etSearchUrl, etHomeSearch;
    private TextView btnTabCounter;
    private FrameLayout webContainer;
    private View homeView;
    private TabManager tabManager;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private int currentZoomLevel = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabManager = TabManager.getInstance();

        etSearchUrl = findViewById(R.id.etSearchUrl);
        etHomeSearch = findViewById(R.id.etHomeSearch);
        btnTabCounter = findViewById(R.id.btnTabCounter);
        webContainer = findViewById(R.id.webContainer);
        homeView = findViewById(R.id.homeView);

        checkPermissions();

        findViewById(R.id.btnHomeNav).setOnClickListener(v -> navigateHome());
        findViewById(R.id.btnRefresh).setOnClickListener(v -> refreshCurrentTab());
        findViewById(R.id.btnMenu).setOnClickListener(this::showChromePopupMenu);
        btnTabCounter.setOnClickListener(v -> showTabSwitcherDialog());

        etSearchUrl.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = etSearchUrl.getText().toString().trim();
                if (!query.isEmpty()) loadUrlInNewOrCurrentTab(formatUrl(query));
                return true;
            }
            return false;
        });

        etHomeSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = etHomeSearch.getText().toString().trim();
                if (!query.isEmpty()) loadUrlInNewOrCurrentTab(formatUrl(query));
                return true;
            }
            return false;
        });

        findViewById(R.id.btnYoutube).setOnClickListener(v -> loadUrlInNewOrCurrentTab("https://www.youtube.com"));
        findViewById(R.id.btnInstagram).setOnClickListener(v -> loadUrlInNewOrCurrentTab("https://www.instagram.com"));
        findViewById(R.id.btnFacebook).setOnClickListener(v -> loadUrlInNewOrCurrentTab("https://www.facebook.com"));
        findViewById(R.id.btnTwitter).setOnClickListener(v -> loadUrlInNewOrCurrentTab("https://www.twitter.com"));
        findViewById(R.id.btnWhatsapp).setOnClickListener(v -> loadUrlInNewOrCurrentTab("https://web.whatsapp.com"));

        if (tabManager.getTabs().isEmpty()) {
            createNewTab("");
        } else {
            renderActiveTab();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 101);
        }
    }

    private String formatUrl(String input) {
        if (input.startsWith("http://") || input.startsWith("https://")) return input;
        if (input.contains(".") && !input.contains(" ")) return "https://" + input;
        return "https://www.google.com/search?q=" + android.net.Uri.encode(input);
    }

    private void createNewTab(String url) {
        if (tabManager.getTabs().size() >= 10) {
            Toast.makeText(this, "Max 10 tabs allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        WebView webView = new WebView(this);
        setupWebViewSettings(webView);

        TabManager.TabItem tab = new TabManager.TabItem(url, url.isEmpty() ? "New Tab" : url, webView);
        tabManager.addTab(tab);

        if (!url.isEmpty()) {
            webView.loadUrl(url);
        }
        renderActiveTab();
    }

    private void setupWebViewSettings(WebView webView) {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                createNewTab(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                etSearchUrl.setText(url);
                CookieManager.getInstance().flush();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                webContainer.setVisibility(View.GONE);
                FrameLayout decor = (FrameLayout) getWindow().getDecorView();
                decor.addView(customView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;
                FrameLayout decor = (FrameLayout) getWindow().getDecorView();
                decor.removeView(customView);
                customView = null;
                webContainer.setVisibility(View.VISIBLE);
                if (customViewCallback != null) customViewCallback.onCustomViewHidden();
            }
        });
    }

    private void renderActiveTab() {
        btnTabCounter.setText(String.valueOf(tabManager.getTabs().size()));
        webContainer.removeAllViews();

        if (tabManager.getTabs().isEmpty()) {
            homeView.setVisibility(View.VISIBLE);
            webContainer.addView(homeView);
            etSearchUrl.setText("");
            return;
        }

        TabManager.TabItem activeTab = tabManager.getTabs().get(tabManager.getActiveTabIdx());
        if (activeTab.url.isEmpty()) {
            homeView.setVisibility(View.VISIBLE);
            webContainer.addView(homeView);
            etSearchUrl.setText("");
        } else {
            homeView.setVisibility(View.GONE);
            if (activeTab.webView.getParent() != null) {
                ((FrameLayout) activeTab.webView.getParent()).removeView(activeTab.webView);
            }
            webContainer.addView(activeTab.webView);
            etSearchUrl.setText(activeTab.url);
        }
    }

    private void loadUrlInNewOrCurrentTab(String url) {
        if (tabManager.getTabs().isEmpty()) {
            createNewTab(url);
            return;
        }
        TabManager.TabItem currentTab = tabManager.getTabs().get(tabManager.getActiveTabIdx());
        if (currentTab.url.isEmpty()) {
            currentTab.url = url;
            currentTab.title = url;
            currentTab.webView.loadUrl(url);
            renderActiveTab();
        } else {
            createNewTab(url);
        }
    }

    private void navigateHome() {
        if (!tabManager.getTabs().isEmpty()) {
            TabManager.TabItem currentTab = tabManager.getTabs().get(tabManager.getActiveTabIdx());
            currentTab.url = "";
            currentTab.title = "New Tab";
        }
        renderActiveTab();
    }

    private void refreshCurrentTab() {
        if (!tabManager.getTabs().isEmpty()) {
            TabManager.TabItem currentTab = tabManager.getTabs().get(tabManager.getActiveTabIdx());
            if (currentTab.webView != null && !currentTab.url.isEmpty()) {
                currentTab.webView.reload();
            }
        }
    }

    private void showChromePopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("New tab");
        popup.getMenu().add("Close all tabs");
        popup.getMenu().add("Delete all data & cookies");
        popup.getMenu().add("Zoom Controls (" + currentZoomLevel + "%)");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("New tab")) {
                createNewTab("");
            } else if (title.equals("Close all tabs")) {
                tabManager.clearAllTabs();
                createNewTab("");
            } else if (title.equals("Delete all data & cookies")) {
                clearAllDataAndCookies();
            } else if (title.startsWith("Zoom Controls")) {
                showZoomControlDialog();
            }
            return true;
        });
        popup.show();
    }

    private void showZoomControlDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Zoom Controls (" + currentZoomLevel + "%)")
                .setPositiveButton("Zoom In (+)", (d, w) -> {
                    if (currentZoomLevel < 200) {
                        currentZoomLevel += 25;
                        applyZoom(true);
                    }
                })
                .setNegativeButton("Zoom Out (-)", (d, w) -> {
                    if (currentZoomLevel > 50) {
                        currentZoomLevel -= 25;
                        applyZoom(false);
                    }
                })
                .setNeutralButton("Reset (100%)", (d, w) -> {
                    currentZoomLevel = 100;
                    applyZoomReset();
                })
                .show();
    }

    private void applyZoom(boolean zoomIn) {
        if (!tabManager.getTabs().isEmpty()) {
            TabManager.TabItem currentTab = tabManager.getTabs().get(tabManager.getActiveTabIdx());
            if (currentTab.webView != null) {
                if (zoomIn) {
                    currentTab.webView.zoomIn();
                } else {
                    currentTab.webView.zoomOut();
                }
            }
        }
        Toast.makeText(this, "Zoom: " + currentZoomLevel + "%", Toast.LENGTH_SHORT).show();
    }

    private void applyZoomReset() {
        if (!tabManager.getTabs().isEmpty()) {
            TabManager.TabItem currentTab = tabManager.getTabs().get(tabManager.getActiveTabIdx());
            if (currentTab.webView != null) {
                currentTab.webView.setInitialScale(100);
            }
        }
        Toast.makeText(this, "Zoom Reset: 100%", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "All Cookies & Data Cleared!", Toast.LENGTH_SHORT).show();
    }

    private void showTabSwitcherDialog() {
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        String[] titles = new String[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) {
            titles[i] = "Tab " + (i + 1) + ": " + (tabs.get(i).url.isEmpty() ? "New Tab" : tabs.get(i).url);
        }

        new AlertDialog.Builder(this)
                .setTitle("Active Tabs (" + tabs.size() + "/10)")
                .setItems(titles, (dialog, which) -> {
                    tabManager.setActiveTabIdx(which);
                    renderActiveTab();
                })
                .setPositiveButton("+ New Tab", (dialog, which) -> createNewTab(""))
                .setNegativeButton("Close Tab", (dialog, which) -> {
                    tabManager.removeTab(tabManager.getActiveTabIdx());
                    if (tabManager.getTabs().isEmpty()) {
                        createNewTab("");
                    } else {
                        renderActiveTab();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            WebChromeClient client = new WebChromeClient();
            client.onHideCustomView();
            return;
        }
        if (!tabManager.getTabs().isEmpty()) {
            TabManager.TabItem currentTab = tabManager.getTabs().get(tabManager.getActiveTabIdx());
            if (currentTab.webView != null && currentTab.webView.canGoBack()) {
                currentTab.webView.goBack();
                return;
            }
        }
        super.onBackPressed();
    }
}
