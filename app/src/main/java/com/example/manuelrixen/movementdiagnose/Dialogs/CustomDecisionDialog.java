package com.example.manuelrixen.movementdiagnose.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.manuelrixen.movementdiagnose.R;


/**
 * Created by Manuel.Rixen on 05.09.2015.
 */
public class CustomDecisionDialog extends Dialog {

    private Context context;
    private EditText inputTextField;
    private Button yesButton, noButton, cancelButton;
    private TextView descriptionText;
    private boolean result;

    public CustomDecisionDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_decision);
        this.context = context;

        descriptionText = (TextView) this.findViewById(R.id.description);
        yesButton = (Button) this.findViewById(R.id.yes_button);
        noButton = (Button) this.findViewById(R.id.no_button);
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

    public void showDialog(String dialogHeader, View.OnClickListener yesButtonListener, View.OnClickListener noButtonListener) {
        int dialogWidth=0;
        int dialogHeight=0;
        if ((getRotation(context).equals("landscape")) || (getRotation(context).equals("reverse landscape"))) {
            dialogWidth = calcDimPercentageLandscape("width", 70, context);
            dialogHeight = calcDimPercentageLandscape("height", 30, context);
        }
        if (getRotation(context).equals("portrait")) {
            dialogWidth = calcDimPercentagePortrait("width", 70, context);
            dialogHeight = calcDimPercentagePortrait("height", 30, context);
        }
        Window dialogWindow = this.getWindow();
        dialogWindow.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.ModernWhite)));
        dialogWindow.setTitleColor(context.getResources().getColor(R.color.ModernWhite));
        dialogWindow.setLayout(dialogWidth, dialogHeight);

        result = false;
        descriptionText.setText(dialogHeader);

        yesButton.setOnClickListener(yesButtonListener);
        noButton.setOnClickListener(noButtonListener);
        this.show();
    }

    public void recreateDialog() {
//        showDialog();
    }

}