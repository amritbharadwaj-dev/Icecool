package com.icecool.browser;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class BrowserActivity extends AppCompatActivity {
    private WebView webView;
    private EditText etUrl;
    private TabManager tabManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        etUrl = findViewById(R.id.etUrl);
        Button btnMenu = findViewById(R.id.btnMenu);
        webView = findViewById(R.id.webView);
        tabManager = TabManager.getInstance();

        setupWebViewSettings();
        loadActiveTab();

        etUrl.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String input = etUrl.getText().toString().trim();
                String targetUrl = formatUrl(input);
                webView.loadUrl(targetUrl);
                return true;
            }
            return false;
        });

        btnMenu.setOnClickListener(v -> showOptionsMenu());
    }

    private void setupWebViewSettings() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                etUrl.setText(url);
                CookieManager.getInstance().flush(); // Save local storage & cookies instantly
            }
        });
    }

    private void loadActiveTab() {
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        if (!tabs.isEmpty()) {
            TabManager.TabItem currentTab = tabs.get(tabManager.getActiveTabIdx());
            // Swap webView parent container view safely if needed
            webView.loadUrl(currentTab.url);
        } else {
            webView.loadUrl("https://www.google.com");
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

    private void showOptionsMenu() {
        String[] options = {"My Tabs", "Clear Data & Cookies", "Pin to Home"};
        new AlertDialog.Builder(this)
                .setTitle("Ice Cool Menu")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showTabsManagerDialog();
                    } else if (which == 1) {
                        clearAllBrowserData();
                    } else if (which == 2) {
                        Toast.makeText(this, "Current tab pinned!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void clearAllBrowserData() {
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();
        
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        
        android.webkit.WebStorage.getInstance().deleteAllData();
        Toast.makeText(this, "All Data & Cookies Cleared Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void showTabsManagerDialog() {
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        if (tabs.isEmpty()) {
            Toast.makeText(this, "No active tabs", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] tabTitles = new String[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) {
            tabTitles[i] = "Tab " + (i + 1) + ": " + tabs.get(i).url;
        }

        new AlertDialog.Builder(this)
                .setTitle("My Tabs (" + tabs.size() + "/5)")
                .setItems(tabTitles, (dialog, which) -> {
                    tabManager.setActiveTabIdx(which);
                    webView.loadUrl(tabs.get(which).url);
                })
                .setNegativeButton("Close Specific Tab", (dialog, which) -> showCloseTabSelector())
                .setPositiveButton("Back to Browse", null)
                .show();
    }

    private void showCloseTabSelector() {
        List<TabManager.TabItem> tabs = tabManager.getTabs();
        if (tabs.isEmpty()) return;

        String[] tabTitles = new String[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) {
            tabTitles[i] = "Close Tab " + (i + 1);
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Tab to Close")
                .setItems(tabTitles, (dialog, which) -> {
                    tabManager.removeTab(which);
                    Toast.makeText(this, "Tab closed", Toast.LENGTH_SHORT).show();
                    if (tabManager.getTabs().isEmpty()) {
                        finish();
                    } else {
                        loadActiveTab();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
                          }
