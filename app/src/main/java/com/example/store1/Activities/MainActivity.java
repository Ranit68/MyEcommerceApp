package com.example.store1.Activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.store1.Fragments.AccountFragment;
import com.example.store1.Fragments.CartFragment;
import com.example.store1.Fragments.HomeFragment;
import com.example.store1.Fragments.WishlistFragment;
import com.example.store1.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.navHome) fragment = new HomeFragment();
            else if (id == R.id.navCart) fragment = new CartFragment();
            else if (id == R.id.navWishlist) fragment = new WishlistFragment();
            else if (id == R.id.navAccount) fragment = new AccountFragment();
            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
