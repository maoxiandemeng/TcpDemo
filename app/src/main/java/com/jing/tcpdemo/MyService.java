package com.jing.tcpdemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MyService extends Service implements MqttCallback {
    private static final String TAG = "MyService";

    private static MqttAndroidClient client;
    private MqttConnectOptions connectOptions;

    //需要在同一网络下
    private String host = "tcp://192.168.40.214:61613";
    private String userName = "admin";
    private String passWord = "password";
    private static String myTopic = "topic";
    //这里clientId相当于用户id 唯一标示符的
    private String clientId = "test";

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        //服务器地址
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        client.setCallback(this);

        connectOptions = new MqttConnectOptions();
        //清楚缓存
        connectOptions.setCleanSession(true);
        //设置超时时间 单位：秒
        connectOptions.setConnectionTimeout(10);
        //设置心跳包发送间隔 单位：秒
        connectOptions.setKeepAliveInterval(20);
        //设置账号密码
        connectOptions.setUserName(userName);
        connectOptions.setPassword(passWord.toCharArray());

        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        String topic = myTopic;
        Integer qos = 0;
        Boolean retained = false;
        if ((!message.equals("")) || (!topic.equals(""))) {
            try {
                connectOptions.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                actionListener.onFailure(null, e);
            }
        }

        if (doConnect) {
            doClientConnection();
        }
    }

    /**
     * 连接Mqtt服务器
     */
    private void doClientConnection() {
        if (client !=null && !client.isConnected() && isConnectIsNormal()) {
            try {
                client.connect(connectOptions, null, actionListener);
            } catch (MqttException e) {
                Log.i(TAG, "doClientConnection: 连接失败");
                e.printStackTrace();
            }
        }
    }

    IMqttActionListener actionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.i(TAG, "onSuccess: 连接成功");
            try {
                client.subscribe(myTopic, 1);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.i(TAG, "onFailure: 连接失败");
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.i(TAG, "connectionLost: "+cause.getMessage());
    }

    /**
     * 接收消息
     * @param topic
     * @param message
     * @throws Exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String str = new String(message.getPayload());
        Log.i(TAG, "messageArrived: "+str + "qos: "+message.getQos() + "isRetained: " + message.isRetained());
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "deliveryComplete: ");
    }

    /** 判断网络是否连接 */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }

    /**
     * 向外提供一个方法测试用
     * @param msg
     */
    public static void publish(String msg){
        String topic = myTopic;
        Integer qos = 0;
        Boolean retained = false;
        try {
            client.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
