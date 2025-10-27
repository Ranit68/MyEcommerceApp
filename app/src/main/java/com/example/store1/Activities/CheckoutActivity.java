package com.example.store1.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.store1.Adapters.AddressAdapter;
import com.example.store1.Adapters.CheckoutCartAdapter;
import com.example.store1.Models.Address;
import com.example.store1.Models.Product;
import com.example.store1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView rvCheckoutItems, rvAddresses;
    private CheckoutCartAdapter cartAdapter;
    private AddressAdapter addressAdapter;
    private ArrayList<Product> cartList = new ArrayList<>();
    private ArrayList<Address> addressList = new ArrayList<>();
    private DatabaseReference cartRef, addressRef, ordersRef;
    private TextView tvTotalBill;
    private Button btnPayNow, btnAddAddress;
    private CheckBox cbCashOnDelivery;
    private Address selectedAddress;
    private double totalAmount = 0.0;
    private boolean isBuyNow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        rvCheckoutItems = findViewById(R.id.rvCheckoutItems);
        rvAddresses = findViewById(R.id.rvAddresses);
        tvTotalBill = findViewById(R.id.tvTotalBill);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        cbCashOnDelivery = findViewById(R.id.cbCashOnDelivery);
        ImageView back1 = findViewById(R.id.back1);

        back1.setOnClickListener(v -> {
            onBackPressed();
        });

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("cart");
        addressRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("addresses");
        ordersRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("orders");

        // Cart RecyclerView
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CheckoutCartAdapter(this, cartList);
        rvCheckoutItems.setAdapter(cartAdapter);

        // Address RecyclerView
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(this, addressList, address -> selectedAddress = address);
        rvAddresses.setAdapter(addressAdapter);

        Intent intent = getIntent();
        if (intent != null & intent.hasExtra("id")){
            Product product = new Product(
                    intent.getStringExtra("id"),
                    intent.getStringExtra("title"),
                    intent.getStringExtra("category") != null ? intent.getStringExtra("category") : "General",
                    intent.getStringExtra("imageUrl"),
                    intent.getStringExtra("description"),
                    intent.getDoubleExtra("price", 0),
                    1,
                    5.0
            );
            cartList.clear();
            cartList.add(product);
            totalAmount = product.getPrice() * product.getQuantity();
            cartAdapter.updateList(cartList);
            tvTotalBill.setText("Total: ₹" + String.format("%.2f", totalAmount));
            isBuyNow = true;
        }else {
            loadCart();
        }
        loadAddresses();

        btnAddAddress.setOnClickListener(v -> openAddAddressForm());

        btnPayNow.setOnClickListener(v -> {
            if (selectedAddress == null) {
                Toast.makeText(this, "Select an address first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cbCashOnDelivery.isChecked()) {
                placeOrder("Cash on Delivery");
            } else {
                startPayment();
            }
        });
    }

    private void loadCart() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);
                    if (product != null) {
                        cartList.add(product);
                        totalAmount += product.getPrice() * product.getQuantity();
                    }
                }
                cartAdapter.updateList(cartList);
                tvTotalBill.setText("Total: ₹" + String.format("%.2f", totalAmount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CheckoutActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAddresses() {
        addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Address address = ds.getValue(Address.class);
                    if (address != null) addressList.add(address);
                }
                addressAdapter.updateList(addressList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void openAddAddressForm() {
        AddAddressDialog dialog = new AddAddressDialog(this, address -> {
            String key = addressRef.push().getKey();
            if (key != null) {
                addressRef.child(key).setValue(address);
                Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void startPayment() {
        String total = String.format("%.2f", totalAmount);
        Uri uri =
                Uri.parse("upi://pay?pa=sayon29@ptyes&pn=Store1&mc=0000&tr=1234567890&tn" +
                        "=OrderPayment&am=" + total + "&cu=INR");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void placeOrder(String paymentMode) {
        String orderId = ordersRef.push().getKey();
        if (orderId == null) return;

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("address", selectedAddress);
        orderData.put("items", cartList);
        orderData.put("totalAmount", totalAmount);
        orderData.put("paymentMode", paymentMode);
        orderData.put("status", "Pending");

        ordersRef.child(orderId).setValue(orderData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                cartRef.removeValue(); // Clear cart after order
                Toast.makeText(this, "Order placed successfully (" + paymentMode + ")", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
