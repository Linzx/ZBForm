package com.zbform.penform.util;

import android.util.Log;

/**
 * Created by isaac on 2018/8/2.
 */
public class L {
    public static void D(boolean print, String tag, String content) {
        if (print)
            Log.d(tag, content);
    }

    public static void E(boolean print, String tag, String content) {
        if (print)
            Log.e(tag, content);
    }
}
