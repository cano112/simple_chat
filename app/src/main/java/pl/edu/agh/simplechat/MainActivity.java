package pl.edu.agh.simplechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import static pl.edu.agh.simplechat.Constants.IP_PARAM;
import static pl.edu.agh.simplechat.Constants.NICK_PARAM;

public class MainActivity extends AppCompatActivity {

    private EditText ipText;

    private EditText nickText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipText = findViewById(R.id.ipInput);
        nickText = findViewById(R.id.nickInput);
        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SimpleChatActivity.class);
                intent.putExtra(IP_PARAM, ipText.getText().toString());
                intent.putExtra(NICK_PARAM, nickText.getText().toString());
                startActivity(intent);
            }
        });
    }
}
