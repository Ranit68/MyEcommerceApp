package com.example.store1.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.store1.Models.OrderItem;
import com.example.store1.R;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private ArrayList<OrderItem> orderItems;

    public OrderAdapter(Context context, ArrayList<OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.title.setText(item.getTitle());
        holder.desc.setText(item.getDescription());
        holder.price.setText("₹" + item.getPrice());
        Glide.with(context).load(item.getImageUrl()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc, price;
        ImageView image;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvOrderTitle);
            desc = itemView.findViewById(R.id.tvOrderDesc);
            price = itemView.findViewById(R.id.tvOrderPrice);
            image = itemView.findViewById(R.id.ivOrderImage);
        }
    }
}
