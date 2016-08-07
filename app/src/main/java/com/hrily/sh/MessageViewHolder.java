package com.hrily.sh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by hrishi on 9/6/16.
 */
public class MessageViewHolder extends RecyclerView.ViewHolder {

    View mView;
    Context mContext;
    TextView msg_txt, time_txt;
    RelativeLayout mlayout;

    public MessageViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        msg_txt = (TextView) mView.findViewById(R.id.msg_txt);
        time_txt = (TextView) mView.findViewById(R.id.time_txt);
        mlayout = (RelativeLayout) mView.findViewById(R.id.msg_lay);
    }

}
