package com.example.android.globalactionbarservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GlobalActionBarService extends AccessibilityService {


    FrameLayout mLayout;
    RelativeLayout collapsedLayout;
    LinearLayout expandedLayout;
    RelativeLayout mainLayout;
    ImageView widgetCloseIcon;
    Button yesButton;
    Button noButton;
    TextView description;

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


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo currentNode = getRootInActiveWindow();

        if(yesClicked){
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER ||
                event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED ) {
                System.out.println(event.getText());
            }
        }

        System.out.println(AccessibilityNodeInfo.ACTION_LONG_CLICK);

    }

    public String translate(String message){

        String translatedMessage = "";
        return translatedMessage;
    }


    @Override
    public void onInterrupt() {

    }
}
