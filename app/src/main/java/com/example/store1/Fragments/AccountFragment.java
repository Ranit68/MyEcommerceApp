package com.example.store1.Fragments;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.store1.Activities.AddAddressDialog;
import com.example.store1.Activities.LoginActivity;
import com.example.store1.Activities.OrderHistoryActivity;
import com.example.store1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    private TextView tvName;
    private Button btnLogout, btnOrderHistory, btnAddAddress;
    private DatabaseReference addressRef, userRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tvName = view.findViewById(R.id.tvName);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnOrderHistory = view.findViewById(R.id.btnOrderHistory);
        btnAddAddress = view.findViewById(R.id.btnAddAddress);
        ImageView back = view.findViewById(R.id.back3);


        back.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HomeFragment())
                    .commit();
        });




        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        addressRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("addresses");

        loadUserName();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        btnOrderHistory.setOnClickListener(v ->

                startActivity(new Intent(getActivity(), OrderHistoryActivity.class))
                );

        btnAddAddress.setOnClickListener(v ->
                openAddAddressForm()
        );

        return view;
    }
    private void loadUserName() {

        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        tvName.setText("Hi, " + name);
                    } else {
                        tvName.setText("Hi, User");
                    }
                } else {
                    tvName.setText("Hi, User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvName.setText("Hi, User");
            }
        });
    }

    private void openAddAddressForm() {
        AddAddressDialog dialog = new AddAddressDialog(getActivity(), address -> {
            String key = addressRef.push().getKey();
            if (key != null) {
                addressRef.child(key).setValue(address);
                Toast.makeText(getContext(), "Address saved", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}
