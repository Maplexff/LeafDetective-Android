package com.example.demo;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.auto.ListData;
import com.bumptech.glide.Glide;
import com.example.demo.NetworkUtils.ImageUtils;
import com.example.demo.R;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;
    private OnItemClickListener onItemClickListener;
    private int selectedPosition = -1;
    public boolean isloadingflag = false;
    public HistoryAdapter(List<HistoryItem> historyList, OnItemClickListener onItemClickListener) {
        this.historyList = historyList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.timeTextView.setText("时间：" + item.getTime());
        holder.locationTextView.setText("地点：" +item.getLocation());
        holder.reportIdTextView.setText("上报ID：" +item.getReportId());
//        holder.imageView.setImageResource(item.getImageResId());  // 假设我们有一个图片资源ID
//        displayHistoryItem(item, holder.imageView);
        String base64 = item.getimagebase64(); // 确保是纯 base64 字符串（不含 data:image/... 开头）

        if (base64 != null && !base64.isEmpty()) {
            String imageDataUri = "data:image/jpeg;base64," + base64;

            Glide.with(holder.imageView.getContext())
                    .load(imageDataUri)
                    .into(holder.imageView);
        }else{
            holder.imageView.setImageResource(R.drawable.ic_icon_round);
        }

//        Glide.with(holder.imageView.getContext())
//                .load(item.getimagebase64())
//                .into(holder.imageView);
        holder.predictClassTextView.setText("病害：" +item.getPredictClass());
        holder.itemIdTextView.setText("序列：" + item.getItemId());
        holder.itemImageResIdTextView.setText("记录ID：" + item.getImageResId());
        holder.itemPredictScoreTextView.setText("Score：" + item.getPredictScore());
        // 设置点击事件
        // 根据选中项更新背景颜色
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY); // 选中的背景颜色
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); // 默认背景颜色
        }

        holder.itemView.setOnClickListener(v -> {
            if(isloadingflag){
                return;
            }
            int previousSelectedPosition = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();

//            // 如果点击的是相同项，则取消选中
//            if (previousSelectedPosition == selectedPosition) {
//                selectedPosition = -1;
//            }
            notifyItemChanged(previousSelectedPosition);  // 更新之前的项
            notifyItemChanged(selectedPosition);  // 更新当前项
            // 触发点击事件回调
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    public void displayHistoryItem(HistoryItem item, ImageView imageView) {
        String cacheKey = item.getTime();  // 使用itemId作为缓存键
        ImageUtils imageUtils = new ImageUtils();
        imageUtils.setImageFromBase64(item.getimagebase64(), cacheKey, imageView);
    }
    // 清空数据并刷新界面
    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        historyList.clear();
        notifyDataSetChanged();  // 通知RecyclerView数据已更改并刷新界面
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }


    public void setSelectedPosition(int position) {
        int previousSelectedPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousSelectedPosition);
        notifyItemChanged(selectedPosition);
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView timeTextView, locationTextView, reportIdTextView,predictClassTextView,itemIdTextView,itemImageResIdTextView,itemPredictScoreTextView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            itemIdTextView = itemView.findViewById(R.id.itemId);
            itemImageResIdTextView = itemView.findViewById(R.id.itemImageResId);
            imageView = itemView.findViewById(R.id.itemImage);
            timeTextView = itemView.findViewById(R.id.itemTime);
            locationTextView = itemView.findViewById(R.id.itemLocation);
            reportIdTextView = itemView.findViewById(R.id.itemReportId);
            predictClassTextView = itemView.findViewById(R.id.itemPredictClass);
            itemPredictScoreTextView = itemView.findViewById(R.id.itemPredictScore);
        }
    }

    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }
}

