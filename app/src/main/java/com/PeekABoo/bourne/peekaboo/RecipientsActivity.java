package com.PeekABoo.bourne.peekaboo;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RecipientsActivity extends ListActivity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();

    protected ParseUser mCurrentUser;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected List<ParseUser> mFriends;
    protected MenuItem mSendMenuItem;
    protected Uri mMediaUri;
    protected String mFileType;
    protected String mTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_recipients);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

        if(mFileType.equals(ParseConstants.TYPE_TEXT)){
            mTextMessage = getIntent().getStringExtra(ParseConstants.TYPE_TEXT);
        }
        else {
            mMediaUri = getIntent().getData();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if( e == null ) {
                    mFriends = friends;
                    String[] usernames = new String[mFriends.size()];
                    int i = 0;

                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<String>(
                                    getListView().getContext(),
                                    android.R.layout.simple_list_item_checked,
                                    usernames);
                    setListAdapter(adapter);
                }
                else {
                    errorDialogBox(e);
                }
            }
        });

    }

    //Shows an Error DialogBox
    protected void errorDialogBox(ParseException e) {
        Log.e(TAG, e.getMessage());
        AlertDialog.Builder builder =
                new AlertDialog.Builder(RecipientsActivity.this);
        builder.setMessage(e.getMessage())
                .setTitle(R.string.error_title)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipients, menu);
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_send:
                ParseObject message = createMessage();
                if(message == null) {
                    //Error Selecting the message
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.error_selecting_file)
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    send(message);
                    finish();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(l.getCheckedItemCount() > 0) {
            mSendMenuItem.setVisible(true);
        }
        else{
            mSendMenuItem.setVisible(false);
        }
    }

    // The Message objects
    protected ParseObject createMessage(){
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGE);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENTS_ID, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        // User sends a SMS
        if(mFileType.equals(ParseConstants.TYPE_TEXT)){
            message.put(ParseConstants.TYPE_TEXT, mTextMessage);
            return message;
        }
        else {
            byte[] filebytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
            if (filebytes == null) {
                return null;
            } else {
                if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                    filebytes = FileHelper.reduceImageForUpload(filebytes);
                }

                String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
                ParseFile file = new ParseFile(fileName, filebytes);
                message.put(ParseConstants.KEY_FILE, file);
                return message;
            }
        }

    }

    // Get all the recipients IDS
    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientsIds = new ArrayList<String>();
        for(int i = 0 ; i < getListView().getCount(); i++){
            if(getListView().isItemChecked(i)){
                recipientsIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientsIds;
    }

    protected void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    // Message has been sent
                    Toast.makeText(RecipientsActivity.this,
                            R.string.success_message,
                            Toast.LENGTH_LONG).show();
                    sendPushNotification();
                }
                else {
                    errorDialogBox(e);
                }
            }
        });
    }

    protected void sendPushNotification() {
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());

        //send push notifications
        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(getString(R.string.push_message,
                ParseUser.getCurrentUser().getUsername()));
        push.sendInBackground();
    }

}
