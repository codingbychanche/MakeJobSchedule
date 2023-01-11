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
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

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

        // Check if there is an update available
        currentVersion = GetThisAppsVersion.thisVersion(getApplicationContext());
        getSupportActionBar().setSubtitle("Version:" + currentVersion);

        //
        // Load info text...
        //
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

                } catch (IOException io) {
                    Log.v("Info", io.toString());
                }
            }
        });
        t.start();

        /*
        //
        // Get this App's version from the play store...
        //
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {

                //
                // Active network available?
                //
                //
                // The following statement is true, when network is available, no ,matter if it is switched on or off!!!
                // This statement is noo good if one wants to check if a network connection is possible....
                //
                if (CheckForNetwork.isNetworkAvailable(getApplicationContext())) {
                    final String latestVersionInGooglePlay = getAppVersionfromGooglePlay(getApplicationContext());

                    if (latestVersionInGooglePlay != "-") {
                        if (latestVersionInGooglePlay.equals(currentVersion)) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //updateInfoView.setText(getResources().getText(R.string.version_info_is_latest_version));
                                    updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.version_info_ok) + "", 0));
                                }
                            });

                            // OK, could connect to network, could connect to google plays store listing of the app, get version.
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.version_info_update_available) + latestVersionInGooglePlay, 0));
                                }
                            });
                        }

                    } else
                        // Network was available but, could not retrieve version info from google plays store listing of this app.
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.no_version_info_available) + "", 0));
                            }
                        });
                } else
                    // No network connection available or network disabled on device.
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.no_network) + "", 0));
                        }
                    });
            }
        });
        t2.start();
         */

        //
        // Play core library
        // Checks if there is a newer version of this app available.
        //
        // todo: test of play core library
        // This is a test....
        //
        if (CheckForNetwork.isNetworkAvailable(getApplicationContext())) {

            final AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
            // Returns an intent object that you use to check for an update.
            final Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
            appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo result) {
                    if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        updateInfoView.setText(HtmlCompat.fromHtml(getResources().getText(R.string.version_info_update_available_short) +"", 0));

                    } else {
                        updateInfoView.setText(getResources().getText(R.string.version_info_ok));
                    }
                }
            });
        } else {
            updateInfoView.setText(getResources().getText(R.string.no_network));
        }
    }

    /**
     * Returns the version from the app's Google Play store listing...
     *
     * @param c
     * @return A String containing the version tag or, if errors: - char.
     */
    public String getAppVersionfromGooglePlay(Context c) {
        String latest;

        VersionChecker vc = new VersionChecker();

        try {
            latest = vc.execute().get();
        } catch (Exception e) {
            Log.v(tag,e.toString());
            latest="-";
        }
        return latest;
    }
}
