package com.example.manuelrixen.movementdiagnose.Socket;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Receiver implements Runnable {

    private final Context context;
    private final String ip;
    private final String port;
    private boolean isRunning = true;
    private NetClient nc;
    private String data[] = new String[]{"", ""};
    private Activity activity; // = new Activity();
    private ArrayList<EventListener> listeners = new ArrayList<>();
    private long maxActivityShowTime = 3000;
    private String CLIENT_NAME = "Phone;";

    public Receiver(Context context, String ip, String port, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.ip = ip;
        this.port = port;
        if (nc == null) nc = new NetClient(this.ip, Integer.parseInt(this.port));
    }

    public void stopRunRoutine() {
        isRunning = false;
    }

    public void run() {
        if (nc.connectWithServer()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Connected to ip " + ip + " and port " + port, Toast.LENGTH_SHORT).show();
                }
            });
            // Send client name to server
//            while(isRunning) {
//                nc.sendDataAsString(CLIENT_NAME);
//                data = nc.receiveDataFromServer();
//                if ((data[0] != null) && (data[1] != null))  isRunning = false;
//            }
            isRunning = true;
            while (isRunning) {
                data = nc.receiveDataFromServer();
                if ((data[0] != null) && (data[1] != null)) {
//                    nc.sendDataAsString("1");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                    for (EventListener eventListener : listeners)
                                        eventListener.onEvent(data[1], data[0]);
                            } catch (NullPointerException e) {
                                Log.d("Exception:run", String.valueOf(e));
                            }
                        }
                    });
                }
                else{
                    // Send same message again
//                    nc.sendDataAsString("0");
                }
            }
            nc.disConnectWithServer();
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(context, R.string.connectionError, Toast.LENGTH_LONG).show();
                    Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.finish();
                        }
                    }, maxActivityShowTime);
                }
            });
        }
    }

    public void registerListener(EventListener listener) {
        this.listeners.add(listener);
    }

    public interface EventListener {
        void onEvent(String data1, String data2);

        void onError();
    }
}
