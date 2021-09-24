package com.payu.payuui.SdkuiUtil;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SdkuiUtils {

    public static void hideSoftKeyboard(Activity context) {
        if (context != null && !context.isDestroyed()) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = context.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(context);
            }
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
