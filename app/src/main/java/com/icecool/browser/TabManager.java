package com.icecool.browser;

import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

/** Keeps the list of open tabs (one instance per process). Up to 25 tabs. */
public class TabManager {

    public static final int MAX_TABS = 25;

    private static TabManager instance;

    private final List<TabItem> tabs = new ArrayList<>();
    private int activeTabIdx = 0;

    public static synchronized TabManager getInstance() {
        if (instance == null) instance = new TabManager();
        return instance;
    }

    public List<TabItem> getTabs() {
        return tabs;
    }

    public void addTab(TabItem tab) {
        tabs.add(tab);
        activeTabIdx = tabs.size() - 1;
    }

    public void removeTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        tabs.remove(index);
        if (activeTabIdx >= tabs.size()) activeTabIdx = Math.max(0, tabs.size() - 1);
    }

    public int getActiveTabIdx() {
        return activeTabIdx;
    }

    public void setActiveTabIdx(int idx) {
        activeTabIdx = idx;
    }

    public void clearAllTabs() {
        tabs.clear();
        activeTabIdx = 0;
    }

    public static class TabItem {
        public String url;
        public String title;
        public WebView webView;

        public TabItem(String url, String title, WebView webView) {
            this.url = url;
            this.title = title;
            this.webView = webView;
        }
    }
}
