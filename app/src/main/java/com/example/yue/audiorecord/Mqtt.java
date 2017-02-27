package com.example.yue.audiorecord;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yue on 2017/2/10.
 */

public class Mqtt {

    private final static int FLAG_WAV = 0;
    private final static int FLAG_AMR = 1;
    private static final String TAG1 = "Mqtt";

    private int mState = -1;    //-1:没再录制，0：录制wav，1：录制amr

    MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://iot.eclipse.org:1883";
    String clientID;
    String subscriptionTopic;
    public void mqttInit(String clientID, String topic, Context c){//初始化mqtt，输入自身ID
        subscriptionTopic = topic;
        mqttAndroidClient = new MqttAndroidClient(c, serverUri, clientID);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if(reconnect){
                    Log.d(TAG1, "connectComplete: ");
                    subscribeToTopic();
                }else {

                }
            }
            @Override
            public void connectionLost(Throwable cause) {

            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    return;
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }
    public void subscribeToTopic(){//订阅topic
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("subscribe successfully");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(subscriptionTopic, 1, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage cipherMessage) throws Exception {//接收到信息，进行处理
                    Log.d(TAG1, "messageArrived: " + new String(cipherMessage.getPayload()) +"\n" + new Date().toString()+" "+new Date().getTime());

                    long timeSet = Long.valueOf(new String(cipherMessage.getPayload()));
                    int mResult = -1;
                    long time_sp = timeSet - new Date().getTime()-300;
                    if(time_sp < 0){
                        Log.d(TAG1, "messageArrived: error------ time short ");
                        return;
                    }
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                            Log.d(TAG1, "run: begin " + new Date().toString()+" "+ new Date().getTime());
                            mRecord_1.stopRecordAndFile();
                            //Log.d(TAG1, "run: end " + new Date().toString()+" "+ new Date().getTime());

                            long mSize = mRecord_1.getRecordFileSize();
                            Log.d("Mqtt","录音已停止.录音文件:"+mRecord_1.getWavFilePath()+"\n文件大小："+mSize);

                        }
                    },time_sp+5*1000+5);

                    Timer timer2 = new Timer();
                    timer2.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                            Log.d(TAG1, "messageArrived: begin： " + new Date().toString()+" "+ new Date().getTime());
                            int mResult = mRecord_1.startRecordAndFile();
                            //Log.d(TAG1, "messageArrived: end: " + new Date().toString()+" "+ new Date().getTime());
                        }
                    },time_sp);
/*
                    AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                    Log.d(TAG1, "messageArrived: begin： " + new Date().toString()+" "+ new Date().getTime());
                    mResult = mRecord_1.startRecordAndFile();
                    Log.d(TAG1, "messageArrived: end: " + new Date().toString()+" "+ new Date().getTime());
*/
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }
    public void publishMessage(String ID, String mes){//推送信息，输入对方ID
        try {
            String topic = "jiamitongxin"+ID;
            MqttMessage message = new MqttMessage();
            message.setPayload(mes.getBytes());
            mqttAndroidClient.publish(topic, message);
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void notification(){

    }


}
