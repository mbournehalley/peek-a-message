package com.PeekABoo.bourne.peekaboo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SendMessage extends Activity implements OnClickListener {

    protected EditText mText;
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkForEmptyValue();
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    //Check if Editfield is empty
    private void checkForEmptyValue(){

        Button button = (Button) findViewById(R.id.send_message_button);
        button.setOnClickListener(this);

        String text = mText.getText().toString();

        if(text.equals("")){
            button.setEnabled(false);
        }
        else{
            button.setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        mText = (EditText) findViewById(R.id.editText2);

        mText.addTextChangedListener(textWatcher);

        checkForEmptyValue();

    }

    @Override
    public void onClick(View v) {
        String fileType = ParseConstants.TYPE_TEXT;
        String textMessage = mText.getText().toString();
        Intent intent = new Intent(this, RecipientsActivity.class);
        intent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType );
        intent.putExtra(ParseConstants.TYPE_TEXT, textMessage);
        startActivity(intent);
    }
}
