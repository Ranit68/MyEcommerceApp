package com.example.store1.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.store1.Adapters.ProductAdapter;
import com.example.store1.Models.Product;
import com.example.store1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WishlistFragment extends Fragment {

    private RecyclerView rvWishlist;
    private ProductAdapter adapter;
    private ArrayList<Product> wishlistProducts = new ArrayList<>();
    private DatabaseReference wishlistRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        rvWishlist = view.findViewById(R.id.rvWishlist);
        rvWishlist.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ImageView back = view.findViewById(R.id.back6);

        back.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        });
        adapter = new ProductAdapter(getContext(), wishlistProducts);
        rvWishlist.setAdapter(adapter);

        loadWishlist();

        return view;
    }

    private void loadWishlist() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        wishlistRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("wishlists");

        wishlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistProducts.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    if(product != null){
                        wishlistProducts.add(product);
                    }
                }
                adapter.updateList(wishlistProducts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load wishlist", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
