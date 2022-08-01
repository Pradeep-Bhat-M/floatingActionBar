package com.example.android.globalactionbarservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;


public class GlobalActionBarService extends AccessibilityService {


    FrameLayout mLayout;
    RelativeLayout collapsedLayout;
    LinearLayout expandedLayout;
    RelativeLayout mainLayout;
    ImageView widgetCloseIcon;
    Button yesButton;
    Button noButton;
    Button closeButton;
    TextView description;
    TextView translatedTextView;


    boolean expanded = false;
    boolean yesClicked = false;

    @SuppressLint("SetTextI18n")
    public void updateServiceProvidingState(){
        if(yesClicked){
            yesButton.setVisibility(View.GONE);
            noButton.setText("Disable");
            description.setText(R.string.description2);
        }else{
            yesButton.setVisibility(View.VISIBLE);
            noButton.setText("No");
            description.setText(R.string.description1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onServiceConnected() {

        // Create an overlay and display the action bar
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.LEFT;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, lp);

        collapsedLayout = mLayout.findViewById(R.id.Layout_Collapsed);
        expandedLayout  = mLayout.findViewById(R.id.Layout_Expended);
        mainLayout = mLayout.findViewById(R.id.MainParentRelativeLayout);
        widgetCloseIcon = mLayout.findViewById(R.id.Widget_Close_Icon);
        yesButton = mLayout.findViewById(R.id.yesButton);
        noButton = mLayout.findViewById(R.id.noButton);
        description = mLayout.findViewById(R.id.description);
        translatedTextView = mLayout.findViewById(R.id.translatedTextView);
        closeButton = mLayout.findViewById(R.id.closeButton);

        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            int X_Axis, Y_Axis;
            float TouchX, TouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        X_Axis = lp.x;
                        Y_Axis = lp.y;
                        TouchX = event.getRawX();
                        TouchY = event.getRawY();
                        wm.updateViewLayout(mLayout, lp);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(!expanded){
                            expandedLayout.setVisibility(View.VISIBLE);
                            collapsedLayout.setVisibility(View.GONE);
                            expanded = true;
                        }else{
                            expandedLayout.setVisibility(View.GONE);
                            collapsedLayout.setVisibility(View.VISIBLE);
                            expanded = false;
                        }
                        wm.updateViewLayout(mLayout, lp);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        lp.x = X_Axis + (int) (event.getRawX() - TouchX);
                        lp.y = Y_Axis + (int) (event.getRawY() - TouchY);
                        wm.updateViewLayout(mLayout, lp);
                        return true;
                }
                return false;
            }
        });

        widgetCloseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandedLayout.setVisibility(View.GONE);
                collapsedLayout.setVisibility(View.GONE);
            }
        });

       closeButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               expandedLayout.setVisibility(View.GONE);
               collapsedLayout.setVisibility(View.VISIBLE);
               translatedTextView.setText("");
           }
       });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!yesClicked) {
                    expandedLayout.setVisibility(View.GONE);
                    collapsedLayout.setVisibility(View.VISIBLE);
                    expanded = false;
                    yesClicked = true;
                    updateServiceProvidingState();
                }
                System.out.println("Translation");
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandedLayout.setVisibility(View.GONE);
                collapsedLayout.setVisibility(View.VISIBLE);
                translatedTextView.setText("");
                expanded = false;
                yesClicked = false;
                updateServiceProvidingState();
            }
        });

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_HOVER_ENTER;
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = null;
        setServiceInfo(info);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo currentNode = getRootInActiveWindow();

//        if(!event.event.getClassName().toString().contains("ViewGroup")getClassName().toString().contains("RelativeLayout") || !event.getClassName().toString().contains("ImageView")) {
        if(yesClicked){
            if ( event.getClassName().toString().contains("ViewGroup") || event.getClassName().toString().contains("TextView")) {
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                    StringBuilder res = new StringBuilder();
                    for (CharSequence i : event.getText()) {
                        res.append(i);
                        res.append('\n');
                    }

//                    System.out.println(res.toString());
                    String rawText = res.toString();
                    if (rawText != null) {

                        translatedTextView.setText(res.toString());
                        expandedLayout.setVisibility(View.VISIBLE);
                        collapsedLayout.setVisibility(View.GONE);
                        expanded = true;
                        try {
                            String translated = translate(rawText.toString(), "en", "kn");
                            System.out.println(translated.toString());
                            translatedTextView.setText(translated.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
//                    System.out.println(event.getClassName().toString());
                    System.out.println(Locale.getDefault().getDisplayLanguage());
                }
            }
//            System.out.println("\n\n "+event.getClassName().toString()+"\n\n");
        }
    }



    public String translate(String text, String langTo, String langFrom) throws IOException {
        String urlStr = "https://script.google.com/macros/s/AKfycbxGhrb7hDSjY17ysrzt_dFK5ygd-GiHfQd4Xxd20bewVYoYMfWrmYx4N4841EmsKuTX/exec" +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }


    @Override
    public void onInterrupt() {

    }
}
//class RetrieveFeedTask extends AsyncTask<String, Void, RSSFeed> {
//
//    private Exception exception;
//
//    protected RSSFeed doInBackground(String... urls) {
//        try {
//            URL url = new URL(urls[0]);
//            SAXParserFactory factory = SAXParserFactory.newInstance();
//            SAXParser parser = factory.newSAXParser();
//            XMLReader xmlreader = parser.getXMLReader();
//            RssHandler theRSSHandler = new RssHandler();
//            xmlreader.setContentHandler(theRSSHandler);
//            InputSource is = new InputSource(url.openStream());
//            xmlreader.parse(is);
//
//            return theRSSHandler.getFeed();
//        } catch (Exception e) {
//            this.exception = e;
//
//            return null;
//        } finally {
//            is.close();
//        }
//    }
//
//    protected void onPostExecute(RSSFeed feed) {
//        // TODO: check this.exception
//        // TODO: do something with the feed
//    }
//}