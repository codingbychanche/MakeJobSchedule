package com.berthold.convertjobscheduletocalendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InfoActivity extends AppCompatActivity {

    /**
     * Shows app info...
     */
    // Info
    private static String tag;

    // Filesystem
    private BufferedReader bufferedReader;

    // Html
    private StringBuilder htmlSite;
    private TextView versionNameTagView;
    private WebView webView;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // UI
        final Handler handler = new Handler();
        Context context = getApplicationContext();

        versionNameTagView=findViewById(R.id.version_name_tag_display);
        webView = (WebView) findViewById(R.id.browser);
        progress = (ProgressBar) findViewById(R.id.html_load_progress);

        // @rem:Get current locale (determine language from Androids settings@@
        //final Locale current=getResources().getConfiguration().locale;
        final String current = getResources().getConfiguration().locale.getLanguage();
        Log.v("LOCALE", "Country:" + current);

        String version=GetThisAppsVersion.thisVersion(getApplicationContext());
        versionNameTagView.setText(version);

        // Load html...
        progress.setVisibility(View.VISIBLE);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    htmlSite = new StringBuilder();

                    // @rem:Shows how to load data from androids 'assests'- folder@@

                    if (current.equals("de") || current.equals("en")) {
                        if (current.equals("de"))
                            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("InfoPage-de.html")));
                        if (current.equals("en"))
                            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("InfoPage-en.html")));
                    } else
                        bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("InfoPage-en.html")));

                    String line;
                    while ((line = bufferedReader.readLine()) != null)
                        htmlSite.append(line);

                } catch (IOException io) {
                    Log.v("Info", io.toString());
                }

                // Wait a vew millisec's to enable the main UI thread
                // to react.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                // Show
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);
                        webView.loadData(htmlSite.toString(), "text/html", null);
                    }
                });
            }
        });
        t.start();
    }
}
