package com.PeekABoo.bourne.peekaboo;

/**
 * Created by bournelipardo on 8/14/14.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends ListFragment {

    protected List<ParseObject> mMessages;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListner);
        mSwipeRefreshLayout.setColorScheme(
                R.color.swipeRefresh1,
                R.color.swipeRefresh2,
                R.color.swipeRefresh3,
                R.color.swipeRefresh4
        );

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setProgressBarIndeterminateVisibility(true);

        retrieveMessages();
    }

    protected void retrieveMessages() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGE);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENTS_ID, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);

                if(mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (e == null) {
                    // New messages
                    mMessages = messages;

                    String[] usernames = new String[mMessages.size()];
                    int i = 0;

                    for (ParseObject message : mMessages) {
                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }

                    if (getListView().getAdapter() == null) {
                        MessageAdapter adapter = new MessageAdapter(
                                getListView().getContext(),
                                mMessages);
                        setListAdapter(adapter);
                    } else {
                        //refill the adapter
                        ((MessageAdapter) getListView().getAdapter()).refill(mMessages);
                    }

                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);

        //Check if Text or Image/Video
        if(messageType.equals(ParseConstants.TYPE_TEXT)){
            //SMS
            String textMessage = message.getString(ParseConstants.TYPE_TEXT);
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.putExtra(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_TEXT);
            intent.putExtra(ParseConstants.TYPE_TEXT, textMessage);
            startActivity(intent);
        }
        else {
            ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
            Uri fileUri = Uri.parse(file.getUrl());

            //Check if Image or Video
            if (messageType.equals(ParseConstants.TYPE_IMAGE)) {
                //View Image
                Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                intent.setData(fileUri);
                intent.putExtra(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_IMAGE);
                startActivity(intent);
            } else {
                //View Video
                Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
                intent.setDataAndType(fileUri, "video/*");
                startActivity(intent);
            }
        }

        List<String> ids = message.getList(ParseConstants.KEY_RECIPIENTS_ID);
        // Delete the message
        if (ids.size() == 1) {
            // last recipient - delete the whole thing!
            message.deleteInBackground();
        } else {
            // remove the recipient and save
            ids.remove(ParseUser.getCurrentUser().getObjectId());

            ArrayList<String> idsToRemove = new ArrayList<String>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            message.removeAll(ParseConstants.KEY_RECIPIENTS_ID, idsToRemove);
            message.saveInBackground();
        }
    }

    protected OnRefreshListener mOnRefreshListner = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveMessages();
        }
    };
}
