package com.icecool.browser;

import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

import java.io.File;

/**
 * Private window. Runs in its own process (":private") with its own WebView data folder,
 * so cookies / storage / cache never mix with normal tabs and are wiped when it closes.
 */
public class PrivateActivity extends MainActivity {

    private static boolean cleaned = false;

    @Override
    protected boolean isPrivateMode() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                WebView.setDataDirectorySuffix("private");
            } catch (Throwable ignored) {
            }
        }
        if (!cleaned) {
            cleaned = true;
            deleteLeftovers();
        }
        super.onCreate(savedInstanceState);
    }

        /** Fresh private process = fresh data. Removes anything an old private session left on disk. */
    private void deleteLeftovers() {
        try {
            File[] files = getDataDir().listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith("app_webview_private")) deleteRecursive(f);
                }
            }
            File[] cache = getCacheDir().listFiles();
            if (cache != null) {
                for (File f : cache) {
                    if (f.getName().toLowerCase().contains("private")) deleteRecursive(f);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void deleteRecursive(File f) {
        File[] kids = f.listFiles();
        if (kids != null) {
            for (File k : kids) deleteRecursive(k);
        }
        // noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
