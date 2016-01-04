package org.xdty.callerinfo;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import java.util.Arrays;
import java.util.List;

import app.minimize.com.seek_bar_compat.SeekBarCompat;

import static org.xdty.callerinfo.utils.Utils.closeWindow;
import static org.xdty.callerinfo.utils.Utils.mask;
import static org.xdty.callerinfo.utils.Utils.sendData;
import static org.xdty.callerinfo.utils.Utils.showTextWindow;

public class SettingsActivity extends AppCompatActivity {

    public final static String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment {

        SharedPreferences sharedPrefs;
        Preference bdApiPreference;
        Preference jhApiPreference;
        Preference textSizePref;
        Preference winTransPref;
        Preference apiTypePref;
        String baiduApiKey;
        String juheApiKey;
        String textSizeKey;
        String windowTransKey;
        String apiTypeKey;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            sharedPrefs = getPreferenceManager().getSharedPreferences();

            Preference version = findPreference(getString(R.string.version_key));
            String versionString = BuildConfig.VERSION_NAME;
            if (BuildConfig.DEBUG) {
                versionString += "." + BuildConfig.BUILD_TYPE;
            }
            version.setSummary(versionString);

            baiduApiKey = getString(R.string.baidu_api_key);

            bdApiPreference = findPreference(baiduApiKey);
            bdApiPreference.setSummary(mask(sharedPrefs.getString(baiduApiKey, "")));

            bdApiPreference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            showApiDialog(baiduApiKey, R.string.custom_bd_api_key);
                            return true;
                        }
                    });

            juheApiKey = getString(R.string.juhe_api_key);

            jhApiPreference = findPreference(juheApiKey);
            jhApiPreference.setSummary(mask(sharedPrefs.getString(juheApiKey, "")));

            jhApiPreference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            showApiDialog(juheApiKey, R.string.custom_jh_api_key);
                            return true;
                        }
                    });

            textSizeKey = getString(R.string.window_text_size_key);

            textSizePref = findPreference(textSizeKey);
            textSizePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showSeekBarDialog(textSizeKey, FloatWindow.TEXT_SIZE, 20, 60,
                            R.string.window_text_size, R.string.text_size);
                    return true;
                }
            });

            windowTransKey = getString(R.string.window_transparent_key);
            winTransPref = findPreference(windowTransKey);
            winTransPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showSeekBarDialog(windowTransKey, FloatWindow.WINDOW_TRANS, 80, 100,
                            R.string.window_transparent, R.string.text_transparent);
                    return true;
                }
            });

            apiTypeKey = getString(R.string.api_type_key);
            apiTypePref = findPreference(apiTypeKey);

            final List<String> apiList = Arrays.asList(
                    getResources().getStringArray(R.array.api_type));

            apiTypePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showRadioDialog(apiTypeKey, R.string.api_type, apiList);
                    return true;
                }
            });
            apiTypePref.setSummary(apiList.get(sharedPrefs.getInt(apiTypeKey, 0)));
        }

        private void showSeekBarDialog(final String key, final String bundleKey, int defaultValue,
                int max, int title, int textRes) {
            int value = sharedPrefs.getInt(key, defaultValue);
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(title));
            View layout = getActivity().getLayoutInflater().inflate(R.layout.dialog_seek, null);
            builder.setView(layout);

            final SeekBarCompat seekBar = (SeekBarCompat) layout.findViewById(R.id.seek_bar);
            seekBar.setProgress(value);
            seekBar.setMax(max);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 0) {
                        progress = 1;
                    }
                    sendData(getActivity(), bundleKey, progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int value = seekBar.getProgress();
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putInt(key, value);
                    editor.apply();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    closeWindow(getActivity());
                }
            });
            builder.show();

            showTextWindow(getActivity(), textRes);
        }

        private void showApiDialog(final String key, int title) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(title));
            View layout = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit, null);
            builder.setView(layout);

            final EditText editText = (EditText) layout.findViewById(R.id.text);
            editText.setText(sharedPrefs.getString(key, ""));

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String value = editText.getText().toString();
                    findPreference(key).setSummary(mask(value));
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(key, value);
                    editor.apply();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setCancelable(false);
            builder.show();
        }

        private void showRadioDialog(final String key, int title, final List<String> list) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(title));
            View layout = getActivity().getLayoutInflater().inflate(R.layout.dialog_radio, null);
            builder.setView(layout);
            final AlertDialog dialog = builder.create();

            final RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.radio);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            for (String s : list) {
                RadioButton radioButton = new RadioButton(getActivity());
                radioButton.setText(s);
                radioGroup.addView(radioButton, layoutParams);
            }

            RadioButton button = ((RadioButton) radioGroup.getChildAt(sharedPrefs.getInt(key, 0)));
            button.setChecked(true);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int index = group.indexOfChild(group.findViewById(checkedId));
                    findPreference(key).setSummary(list.get(index));
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putInt(key, index);
                    editor.apply();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }
}