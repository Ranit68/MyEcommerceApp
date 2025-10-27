package com.example.store1.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.store1.Fragments.ProductDetailsFragment;
import com.example.store1.Models.Product;
import com.example.store1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private ArrayList<Product> cartList;

    public CartAdapter(Context context, ArrayList<Product> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    public void updateList(ArrayList<Product> newList) {
        this.cartList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Product product = cartList.get(position);

        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText("₹" + String.format("%.2f", product.getPrice()));

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivProduct);

        // ---------- Quantity Spinner Fix ----------
        Integer[] quantities = {1, 2, 3, 4, 5};
        ArrayAdapter<Integer> qtyAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, quantities);
        qtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spQuantity.setAdapter(qtyAdapter);

        // Default quantity 1 if not set
        int qty = product.getQuantity() > 0 ? product.getQuantity() : 1;
        product.setQuantity(qty);

        // Remove previous listener to prevent unwanted trigger
        holder.spQuantity.setOnItemSelectedListener(null);

        // Set selection without triggering listener
        holder.spQuantity.setSelection(qty - 1, false);

        // Set listener after selection
        holder.spQuantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int newQty = i + 1;
                if (product.getQuantity() != newQty) {
                    product.setQuantity(newQty);
                    updateQuantityInFirebase(product);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        // ------------------------------------------

        // Remove button
        holder.btnRemove.setOnClickListener(v -> {
            removeFromFirebase(product);
            cartList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartList.size());
        });

        // Click to open Product Details
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

                ProductDetailsFragment fragment = ProductDetailsFragment.newInstance(
                        product.getId(),
                        product.getTitle(),
                        product.getPrice(),
                        product.getImageUrl(),
                        product.getDescription(),
                        product.getCategory()
                );

                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

    private void updateQuantityInFirebase(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("cart")
                .child(product.getId());

        cartRef.child("quantity").setValue(product.getQuantity());
    }

    private void removeFromFirebase(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("cart")
                .child(product.getId());

        cartRef.removeValue().addOnSuccessListener(aVoid ->
                Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvTitle, tvPrice;
        Spinner spQuantity;
        ImageView btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            spQuantity = itemView.findViewById(R.id.spQuantity);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
