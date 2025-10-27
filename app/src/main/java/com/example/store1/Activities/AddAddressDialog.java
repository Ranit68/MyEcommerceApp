package com.example.store1.Activities;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.store1.Models.Address;
import com.example.store1.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class AddAddressDialog extends Dialog {

    private EditText etName, etPhone, etEmail, etLine1, etLine2, etPincode, etCity, etState;
    private Button btnSave;
    private AddressSavedListener listener;
    private Context context;

    public interface AddressSavedListener {
        void onAddressSaved(Address address);
    }

    public AddAddressDialog(@NonNull Context context, AddressSavedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_address);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etLine1 = findViewById(R.id.etLine1);
        etLine2 = findViewById(R.id.etLine2);
        etPincode = findViewById(R.id.etPincode);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        btnSave = findViewById(R.id.btnSave);

        etCity.setEnabled(false);
        etState.setEnabled(false);

        etPincode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 6) {
                    fetchCityState(editable.toString());
                }
            }
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String line1 = etLine1.getText().toString().trim();
            String line2 = etLine2.getText().toString().trim();
            String pincode = etPincode.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String state = etState.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || line1.isEmpty() || pincode.isEmpty() || city.isEmpty() || state.isEmpty()) {
                Toast.makeText(context, "Please fill all mandatory fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = new Address(name, phone, email, line1, line2, city, state, pincode);
            listener.onAddressSaved(address);
            dismiss();
        });
    }

    private void fetchCityState(String pincode) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.postalpincode.in/pincode/" + URLEncoder.encode(pincode, "UTF-8"));
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream in = conn.getInputStream();
                Scanner scanner = new Scanner(in);
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNext()) sb.append(scanner.nextLine());
                scanner.close();

                JSONArray array = new JSONArray(sb.toString());
                JSONObject obj = array.getJSONObject(0);

                if (!obj.isNull("PostOffice")) {
                    JSONArray postOffices = obj.getJSONArray("PostOffice");
                    if (postOffices.length() > 0) {
                        JSONObject po = postOffices.getJSONObject(0);
                        String city = po.getString("District");
                        String state = po.getString("State");

                        // Run safely on UI thread using dialog’s own context
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            etCity.setText(city);
                            etState.setText(state);
                        });
                    } else {
                        ((android.app.Activity) context).runOnUiThread(() ->
                                Toast.makeText(context, "No data found for this PIN", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Invalid Pincode", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
