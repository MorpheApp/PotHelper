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

import android.content.Intent;
import android.graphics.Point;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Scanner;

@SuppressWarnings({"deprecation", "FieldCanBeLocal"})
public class MorpheFragment extends PreferenceFragment {
    private final String TAG = "morphe: MorpheFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);

        for (LicensePreference licensePref : LicensePreference.values()) {
            Preference pref = findPreference(licensePref.key);
            if (pref != null) {
                pref.setOnPreferenceClickListener(preference -> {
                    showLicenseDialog(licensePref);
                    return true;
                });
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            ListView listView = view.findViewById(android.R.id.list);
            if (listView != null) {
                View headerContainer = inflater.inflate(R.layout.header_container, listView, false);
                TextView textView = headerContainer.findViewById(R.id.header_app_version);
                if (textView != null) {
                    textView.setText(String.format(getResources().getString(R.string.app_version), BuildConfig.VERSION_NAME));
                }

                listView.addHeaderView(headerContainer, null, false);
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        final View rootView = getView();
        if (rootView == null) return;
        ListView listView = getView().findViewById(android.R.id.list);
        if (listView == null) return;
        listView.setDivider(null);
        listView.setDividerHeight(0);
    }

    @SuppressWarnings("CharsetObjectCanBeUsed")
    private void showLicenseDialog(LicensePreference licensePref) {
        Context context = getContext();
        Spanned content;
        String fileName = licensePref.name().toLowerCase() + ".html";
        try (InputStream is = context.getAssets().open(fileName)) {
            String text = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            content = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } catch (Exception ex) {
            Log.e(TAG, "openAssets failed", ex);
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        View contentView = inflater.inflate(R.layout.license_dialog, null);
        TextView licenseContentView = contentView.findViewById(R.id.license_content);
        licenseContentView.setText(handleBulletSpans(content, licensePref.useNumberFormat));

        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.DialogTheme)
                .setView(contentView)
                .setPositiveButton(R.string.license_dialog_ok_button_text, null)
                .setNeutralButton(R.string.license_dialog_source_button_text, (dialog, id) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(licensePref.url));
                    startActivity(intent);
                })
                .show();

        Window window = alertDialog.getWindow();
        if (window != null) {
            // Remove window background.
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            // If the message is too long, the dialog uses the entire vertical area of the screen.
            // Set the dialog height to 85% of the screen.
            if (licensePref.resizeDialog) {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                WindowManager.LayoutParams params = window.getAttributes();
                params.height = (int) (size.y * 0.85);
                window.setAttributes(params);
            }
        }
    }

    /**
     * 'Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)' was used to show HTML as a dialog message.
     * As a result, the attributes of most HTML tags, including the 'ul' tag, are ignored.
     * Since the 'li' tag is always converted to a bullet point, it must be replaced with the string used in the original license.
     *
     * @param spanned           Formatted HTML string.
     * @param useNumberFormat   Whether to replace 'li' tags with numbers.
     *                          If true, they will be replaced with 1, 2, 3.
     *                          If false, they will be replaced with a, b, c.
     * @return                  Replaced formatted HTML string.
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