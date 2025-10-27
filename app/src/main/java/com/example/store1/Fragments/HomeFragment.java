package com.example.store1.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.store1.Activities.SeaarchActivity;
import com.example.store1.Adapters.BannerAdapter;
import com.example.store1.Adapters.CategoryAdapter;
import com.example.store1.Adapters.ProductAdapter;
import com.example.store1.Models.Banner;
import com.example.store1.Models.Product;
import com.example.store1.R;
import com.example.store1.api.ApiClient;
import com.example.store1.api.ApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvProducts, rvCategories;
    private ProgressBar progressBar;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private ArrayList<Product> productList = new ArrayList<>();
    private ArrayList<Product> filteredList = new ArrayList<>();
    private ArrayList<String> categories = new ArrayList<>();
    private String selectedCategory = "";
    private ViewPager2 bannerViewPager;
    private BannerAdapter bannerAdapter;
    private ArrayList<Banner> bannerList = new ArrayList<>();
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private DatabaseReference userRef;
    private TextView tvGreeting;
    private FirebaseAuth auth;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvProducts = view.findViewById(R.id.rvProducts);
        rvCategories = view.findViewById(R.id.rvCategories);
        progressBar = view.findViewById(R.id.progressBar);
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        ImageView searchView = view.findViewById(R.id.searchView);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            loadUserName();
        }
        productAdapter = new ProductAdapter(getActivity(), filteredList);
        rvProducts.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        rvProducts.setAdapter(productAdapter);

        bannerList.add(new Banner("https://img.freepik.com/premium-psd/special-promo-t-shirt-banner-template_361928-1557.jpg"));
        bannerList.add(new Banner("https://static.vecteezy.com/system/resources/thumbnails/020/903/143/small/shoe-sale-banner-vector.jpg"));
        bannerList.add(new Banner("https://marketplace.canva.com/EAEo2Tqgahw/1/0/800w/canva-gold-minimalist-jewelry-new-arrival-landscape-banner-c7JOsif5u7Y.jpg"));

        bannerAdapter = new BannerAdapter(getActivity(), bannerList);
        bannerViewPager.setAdapter(bannerAdapter);

        int startPosition = Integer.MAX_VALUE / 2 -((Integer.MAX_VALUE /2) % bannerList.size());
        bannerViewPager.setCurrentItem(startPosition, false);

        // --- Category RecyclerView ---
        categories.add("All");
        categories.add("Men Shirt");
        categories.add("Saree");
        categories.add("Shoes");
        categories.add("Jewelry");
        categories.add("Women Dress");

        categoryAdapter = new CategoryAdapter(getActivity(), categories, category -> {
            selectedCategory = category.equals("All") ? "" : category;
            filterProducts("");
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        // --- Search ---
        searchView.setOnClickListener(v -> startActivity(new Intent(getActivity(), SeaarchActivity.class)));


        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int nextPos = bannerViewPager.getCurrentItem() + 1;
                bannerViewPager.setCurrentItem(nextPos, true);
                bannerHandler.postDelayed(this, 3000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);


// Optional: stop auto-scroll while user swipes manually
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bannerHandler.removeCallbacks(bannerRunnable);
                bannerHandler.postDelayed(bannerRunnable, 3000);
            }
        });




        loadProducts();
        return view;
    }

    private void loadUserName() {

        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.exists() ? snapshot.getValue(String.class) : "User";
                tvGreeting.setText(getGreetingMessage() + ", " + name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvGreeting.setText(getGreetingMessage() + ", User");
            }
        });
    }

    private String getGreetingMessage() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Product>> call = apiService.getAllProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call,
                                   @NonNull Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    filterProducts(""); // Initially show all
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        for (Product product : productList) {
            boolean matchesCategory = TextUtils.isEmpty(selectedCategory) || product.getCategory().equalsIgnoreCase(selectedCategory);
            boolean matchesQuery = TextUtils.isEmpty(query) || product.getTitle().toLowerCase().contains(query.toLowerCase());

            if (matchesCategory && matchesQuery) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);
    }
}
