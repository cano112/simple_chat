package pl.edu.agh.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static pl.edu.agh.simplechat.Constants.IP_PARAM;
import static pl.edu.agh.simplechat.Constants.MESSAGE_SEPARATOR;
import static pl.edu.agh.simplechat.Constants.MQTT_TOPIC;
import static pl.edu.agh.simplechat.Constants.MSG_PARAM;
import static pl.edu.agh.simplechat.Constants.NICK_PARAM;

public class SimpleChatActivity extends AppCompatActivity {

    private String nick;

    private String ip;

    private List<String> listItems = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    private ListView messagesView;

    private EditText messageText;

    private MqttClient sampleClient;

    private Handler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_chat);
        nick = getIntent().getStringExtra(NICK_PARAM);
        ip = getIntent().getStringExtra(IP_PARAM);
        adapter = new ArrayAdapter<>(this, R.layout.simple_list_item_1, listItems);
        messagesView = findViewById(R.id.messagesView);
        messagesView.setAdapter(adapter);
        messageText = findViewById(R.id.messageText);
        new Thread(new MqttClientConnection()).start();
        findViewById(R.id.sendButton).setOnClickListener(new MessageSendListener());
    }

    private class MessageSendListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String payload =  nick + MESSAGE_SEPARATOR + messageText.getText().toString();
            MqttMessage message = new MqttMessage(payload.getBytes());
            try {
                sampleClient.publish(MQTT_TOPIC, message);
                messageText.setText("");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sampleClient != null) {
            try {
                sampleClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private class MyMqttCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable throwable) {
            Toast.makeText(getApplicationContext(),
                    "MQTT server connection lost",
                    Toast.LENGTH_LONG).show();
            throwable.printStackTrace();
        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) {
            Message msg = myHandler.obtainMessage();
            Bundle b = new Bundle();
            String[] splitted = mqttMessage.toString().split("#", 2);
            String nick = splitted[0];
            String message = splitted[1];
            b.putString(NICK_PARAM, nick);
            b.putString(MSG_PARAM, message);
            msg.setData(b);
            myHandler.sendMessage(msg);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            System.out.println("Delivery complete");
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<SimpleChatActivity> sActivity;
        MyHandler(SimpleChatActivity activity){
            sActivity = new WeakReference<>(activity);
        }
        public void handleMessage(Message msg) {
            SimpleChatActivity activity = sActivity.get();
            activity.listItems.add("[" + msg.getData().getString(NICK_PARAM) + "] " +
                    msg.getData().getString(MSG_PARAM));
            activity.adapter.notifyDataSetChanged();
            activity.messagesView.setSelection(activity.listItems.size() - 1);
        }
    }

    private class MqttClientConnection implements Runnable {

        @Override
        public void run() {
            String clientId;
            MemoryPersistence persistence = new MemoryPersistence();
            try {
                String broker = "tcp://" + ip + ":1883";
                clientId = nick;
                sampleClient = new MqttClient(broker, clientId, persistence);
                sampleClient.setCallback(new MyMqttCallback());
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                System.out.println("Connecting to broker: " + broker);
                sampleClient.connect(connOpts);
                System.out.println("Connected");
                sampleClient.subscribe(MQTT_TOPIC + "/#");
            } catch (MqttException e) {
                Toast.makeText(getApplicationContext(),
                        "MQTT server connection error",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
