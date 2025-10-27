package com.example.store1.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.store1.Activities.ProductDetailsActivity;
import com.example.store1.Fragments.ProductDetailsFragment;
import com.example.store1.Models.Product;
import com.example.store1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private ArrayList<Product> productList;
    private ArrayList<String> wishlistIds = new ArrayList<>();
    private DatabaseReference wishlistRef;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public ProductAdapter(Context context, ArrayList<Product> productList){
        this.context = context;
        this.productList = productList;

        if(auth.getCurrentUser() != null){
            String uid = auth.getCurrentUser().getUid();
            wishlistRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child(
                    "wishlists");
            loadWishlist();
        }
    }

    private void loadWishlist() {
        wishlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistIds.clear();
                for (DataSnapshot item : snapshot.getChildren()){
                    wishlistIds.add(item.getKey());
                }
                notifyDataSetChanged(); // Update the icons
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position){
        Product product = productList.get(position);
        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText("₹" + product.getPrice());

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivProduct);

      holder.ivWishlist.setImageResource(wishlistIds.contains(product.getId()) ?
              R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        holder.ivWishlist.setOnClickListener(v -> {
            if(auth.getCurrentUser() == null){
                Toast.makeText(context, "Please login to use wishlist", Toast.LENGTH_SHORT).show();
                return;
            }

            if(wishlistIds.contains(product.getId())){
                // Remove from wishlist
                wishlistRef.child(product.getId()).removeValue();
                Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
            } else {
                // Add to wishlist under user
                wishlistRef.child(product.getId()).setValue(product);
                Toast.makeText(context, "Added to Wishlist", Toast.LENGTH_SHORT).show();
            }
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("id", product.getId());
            intent.putExtra("title", product.getTitle());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("description", product.getDescription());
            intent.putExtra("category", product.getCategory());
            context.startActivity(intent);
        });



//        holder.itemView.setOnClickListener(v -> {
//            if (context instanceof FragmentActivity) {
//                FragmentActivity activity = (FragmentActivity) context;
//                FragmentManager fm = activity.getSupportFragmentManager();
//
//                int containerId = activity.findViewById(R.id.fragmentContainer) != null
//                        ? R.id.fragmentContainer
//                        : android.R.id.content;
//
//                ProductDetailsFragment fragment = ProductDetailsFragment.newInstance(
//                        product.getId(),
//                        product.getTitle(),
//                        product.getPrice(),
//                        product.getImageUrl(),
//                        product.getDescription(),
//                        product.getCategory()
//                );
//
//                fm.beginTransaction()
//                        .replace(containerId, fragment)
//                        .addToBackStack(null)
//                        .commit();
//            }
//        });
    }

    private void updateWishlistIcon(ProductViewHolder holder, Product product){
        boolean isWishlisted = wishlistIds.contains(product.getId());
        holder.ivWishlist.setImageResource(isWishlisted ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(ArrayList<Product> newList){
        this.productList = newList;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder{
        ImageView ivProduct, ivWishlist;
        TextView tvTitle, tvPrice;

        public ProductViewHolder(@NonNull View itemView){
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivWishlist = itemView.findViewById(R.id.ivWishlist);
        }
    }
}
