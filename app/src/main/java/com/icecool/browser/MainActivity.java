package com.icecool.browser;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSearch = findViewById(R.id.etSearch);
        ImageView btnSearchSubmit = findViewById(R.id.btnSearchSubmit);

        etSearch.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    openBrowser(formatUrl(query));
                }
                return true;
            }
            return false;
        });

        btnSearchSubmit.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                openBrowser(formatUrl(query));
            }
        });

        // 5 Quick Shortcuts
        findViewById(R.id.btnYoutube).setOnClickListener(v -> openBrowser("https://www.youtube.com"));
        findViewById(R.id.btnInstagram).setOnClickListener(v -> openBrowser("https://www.instagram.com"));
        findViewById(R.id.btnFacebook).setOnClickListener(v -> openBrowser("https://www.facebook.com"));
        findViewById(R.id.btnTwitter).setOnClickListener(v -> openBrowser("https://twitter.com"));
        findViewById(R.id.btnWhatsapp).setOnClickListener(v -> openBrowser("https://web.whatsapp.com"));
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

    private void openBrowser(String url) {
        TabManager tabManager = TabManager.getInstance();
        if (tabManager.getTabs().size() >= 5) {
            // If 5 tabs already open, jump directly to browser with active tabs or replace
            Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
            startActivity(intent);
            return;
        }

        WebView webView = new WebView(this);
        TabManager.TabItem newTab = new TabManager.TabItem(url, url, webView);
        tabManager.addTab(newTab);

        Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
        startActivity(intent);
    }
}
