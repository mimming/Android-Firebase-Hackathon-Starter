package com.mimming.hacks.starter;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final String CHAT_PATH = "/chat/";
    private Button mSendButton;
    private EditText mChatMessage;
    private ListView mChatBuffer;

    private List<String> mChatBufferList;

    ArrayAdapter mChatBufferAdapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFirebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // a JavaScript hack in Java?  feels so dirty
        final Activity self = this;

        mChatBuffer = (ListView)findViewById(R.id.chat_buffer);
        mChatBufferList = new ArrayList<String>();
        mChatBufferAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mChatBufferList);
        ListView listView = (ListView) findViewById(R.id.chat_buffer);
        listView.setAdapter(mChatBufferAdapter);

        mChatMessage = (EditText)findViewById(R.id.message);

        mSendButton = (Button)findViewById(R.id.send_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatMessage = mChatMessage.getText().toString();
                mChatMessage.setText("");
                mFirebaseRef.push().setValue(chatMessage);
            }
        });

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseRef = mFirebaseDatabase.getReference(CHAT_PATH);

        mFirebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mChatBufferList.add(dataSnapshot.getValue(String.class));
                mChatBufferAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // not implemented, but could find by key and replace
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // not implemented, but could find by key and remove
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // not implemented, but could find by key and move
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // not implemented
            }
        });

    }
}
