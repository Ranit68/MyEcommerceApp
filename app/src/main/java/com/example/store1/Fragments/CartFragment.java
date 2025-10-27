package com.example.store1.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.store1.Activities.CheckoutActivity;
import com.example.store1.Adapters.CartAdapter;
import com.example.store1.Models.Product;
import com.example.store1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartFragment extends Fragment {

    private RecyclerView rvCart;
    private CartAdapter adapter;
    private ArrayList<Product> cartList = new ArrayList<>();
    private DatabaseReference cartRef;

    private TextView tvTotalItems, tvTotalPrice;
    private Button btnProceed;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCart = view.findViewById(R.id.rvCart);
        rvCart.setLayoutManager(new LinearLayoutManager(getActivity()));

        tvTotalItems = view.findViewById(R.id.tvTotalItems);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnProceed = view.findViewById(R.id.btnProceed);

        ImageView back = view.findViewById(R.id.back4);
        adapter = new CartAdapter(getActivity(), cartList);
        rvCart.setAdapter(adapter);

        back.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        });


        loadCartFromFirebase();

        btnProceed.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(getActivity(), "Cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                // Go to CheckoutActivity
                Intent intent = new Intent(getActivity(), CheckoutActivity.class);
                intent.putExtra("cartItems", cartList); // pass cart items
                startActivity(intent);
            }
        });


        return view;
    }

    private void loadCartFromFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("cart");

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                double totalPrice = 0;
                int totalItems = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);
                    if (product != null) {
                        cartList.add(product);
                        totalItems += product.getQuantity();
                        totalPrice += product.getQuantity() * product.getPrice();
                    }
                }

                adapter.updateList(cartList);

                tvTotalItems.setText("Items: " + totalItems);
                tvTotalPrice.setText("Total: ₹" + String.format("%.2f", totalPrice));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load cart", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
