package com.example.manuelrixen.movementdiagnose.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

import com.example.manuelrixen.movementdiagnose.R;


/**
 * Created by Manuel.Rixen on 05.09.2015.
 */
public class CustomAboutDialog extends Dialog {

    private Context context;
    int dialogWidth, dialogHeight;

    public CustomAboutDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_about);
        this.context = context;
    }

    public String getRotation(Context context){
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return "portrait";
            case Surface.ROTATION_90:
                return "landscape";
            case Surface.ROTATION_180:
                return "reverse portrait";
            default:
                return "reverse landscape";
        }
    }

    public int calcDimPercentageLandscape(String dimType, int dimPercentage, Context context) {
        // Calculate display size
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        int pxWidth = displayMetrics.widthPixels;
        int pxHeight = displayMetrics.heightPixels;
        Log.d("sizeL11", String.valueOf(pxWidth));
        Log.d("sizeL22", String.valueOf(pxHeight));
            if (dimType.equals("width")) return (pxWidth / 100) * dimPercentage;
            if (dimType.equals("height")) return (pxHeight / 100) * dimPercentage;

        else return 0;
    }

    public int calcDimPercentagePortrait(String dimType, int dimPercentage, Context context) {
        // Calculate display size
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        int pxWidth = displayMetrics.widthPixels;
        int pxHeight = displayMetrics.heightPixels;
        if (dimType.equals("width")) return (pxWidth / 100) * dimPercentage;
        if (dimType.equals("height")) return (pxHeight / 100) * dimPercentage;
        else return 0;
    }

    public void showDialog() {
        if ((getRotation(context).equals("landscape")) || (getRotation(context).equals("reverse landscape"))) {
            dialogWidth = calcDimPercentageLandscape("width", 90, context);
            dialogHeight = calcDimPercentageLandscape("height", 60, context);
        }
        if (getRotation(context).equals("portrait")) {
            dialogWidth = calcDimPercentagePortrait("width", 90, context);
            dialogHeight = calcDimPercentagePortrait("height", 60, context);
        }

        Window dialogWindow = this.getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.ModernWhite)));
        dialogWindow.setTitleColor(context.getResources().getColor(R.color.ModernWhite));
        dialogWindow.setLayout(dialogWidth, dialogHeight);

        this.show();
    }
}