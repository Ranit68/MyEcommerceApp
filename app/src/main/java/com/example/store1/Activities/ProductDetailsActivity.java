package com.example.store1.Activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.store1.Fragments.ProductDetailsFragment;
import com.example.store1.R;

public class ProductDetailsActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        String id = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");
        double price = getIntent().getDoubleExtra("price",-1);
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String description = getIntent().getStringExtra("description");
        String category = getIntent().getStringExtra("category");

        ProductDetailsFragment fragment = ProductDetailsFragment.newInstance(
                id, title, price, imageUrl, description, category
        );

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}