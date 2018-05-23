package mkitbs.ekitchen.app.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import mkitbs.ekitchen.app.R;

/**
 * Created by verakovic on 09.12.2016.
 */

public class StatusButton  extends ImageButton {

    public enum StatusEnum {
        NEW, TAKEN, DELIVERED, CLEAR, CLOSED
    }

    public interface StatusListener {
        void onNew();
        void onTaken();
        void onDelivered();
        void onClear();
        void onClosed();
    }

    private StatusEnum mState;
    private StatusListener mStatusListener;

    public StatusButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int next = ((mState.ordinal() + 1) % StatusEnum.values().length);
                setState(StatusEnum.values()[next]);
                performStatusClick();
            }
        });
        //Sets initial state
        setState(StatusEnum.NEW);
    }

    private void performStatusClick() {
        if(mStatusListener == null)return;
        switch (mState) {
            case NEW:
                mStatusListener.onNew();
                break;
            case TAKEN:
                mStatusListener.onTaken();
                break;
            case DELIVERED:
                mStatusListener.onDelivered();
                break;
            case CLEAR:
                mStatusListener.onClear();
                break;
            case CLOSED:
                mStatusListener.onClosed();
                break;
        }
    }

    private void createDrawableState() {
        switch (mState) {
            case NEW:
                setImageResource(R.drawable.a2);    //new icon red
                break;
            case TAKEN:
                setImageResource(R.drawable.b2);    //taken icon blue
                break;
            case DELIVERED:
                setImageResource(R.drawable.c2);    //delivered icon green
                break;
            case CLEAR:
                setImageResource(R.drawable.d2);    //clear icon orange
                break;
            case CLOSED:
                setImageResource(R.drawable.e2);    //closed icon black
                break;
        }
    }


    public StatusEnum getState() {
        return mState;
    }

    public void setState(StatusEnum state) {
        if(state == null)return;
            this.mState = state;
        createDrawableState();
    }

    public StatusListener getStatusListener() {
        return mStatusListener;
    }

    public void setStatusListener(StatusListener statusListener) {
        this.mStatusListener = statusListener;
    }

}
