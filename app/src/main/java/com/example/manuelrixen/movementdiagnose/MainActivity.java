package com.example.manuelrixen.movementdiagnose;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.manuelrixen.movementdiagnose.Dialogs.CustomAboutDialog;
import com.example.manuelrixen.movementdiagnose.Dialogs.CustomDecisionDialog;
import com.example.manuelrixen.movementdiagnose.Dialogs.CustomInputDialog;
import com.example.manuelrixen.movementdiagnose.Socket.Receiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Process.myPid;

public class MainActivity extends Activity implements Receiver.EventListener, View.OnClickListener {

    private final long maxActivityShowTime = 3000;
    private PowerManager.WakeLock wl;
    private TabHost tabHost;
    private BaseData baseData;
    private boolean alreadyShown;
    private boolean firstStart = true;
    private CustomAboutDialog customAboutDialog;
    private NetworkInfo mWifi;
    private SharedPreferences sharedPreferences;
    private CustomDecisionDialog customDecisionDialog;
    private CustomInputDialog customInputDialog;
    private boolean useLastConnection = false;
    private SharedPreferences.Editor editor;
    private boolean setConnectionDataManually = false;
    private EditText ipField, portField;
    private TextView connectToField;
    private File currentDirectory;
    private String currentfilename;

    private TextView ID_1_x_textView, ID_1_y_textView, ID_1_z_textView;
    private TextView ID_2_x_textView, ID_2_y_textView, ID_2_z_textView;
    private TextView ID_3_x_textView, ID_3_y_textView, ID_3_z_textView;
    private TextView ID_4_x_textView, ID_4_y_textView, ID_4_z_textView;
    private Receiver receiver;


    // TODO Check why zonenbahn-fehler isnt shown as event

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        // add the custom view to the action bar
        actionBar.setCustomView(R.layout.custom_actionbar);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);

        connectToField = (TextView) actionBar.getCustomView().findViewById(R.id.connectTo);
        ID_1_x_textView = (TextView) findViewById(R.id.ID_1_x_value);
        ID_1_y_textView = (TextView) findViewById(R.id.ID_1_y_value);
        ID_1_z_textView = (TextView) findViewById(R.id.ID_1_z_value);

        ID_2_x_textView = (TextView) findViewById(R.id.ID_2_x_value);
        ID_2_y_textView = (TextView) findViewById(R.id.ID_2_y_value);
        ID_2_z_textView = (TextView) findViewById(R.id.ID_2_z_value);

        ID_3_x_textView = (TextView) findViewById(R.id.ID_3_x_value);
        ID_3_y_textView = (TextView) findViewById(R.id.ID_3_y_value);
        ID_3_z_textView = (TextView) findViewById(R.id.ID_3_z_value);

        ID_4_x_textView = (TextView) findViewById(R.id.ID_4_x_value);
        ID_4_y_textView = (TextView) findViewById(R.id.ID_4_y_value);
        ID_4_z_textView = (TextView) findViewById(R.id.ID_4_z_value);

        baseData = new BaseData(this, this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        customDecisionDialog = new CustomDecisionDialog(this);
        customInputDialog = new CustomInputDialog(this, this);

        customAboutDialog = new CustomAboutDialog(this);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

    }

    @Override
    protected void onResume() {
        super.onResume();
        final String ip = sharedPreferences.getString("ip", "0");
        final String port = sharedPreferences.getString("port", "0");
        alreadyShown = sharedPreferences.getBoolean("alreadyShown", false);
        firstStart = sharedPreferences.getBoolean("firstStart", true);
        if ( !(ip.equals("0")) && !(port.equals("0")) && alreadyShown){
            // Show user dialog to ask for manual input
            customDecisionDialog.showDialog("Use last connection with ip: "+ip+" and port: "+port+"?", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    useLastConnection = true;
                    connectToField.setText("IP: " + ip + "\n" + "Port: " + port);
                    startReceiving(ip, port);
                    customDecisionDialog.dismiss();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setConnectionDataManually = true;
                    showDialogForManuallyInput();
                    customDecisionDialog.dismiss();
                }
            });
        }

        else {
            if (firstStart) {
                setConnectionDataManually = true;
                showDialogForManuallyInput();
                firstStart = false;
            }
            else{
                setConnectionDataManually = true;
                showDialogForManuallyInput();
            }
        }

        if (!mWifi.isConnected()) {
            Toast.makeText(this, R.string.errMsgWlan, Toast.LENGTH_LONG).show();
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    finish();
                }
            }, maxActivityShowTime);
        }
    }

    private void showDialogForManuallyInput(){
        if (setConnectionDataManually){
            // Show user dialog to choose between last and new connection
            customInputDialog.showDialog("Set IP and PORT manually.", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Take the manually input and set ip and port
                    // TODO Check ip port formatting
                    String ip = ipField.getText().toString();
                    String port = portField.getText().toString();
                    if (!(ip.equals("")) && !(port.equals(""))) {
                        startReceiving(ipField.getText().toString(), portField.getText().toString());
                        setConnectionDataManually = false;
                        saveConnectionData(ip, port);
                        customInputDialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "Connection data not set", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Cancel manually input and show barcode reader
                    useLastConnection = false;
                    customInputDialog.dismiss();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor = sharedPreferences.edit();
        editor.putBoolean("alreadyShown", true);
        editor.putBoolean("firstStart", firstStart);
        editor.apply();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        editor = sharedPreferences.edit();
        editor.putBoolean("alreadyShown", false);
        editor.apply();
    }

    private void saveConnectionData(String ip, String port) {
        editor = sharedPreferences.edit();
        editor.putString("ip", ip);
        editor.putString("port", port);
        editor.apply();
    }

    private void startReceiving(String ip, String port) {
        baseData.startReceiver(ip, port);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wl.release();
        if (baseData.getReceiver() != null) baseData.getReceiver().stopRunRoutine();
        android.os.Process.killProcess(myPid());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.popup_about):
                customAboutDialog.showDialog();
                break;
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (customAboutDialog.isShowing()){
            customAboutDialog.showDialog();
        }
    }

    public void setTextFields(EditText ipField, EditText portField){
        this.ipField = ipField;
        this.portField = portField;
    }

    @Override
    public void onError() {
        ID_1_x_textView.setText(getResources().getString(R.string.x_value) + "error");
        ID_1_y_textView.setText(getResources().getString(R.string.y_value) + "error");
        ID_1_z_textView.setText(getResources().getString(R.string.z_value) + "error");

        ID_2_x_textView.setText(getResources().getString(R.string.x_value) + "error");
        ID_2_y_textView.setText(getResources().getString(R.string.y_value) + "error");
        ID_2_z_textView.setText(getResources().getString(R.string.z_value) + "error");

        ID_3_x_textView.setText(getResources().getString(R.string.x_value) + "error");
        ID_3_y_textView.setText(getResources().getString(R.string.y_value) + "error");
        ID_3_z_textView.setText(getResources().getString(R.string.z_value) + "error");

        ID_4_x_textView.setText(getResources().getString(R.string.x_value) + "error");
        ID_4_y_textView.setText(getResources().getString(R.string.y_value) + "error");
        ID_4_z_textView.setText(getResources().getString(R.string.z_value) + "error");
    }

    @Override
    public void onEvent(String msgType, String msg) {
        msg = msg.replace("x","");
        msg = msg.replace("y","");
        msg = msg.replace("z","");
        String[] tempMessage = msg.split(":");

        // Write data to file like this: id;x;y;z
        String filename = generateFilename();
        writeToFile(filename, msgType + ";" + tempMessage[0] + ";" + tempMessage[1] + ";" + tempMessage[2]);

        showMessage(msgType, tempMessage);
    }

    private void showMessage(String msgType, String[] msg) {
        if (msgType.equals("1")) {
            ID_1_x_textView.setText(getResources().getString(R.string.x_value) + " " + msg[0]);
            ID_1_y_textView.setText(getResources().getString(R.string.y_value) + " " + msg[1]);
            ID_1_z_textView.setText(getResources().getString(R.string.z_value) + " " + msg[2]);
        }

        if (msgType.equals("2")){
            ID_2_x_textView.setText(getResources().getString(R.string.x_value) + " " + msg[0]);
            ID_2_y_textView.setText(getResources().getString(R.string.y_value) + " " + msg[1]);
            ID_2_z_textView.setText(getResources().getString(R.string.z_value) + " " + msg[2]);
        }

        if (msgType.equals("3")){
            ID_3_x_textView.setText(getResources().getString(R.string.x_value) + " " + msg[0]);
            ID_3_y_textView.setText(getResources().getString(R.string.y_value) + " " + msg[1]);
            ID_3_z_textView.setText(getResources().getString(R.string.z_value) + " " + msg[2]);
        }

        if (msgType.equals("4")){
            ID_4_x_textView.setText(getResources().getString(R.string.x_value) + " " + msg[0]);
            ID_4_y_textView.setText(getResources().getString(R.string.y_value) + " " + msg[1]);
            ID_4_z_textView.setText(getResources().getString(R.string.z_value) + " " + msg[2]);
        }
    }

    @Override
    public void onClick(View v) {
        // Start registering to listener
        if (receiver == null) {
            try {
                receiver = baseData.getReceiver();
                receiver.registerListener(this);
                Toast.makeText(this, R.string.startRegListenerSuccess, Toast.LENGTH_LONG).show();
            } catch (NullPointerException e) {
                Toast.makeText(this, R.string.startRegListenerError, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void writeToFile(String filename, String data) {
        // Create internal directory
        File directoryName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        currentDirectory = directoryName;
        currentfilename = filename;

        File file = new File(directoryName, filename);
        try {
            if (!file.exists()){
                file.createNewFile();
                writing(file, "Context;Durance", false);
                Log.d("time_date", "createNewFile");
            }

            writing(file, data, true);

        } catch (IOException e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void writing(File file, String data, boolean showSaveState) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.append(data);
            outputStreamWriter.append("\n\r");
            outputStreamWriter.close();
            if (showSaveState) Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String generateFilename() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Log.d("time_date", dateFormat.toString());
        String date = dateFormat.format(calendar.getTime());
        return date+".txt";
    }

    public void onSendButtonClicked(View view) {

//        String filelocation="/storage/emulated/0/Download/"+generateFilename();
//        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                "mailto", "manleo.rixen@gmail.com", null));
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//        startActivity(Intent.createChooser(emailIntent, "Send email..."));

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"manleo.rixen@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
        intent.putExtra(Intent.EXTRA_TEXT, "body text");
        File directoryName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(directoryName, generateFilename());
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Uri uri = Uri.parse("file://" + file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Send email..."));
    }

}
