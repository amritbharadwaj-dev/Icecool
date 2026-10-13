package com.icecool.browser;

import android.content.Context;
import android.webkit.WebView;
import java.util.ArrayList;
import java.util.List;

public class TabManager {
    private static TabManager instance;
    private final List<TabItem> tabs = new ArrayList<>();
    private int activeTabIdx = 0;

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

    public static synchronized TabManager getInstance() {
        if (instance == null) {
            instance = new TabManager();
        }
        return instance;
    }

    public List<TabItem> getTabs() { return tabs; }
    public int getActiveTabIdx() { return activeTabIdx; }
    public void setActiveTabIdx(int idx) { activeTabIdx = idx; }

    public boolean addTab(TabItem tab) {
        if (tabs.size() >= 10) { // Enforcing 10 Tabs limit
            return false;
        }
        tabs.add(tab);
        activeTabIdx = tabs.size() - 1;
        return true;
    }

    public void removeTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            TabItem item = tabs.remove(index);
            if (item.webView != null) {
                item.webView.destroy();
            }
            if (activeTabIdx >= tabs.size()) {
                activeTabIdx = Math.max(0, tabs.size() - 1);
            }
        }
    }

    public void clearAllTabs() {
        for (TabItem tab : tabs) {
            if (tab.webView != null) {
                tab.webView.destroy();
            }
        }
        tabs.clear();
        activeTabIdx = 0;
    }
}
