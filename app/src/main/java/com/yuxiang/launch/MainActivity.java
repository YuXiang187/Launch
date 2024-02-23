package com.yuxiang.launch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    int tab_position;
    File YS_CNFolder, YS_OSFolder, SR_CNFolder, SR_OSFolder;
    SharedPreferences themePreferences, tabPreferences;
    MaterialToolbar topBar;
    TabLayout tab;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        YS_CNFolder = new File("/storage/emulated/0/Android/data/com.miHoYo.Yuanshen");
        YS_OSFolder = new File("/storage/emulated/0/Android/data/com.miHoYo.GenshinImpact");
        SR_CNFolder = new File("/storage/emulated/0/Android/data/com.miHoYo.hkrpg");
        SR_OSFolder = new File("/storage/emulated/0/Android/data/com.HoYoverse.hkrpgoversea");

        topBar = findViewById(R.id.topAppBar);
        tab = findViewById(R.id.tab);
        fab = findViewById(R.id.launch_fab);

        themePreferences = this.getSharedPreferences("theme", Context.MODE_PRIVATE);
        tabPreferences = this.getSharedPreferences("tab", Context.MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(themePreferences.getInt("theme", 0) == 0 ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : themePreferences.getInt("theme", 0));

        setSupportActionBar(topBar);

        tab_position = tabPreferences.getInt("tab", 0);
        tab.selectTab(tab.getTabAt(tab_position));
        if (tab_position == 0) {
            checkFolder(YS_CNFolder, YS_OSFolder);
        } else if (tab_position == 1) {
            checkFolder(SR_CNFolder, SR_OSFolder);
        }

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                SharedPreferences.Editor editor = tabPreferences.edit();
                switch (tab.getPosition()) {
                    case 0:
                        tab_position = 0;
                        checkFolder(YS_CNFolder, YS_OSFolder);
                        break;
                    case 1:
                        tab_position = 1;
                        checkFolder(SR_CNFolder, SR_OSFolder);
                        break;
                }
                editor.putInt("tab", tab_position);
                editor.apply();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        topBar.setNavigationOnClickListener(v -> {
            if (tab_position == 0) {
                renameFolder(YS_CNFolder, YS_OSFolder);
            } else if (tab_position == 1) {
                renameFolder(SR_CNFolder, SR_OSFolder);
            }
        });

        fab.setOnClickListener(v -> {
            if (tab_position == 0) {
                if (isFolderExist(YS_CNFolder)) {
                    launchApp(MainActivity.this, "com.miHoYo.Yuanshen");
                } else if (isFolderExist(YS_OSFolder)) {
                    launchApp(MainActivity.this, "com.miHoYo.GenshinImpact");
                }
            } else if (tab_position == 1) {
                if (isFolderExist(SR_CNFolder)) {
                    launchApp(MainActivity.this, "com.miHoYo.hkrpg");
                } else if (isFolderExist(SR_OSFolder)) {
                    launchApp(MainActivity.this, "com.HoYoverse.hkrpgoversea");
                }
            }
        });
    }

    private void showErrorBar(String text) {
        Snackbar.make(fab, text + "\n" +
                getString(R.string.error_permission_text), Snackbar.LENGTH_INDEFINITE).setAction(R.string.setting, v1 -> openAppSettings(MainActivity.this)).show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Snackbar.make(fab, R.string.error_open_setting_text, Snackbar.LENGTH_LONG).show();
        }
    }

    public void launchApp(Context context, String packageName) {
        if (isAppInstalled(context, packageName)) {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                context.startActivity(launchIntent);
                exit();
            } else {
                Snackbar.make(fab, R.string.error_launch_app_text, Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(fab, R.string.error_exist_app_text, Snackbar.LENGTH_LONG).show();
        }
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isFolderExist(File folder) {
        return (folder.exists() && folder.isDirectory());
    }

    private void checkFolder(File CNFolder, File OSFolder) {
        if (isFolderExist(CNFolder)) {
            topBar.setSubtitle(R.string.official_server);
        } else if (isFolderExist(OSFolder)) {
            topBar.setSubtitle(R.string.international_server);
        } else {
            topBar.setSubtitle(R.string.no_server);
        }
    }

    private void renameFolder(File CNFolder, File OSFolder) {
        if (isFolderExist(CNFolder)) {
            if (CNFolder.renameTo(OSFolder)) {
                topBar.setSubtitle(R.string.international_server);
            } else {
                showErrorBar(getString(R.string.error_official_switch_text));
            }
        } else if (isFolderExist(OSFolder)) {
            if (OSFolder.renameTo(CNFolder)) {
                topBar.setSubtitle(R.string.official_server);
            } else {
                showErrorBar(getString(R.string.error_international_switch_text));
            }
        } else {
            showErrorBar(getString(R.string.error_exist_data_text));
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_theme) {
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle(getString(R.string.theme))
                    .setSingleChoiceItems(new String[]{getString(R.string.theme_system), getString(R.string.theme_white), getString(R.string.theme_black)}, themePreferences.getInt("theme", 0), (dialogInterface, i) -> {
                        SharedPreferences.Editor editor = themePreferences.edit();
                        editor.putInt("theme", i);
                        editor.apply();
                        AppCompatDelegate.setDefaultNightMode(i == 0 ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : i);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else if (id == R.id.menu_explain) {
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle(R.string.explain)
                    .setMessage(R.string.explain_text)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void exit() {
        finish();
        System.exit(0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}