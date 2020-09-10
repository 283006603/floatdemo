package com.example.zxk.mywindowdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class FloatContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    List<String> mlist;
    Context context;

    public FloatContentAdapter(List<String> list, Context context){
        this.mlist = list;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_float_content, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        Log.d("FloatContentAdapter", "aaa");
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        myViewHolder.tv_content.setText(mlist.get(position) + "");
    }

    @Override
    public int getItemCount(){
        return mlist.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_content;

        public MyViewHolder(View itemView){
            super(itemView);
            tv_content = itemView.findViewById(R.id.tv_content);
        }
    }
}
