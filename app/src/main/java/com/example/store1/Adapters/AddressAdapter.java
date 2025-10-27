package com.example.store1.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.store1.Models.Address;
import com.example.store1.R;

import java.util.ArrayList;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<Address> addressList;
    private AddressSelectedListener listener;
    private int selectedPosition = -1; // Keeps track of selected item

    public interface AddressSelectedListener {
        void onAddressSelected(Address address);
    }

    public AddressAdapter(Context context, ArrayList<Address> addressList, AddressSelectedListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
    }

    public void updateList(ArrayList<Address> newList) {
        this.addressList = newList;
        // Reset selection if the list changes
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.tvName.setText(address.getName());
        holder.tvDetails.setText(address.getAddressLine1() + ", " + address.getAddressLine2() + ", " +
                address.getCity() + ", " + address.getState() + " - " + address.getPincode() +
                "\nPhone: " + address.getPhone() + "\nEmail: " + address.getEmail());

        // Set the radio button state based on selection
        holder.radioButton.setChecked(position == selectedPosition);

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            // Update selected position
            if (selectedPosition != position) {
                selectedPosition = position;
                listener.onAddressSelected(address); // Pass selected address to activity
                notifyDataSetChanged(); // Refresh RecyclerView to update radio buttons
            }
        });

        // Also handle radio button click
        holder.radioButton.setOnClickListener(v -> holder.itemView.performClick());
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        RadioButton radioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
    }
}
