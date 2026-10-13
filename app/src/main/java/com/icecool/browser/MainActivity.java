package com.icecool.browser;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etSearchUrl;
    private TextView btnTabCounter;
    private FrameLayout webContainer;
    private View homeView;
    private TabManager tabManager;

    private static final int PERMISSION_REQ_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabManager = TabManager.getInstance();

        etSearchUrl = findViewById(R.id.etSearchUrl);
        btnTabCounter = findViewById(R.id.btnTabCounter);
        webContainer = findViewById(R.id.webContainer);
        homeView = findViewById(R.id.homeView);

        checkAndRequestPermissions();

        // Top Navigation Controls
        findViewById(R.id.btnHomeNav).setOnClickListener(v -> navigateHome());
        findViewById(R.id.btnRefresh).setOnClickListener(v -> refreshCurrentTab());
        findViewById(R.id.btnMenu).setOnClickListener(v -> showChromeMenu());
        btnTabCounter.setOnClickListener(v -> showTabSwitcherDialog());

        // Search action
        etSearchUrl.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_GO
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String input = etSearchUrl.getText().toString().trim();
                if (!input.isEmpty()) {
                    loadUrlInCurrentTab(formatUrl(input));
                }
                return true;
            }
            return false;
        });

        // 5 Shortcuts
        findViewById(R.id.btnYoutube).setOnClickListener(v -> loadUrlInCurrentTab("https://www.youtube.com"));
        findViewById(R.id.btnInstagram).setOnClickListener(v -> loadUrlInCurrentTab("https://www.instagram.com"));
        findViewById(R.id.btnFacebook).setOnClickListener(v -> loadUrlInCurrentTab("https://www.facebook.com"));
        findViewById(R.id.btnTwitter).setOnClickListener(v -> loadUrlInCurrentTab("https://www.twitter.com"));
        findViewById(R.id.btnWhatsapp).setOnClickListener(v -> loadUrlInCurrentTab("https://web.whatsapp.com"));

        // Initialize Home state
        if (tabManager.getTabs().isEmpty()) {
            createNewTab("");
        } else {
            renderActiveTab();
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQ_CODE);
        }
    }

    private String formatUrl(String input) {
        if (input.startsWith("http://") || input.startsWith("https://")) {
            return input;
        } else if (input.contains(".") && !input.contains(" ")) {
            return "https://" + input;
        } else {
            return "https://www.google.com/search?q=" + android.net.Uri.encode(input);
        }
    }

    private void createNewTab(String url) {
        if (tabManager.getTabs().size() >= 10) {
            Toast.makeText(this, "Max 10 tabs allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        WebView webView = new WebView(this);
        setupWebView(webView);

        TabManager.TabItem tab = new TabManager.TabItem(url, url.isEmpty() ? "New Tab" : url, webView);
        tabManager.addTab(tab);

        if (!url.isEmpty()) {
            webView.loadUrl(url);
        }
        renderActiveTab();
    }

    private void setupWebView(WebView webView) {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                etSearchUrl.setText(url);
                CookieManager.getInstance().flush();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
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

    private void loadUrlInCurrentTab(String url) {
        if (tabManager.getTabs().isEmpty()) {
            createNewTab(url);
            return;
        }

        TabManager.TabItem currentTab = tabManager.getTabs().get(tabManager.getActiveTabIdx());
        currentTab.url = url;
        currentTab.title = url;
        currentTab.webView.loadUrl(url);
        renderActiveTab();
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

    private void showChromeMenu() {
        String[] options = {
                "New tab",
                "Close all tabs",
                "Clear all data & cookies"
        };

        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        createNewTab("");
                    } else if (which == 1) {
                        tabManager.clearAllTabs();
                        createNewTab("");
                    } else if (which == 2) {
                        clearAllDataAndCookies();
                    }
                })
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
        Toast.makeText(this, "All Cookies & Data Cleared Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void showTabSwitcherDialog() {
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        String[] titles = new String[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) {
            titles[i] = "Tab " + (i + 1) + ": " + (tabs.get(i).url.isEmpty() ? "New Tab" : tabs.get(i).url);
        }

        new AlertDialog.Builder(this)
                .setTitle("Tabs (" + tabs.size() + "/10)")
                .setItems(titles, (dialog, which) -> {
                    tabManager.setActiveTabIdx(which);
                    renderActiveTab();
                })
                .setPositiveButton("+ New Tab", (dialog, which) -> createNewTab(""))
                .setNegativeButton("Close Selected Tab", (dialog, which) -> {
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
