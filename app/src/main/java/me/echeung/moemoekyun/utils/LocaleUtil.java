package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Locale;

import me.echeung.moemoekyun.App;

public class LocaleUtil {

    public static final String DEFAULT = "default";

    public static Locale getLocale(@NonNull Context context) {
        final Configuration config = context.getResources().getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return config.getLocales().get(0);
        } else {
            return config.locale;
        }
    }

    public static Context setLocale(@NonNull Context context) {
        if (App.getPreferenceUtil() == null) {
            return context;
        }

        final String language = App.getPreferenceUtil().getLanguage();
        return setLocale(context, language);
    }

    public static Context setLocale(@NonNull Context context, String language) {
        final Resources res = context.getResources();
        final Configuration config = new Configuration(res.getConfiguration());

        if (!language.equals(DEFAULT)) {
            final Locale locale = new Locale(language);
            Locale.setDefault(locale);
            config.setLocale(locale);
        }

        context = context.createConfigurationContext(config);
        return context;
    }
}
