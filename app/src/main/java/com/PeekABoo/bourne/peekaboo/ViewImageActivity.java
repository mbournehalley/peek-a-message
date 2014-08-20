package com.PeekABoo.bourne.peekaboo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

public class ViewImageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        TextView textView = (TextView)findViewById(R.id.textMessages);
        String fileType = getIntent().getStringExtra(ParseConstants.KEY_FILE_TYPE);

        if(fileType.equals(ParseConstants.TYPE_TEXT)) {
            String textMessage = getIntent().getStringExtra(ParseConstants.TYPE_TEXT);
            textView.setText(textMessage);
        }
        else {
            Uri imageUri = getIntent().getData();
            Picasso.with(this).load(imageUri.toString()).into(imageView);
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 10*1000);
    }

}
