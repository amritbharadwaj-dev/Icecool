package com.icecool.browser;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlInput;
    private Button goButton, clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        urlInput = findViewById(R.id.urlInput);
        goButton = findViewById(R.id.goButton);
        clearButton = findViewById(R.id.clearButton);

        // Browser Settings (Cookies & LocalStorage ON)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                urlInput.setText(url);
            }
        });

        // Start Page
        webView.loadUrl("https://www.google.com");

        // Go Button Action
        goButton.setOnClickListener(v -> loadUrlFromInput());

        // Keyboard Enter Action
        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                loadUrlFromInput();
                return true;
            }
            return false;
        });

        // Clear Button Action
        clearButton.setOnClickListener(v -> {
            webView.clearCache(true);
            webView.clearHistory();
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
            webView.loadUrl("https://www.google.com");
            Toast.makeText(MainActivity.this, "Browser Data Cleared!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUrlFromInput() {
        String input = urlInput.getText().toString().trim();
        if (!input.isEmpty()) {
            if (input.startsWith("http://") || input.startsWith("https://")) {
                webView.loadUrl(input);
            } else if (input.contains(".") && !input.contains(" ")) {
                webView.loadUrl("https://" + input);
            } else {
                webView.loadUrl("https://www.google.com/search?q=" + android.net.Uri.encode(input));
            }
        }
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
