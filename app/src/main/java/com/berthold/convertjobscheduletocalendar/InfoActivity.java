package com.berthold.convertjobscheduletocalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.graphics.Color;
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

    // UI
    private MainActivityViewModel mainActivityViewModel;

    // Filesystem
    private BufferedReader bufferedReader;

    // Html
    private StringBuilder htmlSite;
    private TextView updateInfoView;
    private WebView webView;
    private ProgressBar progress;

    // Version info
    private String currentVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // ViewModel
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        // UI
        final Handler handler = new Handler();
        Context context = getApplicationContext();

        updateInfoView = findViewById(R.id.info_new_version_available);
        webView = (WebView) findViewById(R.id.browser);
        progress = (ProgressBar) findViewById(R.id.html_load_progress);

        // @rem:Get current locale (determine language from Androids settings@@
        //final Locale current=getResources().getConfiguration().locale;
        final String current = getResources().getConfiguration().locale.getLanguage();
        //Log.v("LOCALE", "Country:" + current);

        // Check if there is an update available
        currentVersion = GetThisAppsVersion.thisVersion(getApplicationContext());
        getSupportActionBar().setSubtitle("Version:" + currentVersion);

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

                // Show
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);
                        webView.loadData(htmlSite.toString(), "text/html", null);

                        String latestVersionInGooglePlay = mainActivityViewModel.getAppVersionfromGooglePlay(getApplicationContext());

                        if (latestVersionInGooglePlay.equals(currentVersion)) {
                            //updateInfoView.setText(getResources().getText(R.string.version_info_is_latest_version));
                            updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.version_info_ok) + "", 0));
                        } else
                            updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.version_info_update_available) + latestVersionInGooglePlay, 0));
                    }
                });
            }
        });
        t.start();
    }
}
