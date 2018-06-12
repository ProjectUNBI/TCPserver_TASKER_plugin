package com.unbi.tcpserver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static com.unbi.tcpserver.MainActivity.SERVER_PORT;
import static com.unbi.tcpserver.MainActivity.booltoast;
import static com.unbi.tcpserver.MainActivity.msg;

public class TCPservice extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onStart(Intent intent, int startId) {

        Log.d("LOG HERE","Intent");

//        int mNotificationId = 001;
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(getApplicationContext())
//                        .setSmallIcon(R.drawable.ic_stat_router)
//                        .setContentTitle("TCP server")
//                        .setContentText("Running.....")
//                        .setOngoing(true);
//        NotificationManager nmnger=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        nmnger.notify(mNotificationId,mBuilder.build());


        new Thread(new Runnable() {

            @Override
            public void run() {


                try {
                    ServerSocket socServer = new ServerSocket(SERVER_PORT);
                    Socket socClient = null;
                    while (true) {
                        socClient = socServer.accept();
                        TCPservice.ServerAsyncTask serverAsyncTask = new TCPservice.ServerAsyncTask();
                        serverAsyncTask.execute(new Socket[] { socClient });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();




    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification();
        return START_STICKY;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, TCPservice.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        int ONGOING_NOTIFICATION_ID = 001;
        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle("TCP server")
                        .setContentText("Running....")
                        .setSmallIcon(R.drawable.ic_stat_router)
                        .setContentIntent(pendingIntent)
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        NotificationManager noti = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        noti.cancel(001);

    }



    /**
     * AsyncTask which handles the commiunication with clients
     */
    class ServerAsyncTask extends AsyncTask<Socket, Void, String> {
        @Override
        protected String doInBackground(Socket... params) {
//            Log.d("LOG HERE","SERVER CONNECTED");
            //TODO Here is the server coonected
            String result = null;
            Socket mySocket = params[0];
            try {

                InputStream is = mySocket.getInputStream();
                PrintWriter out = new PrintWriter(mySocket.getOutputStream(),
                        true);

                //out.println("Welcome to \""+Server_Name+"\" Server");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));

                result = br.readLine();

                //mySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                mySocket.close();
            } catch (IOException e) {

//                Log.d("LOG HERE","CLOSE ERROR");
            }


            Log.d("LOG HERE","SERVER CLOSE");
            return result;
        }


        @Override
        protected void onPostExecute(String s) {

            //TODO HERE IS THE MSG RECEIVED

            Log.d("LOG HERE",s+"\n");
            msg=s;
            Intent intent = new Intent();
            intent.setAction("Intent.unbi.tcpserver.TCP_MSG");
            intent.putExtra("tcpmsg", s);
            sendBroadcast(intent);
            new Thread(){
                public void run() {
                    Object result=null;


                    Looper l = Looper.getMainLooper();
                    Handler h = new Handler(l);
                    h.post(new Runnable() {
                        @Override
                        public void run(){
                            //update ui here
                            // display toast here
                            if(booltoast){
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                        }
                    });
                };
            }.start();

//            runOnUiThread(new Runnable(){
//
//                @Override
//                public void run(){
//                    //update ui here
//                    // display toast here
//                    if(booltoast){
//                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();}
//                }
//            });



        }

    }

}