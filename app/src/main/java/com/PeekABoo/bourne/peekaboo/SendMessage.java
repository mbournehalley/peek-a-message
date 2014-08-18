package com.PeekABoo.bourne.peekaboo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;

public class SendMessage extends Activity implements OnClickListener {

    protected EditText mText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        mText = (EditText) findViewById(R.id.editText2);
        Button button = (Button) findViewById(R.id.send_message_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String textMessage = mText.getText().toString();
        Intent intent = new Intent(this, RecipientsActivity.class);
        intent.putExtra(ParseConstants.TYPE_TEXT, textMessage);
        startActivity(intent);
    }
}
