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

import com.bumptech.glide.Glide;
import com.example.store1.Activities.CheckoutActivity;
import com.example.store1.Models.Product;
import com.example.store1.Models.User;
import com.example.store1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProductDetailsFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_PRICE = "price";
    private static final String ARG_IMAGE_URL = "imageUrl";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_ID = "id";

    private String productId, title, imageUrl, description, category;
    private double price;

    private ImageView ivProduct;
    private TextView tvTitle, tvPrice, tvDescription;
    private Button btnAddToCart, btnBuy;

    // Updated newInstance to accept all fields
    public static ProductDetailsFragment newInstance(String id, String title, double price,
                                                     String imageUrl, String description,String category) {
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_TITLE, title);
        args.putDouble(ARG_PRICE, price);
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            productId = getArguments().getString(ARG_ID);
            title = getArguments().getString(ARG_TITLE);
            price = getArguments().getDouble(ARG_PRICE);
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
            description = getArguments().getString(ARG_DESCRIPTION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);

        ivProduct = view.findViewById(R.id.ivProduct);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvDescription = view.findViewById(R.id.tvDescription);
        btnAddToCart = view.findViewById(R.id.btnAddToCart);
        btnBuy = view.findViewById(R.id.btnBuy);
        ImageView back = view.findViewById(R.id.back5);



        back.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        });

        // Set product details directly
        tvTitle.setText(title);
        tvPrice.setText("₹" + price);
        tvDescription.setText(description);
        Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivProduct);

        btnAddToCart.setOnClickListener(v -> addToCart());

        btnBuy.setOnClickListener(v -> {
            // Redirect to CheckoutActivity
            Intent intent = new Intent(getActivity(), CheckoutActivity.class);
            intent.putExtra("id", productId);
            intent.putExtra("title", title);
            intent.putExtra("price", price);
            intent.putExtra("imageUrl", imageUrl);
            intent.putExtra("description", description);
            intent.putExtra("category", category);
            startActivity(intent);
        });


        return view;
    }
    private void checkIfInCart() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("cart")
                .child(productId);

        cartRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Product already in cart
                btnAddToCart.setText("Go to Cart");
                btnAddToCart.setOnClickListener(v -> {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new CartFragment())
                            .addToBackStack(null)
                            .commit();
                });
            } else {
                // Not in cart
                btnAddToCart.setText("Add to Cart");
                btnAddToCart.setOnClickListener(v -> addToCart());
            }
        });
    }

    private void addToCart() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if(!task.getResult().exists()) {
                    // User does not exist, create dummy entry
                    userRef.setValue(new User("User", "dummy@example.com"));
                }

                // Now add product to cart
                DatabaseReference cartRef = userRef.child("cart").child(productId);
                Product product = new Product(productId, title, category, imageUrl, description,
                        price,
                        1, 5.0);
                cartRef.setValue(product).addOnCompleteListener(cartTask -> {
                    if(cartTask.isSuccessful()) {
                        Toast.makeText(getActivity(), "Added to cart", Toast.LENGTH_SHORT).show();
                        btnAddToCart.setText("Go to Cart");
                        btnAddToCart.setOnClickListener(v -> {
                            getParentFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragmentContainer, new CartFragment())
                                    .addToBackStack(null)
                                    .commit();
                        });
                    } else {
                        Toast.makeText(getActivity(), "Failed to add cart", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void addToWishlist(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference wishRef = FirebaseDatabase.getInstance()
                .getReference("wishlists")
                .child(uid)
                .child(productId);

        Product product = new Product(productId, title, imageUrl, description, "category", price, 1, 5.0);
        wishRef.setValue(product);
        Toast.makeText(getActivity(), "Added to wishlist", Toast.LENGTH_SHORT).show();
    }
}
