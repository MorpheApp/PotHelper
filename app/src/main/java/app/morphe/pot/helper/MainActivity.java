/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/PotHelper
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.morphe.pot.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Scanner;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
    private static final String TAG = "morphe: MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration config = getResources().getConfiguration();
        int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkModeEnabled = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        // Otherwise the system paints its own opaque scrim over the bars.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        // Creates the decor view, which the insets controller needs.
        View decorView = window.getDecorView();

        // A light flag means the bar background is light, so it belongs to the light theme.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                int lightBars = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                insetsController.setSystemBarsAppearance(isDarkModeEnabled ? 0 : lightBars, lightBars);
            }
        } else {
            int lightBars = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            int visibility = decorView.getSystemUiVisibility();
            decorView.setSystemUiVisibility(isDarkModeEnabled
                    ? visibility & ~lightBars
                    : visibility | lightBars);
        }

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorBackground, typedValue, true);

        View contentView = findViewById(android.R.id.content);
        contentView.setBackgroundColor(typedValue.data);

        // The window is edge to edge.
        contentView.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else {
                view.setPadding(
                        insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom()
                );
            }
            return insets;
        });

        setContentView(R.layout.about);

        TextView versionView = findViewById(R.id.app_version);
        versionView.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));

        bind(findViewById(R.id.about_content));

        Switch hideIconSwitch = findViewById(R.id.hide_icon_switch);
        hideIconSwitch.setChecked(isLauncherIconHidden());
        hideIconSwitch.setOnCheckedChangeListener((view, hidden) -> setLauncherIconHidden(hidden));
        findViewById(R.id.hide_icon).setOnClickListener(view -> hideIconSwitch.toggle());
    }

    /**
     * Makes every tagged row tappable. A tag is either a link, or the name
     * of a license asset followed by the URL of its source.
     */
    private void bind(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof String) {
                child.setOnClickListener(view -> open((String) tag));
            } else if (child instanceof ViewGroup && !child.isClickable()) {
                // Only a card is clipped, so that the ripple of its rows follows the rounded
                // corners. Clipping a row would hide it, because its own ripple has no outline.
                child.setClipToOutline(true);
                bind((ViewGroup) child);
            }
        }
    }

    private ComponentName launcherAlias() {
        return new ComponentName(this, getPackageName() + ".Launcher");
    }

    private boolean isLauncherIconHidden() {
        return getPackageManager().getComponentEnabledSetting(launcherAlias())
                == PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }

    private void setLauncherIconHidden(boolean hidden) {
        getPackageManager().setComponentEnabledSetting(
                launcherAlias(),
                hidden
                        ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void open(String tag) {
        if (tag.indexOf(' ') < 0) {
            browse(tag);
        } else {
            showLicenseDialog(tag);
        }
    }

    private void browse(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "No app can open: " + url, ex);
        }
    }

    /**
     * Shows the license of a row tagged with an asset name, the URL of its source,
     * and optionally the markers 'numbered' and 'tall'.
     */
    @SuppressWarnings("CharsetObjectCanBeUsed")
    private void showLicenseDialog(String tag) {
        String[] parts = tag.split(" ");
        String asset = parts[0];
        String sourceUrl = parts[1];
        boolean useNumberFormat = tag.contains(" numbered");
        boolean tallDialog = tag.contains(" tall");

        Spanned content;
        try (InputStream is = getAssets().open(asset + ".html")) {
            String text = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            content = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } catch (Exception ex) {
            Log.e(TAG, "openAssets failed", ex);
            return;
        }

        View contentView = getLayoutInflater().inflate(R.layout.license_dialog, null);
        TextView licenseContentView = contentView.findViewById(R.id.license_content);
        licenseContentView.setText(handleBulletSpans(content, useNumberFormat));

        AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setView(contentView)
                .setPositiveButton(R.string.license_dialog_ok_button_text, null)
                .setNeutralButton(R.string.license_dialog_source_button_text,
                        (dialog, id) -> browse(sourceUrl))
                .show();

        Window window = alertDialog.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            // A long notice would otherwise stretch the dialog over the whole screen.
            if (tallDialog) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
                window.setAttributes(params);
            }
        }
    }

    /**
     * Html.fromHtml turns every 'li' tag into a bullet point and drops the list type,
     * so the bullets are replaced with the numbering of the original license:
     * 1, 2, 3 when useNumberFormat is set, a, b, c otherwise.
     */
    private Spanned handleBulletSpans(Spanned spanned, boolean useNumberFormat) {
        SpannableStringBuilder builder = new SpannableStringBuilder(spanned);
        BulletSpan[] bulletSpans = builder.getSpans(0, builder.length(), BulletSpan.class);

        for (int i = 0; i < bulletSpans.length; i++) {
            BulletSpan bulletSpan = bulletSpans[i];
            int start = builder.getSpanStart(bulletSpan);
            builder.removeSpan(bulletSpan);
            String prefix = useNumberFormat
                    ? (i + 1) + ". "
                    : (char) ('a' + (i % 26)) + ". ";
            builder.insert(start, prefix);
        }

        return builder;
    }
}
