package com.example.mobilepalengke.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mobilepalengke.Adapters.AdminProductAdapter;
import com.example.mobilepalengke.Adapters.IconOptionAdapter;
import com.example.mobilepalengke.DataClasses.IconOption;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.DialogClasses.ProductDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminProductActivity extends AppCompatActivity {

    ConstraintLayout productsLayout, productCategoriesLayout;
    EditText etSearchProduct;
    TextView tvSelectedCategory, btnChangeCategory, tvProductCaption;
    Button btnAddProduct, btnBack;
    RecyclerView recyclerView, recyclerView2;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ProductDialog productDialog;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    int currentStep = 0, maxStep = 1;

    String searchValue;

    Query productsQuery, productCategoriesQuery, cartProductsQuery;

    List<Product> products = new ArrayList<>(), productsCopy = new ArrayList<>();
    List<String> productsCategories = new ArrayList<>(), productsCategoriesCopy = new ArrayList<>();

    AdminProductAdapter adminProductAdapter;

    IconOptionAdapter iconOptionAdapter;

    List<IconOption> productCategories = new ArrayList<>();
    List<String> productCategoriesId = new ArrayList<>();

    int selectedCategoryIndex = 0;
    String selectedCategoryId;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product);

        productsLayout = findViewById(R.id.productsLayout);
        productCategoriesLayout = findViewById(R.id.productCategoriesLayout);

        etSearchProduct = findViewById(R.id.etSearchProduct);
        tvSelectedCategory = findViewById(R.id.tvSelectedCategory);
        btnChangeCategory = findViewById(R.id.btnChangeCategory);
        recyclerView = findViewById(R.id.recyclerView);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        tvProductCaption = findViewById(R.id.tvProductCaption);

        recyclerView2 = findViewById(R.id.recyclerView2);
        btnBack = findViewById(R.id.btnBack);

        context = AdminProductActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        productDialog = new ProductDialog(context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        productsQuery = firebaseDatabase.getReference("products").orderByChild("id");
        productCategoriesQuery = firebaseDatabase.getReference("productCategories").orderByChild("name");
        cartProductsQuery = firebaseDatabase.getReference("cartList").child(uid);

        loadingDialog.showDialog();
        isListening = true;
        productsQuery.addValueEventListener(getProdValueListener());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        adminProductAdapter = new AdminProductAdapter(context, products);
        adminProductAdapter.setProductAdapterListener(product -> {
            productDialog.showDialog();
            productDialog.setData(product);
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adminProductAdapter);

        iconOptionAdapter = new IconOptionAdapter(context, productCategories);
        iconOptionAdapter.setIconOptionAdapterListener(new IconOptionAdapter.IconOptionAdapterListener() {
            @Override
            public void onClick(IconOption iconOption) {

            }

            @Override
            public void onClick(IconOption iconOption, int position) {
                currentStep--;

                tvSelectedCategory.setText(iconOption.getLabelName());

                selectedCategoryIndex = position;
                selectedCategoryId = productCategoriesId.get(position);

                productCategoriesLayout.setVisibility(View.GONE);
                productsLayout.setVisibility(View.VISIBLE);

                filterProducts();
            }
        });
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
                false);
        recyclerView2.setLayoutManager(linearLayoutManager2);
        recyclerView2.setAdapter(iconOptionAdapter);

        etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchValue = editable.toString();

                filterProducts();
            }
        });

        btnAddProduct.setOnClickListener(view -> productDialog.showDialog());

        btnChangeCategory.setOnClickListener(view1 -> {
            if (currentStep < maxStep)
                currentStep++;

            productsLayout.setVisibility(View.GONE);
            productCategoriesLayout.setVisibility(View.VISIBLE);
        });

        btnBack.setOnClickListener(view1 -> {
            currentStep--;

            productCategoriesLayout.setVisibility(View.GONE);
            productsLayout.setVisibility(View.VISIBLE);
        });
    }

    private ValueEventListener getProdValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    products.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null)
                                products.add(product);
                        }
                    }

                    if (products.size() == 0)
                        tvProductCaption.setVisibility(View.VISIBLE);
                    else
                        tvProductCaption.setVisibility(View.GONE);
                    tvProductCaption.bringToFront();

                    adminProductAdapter.notifyDataSetChanged();
                }

                productCategoriesQuery.addValueEventListener(getProdCatValueListener());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "productsQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the products.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getProdCatValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    productCategories.clear();
                    productCategoriesId.clear();

                    if (snapshot.exists()) {
                        productCategories.add(new IconOption(getString(R.string.all), 0));
                        productCategoriesId.add("prodCat00");

                        for (Product product : products) {
                            List<String> categoryIds = new ArrayList<>(product.getCategories().values());
                            List<String> categories = new ArrayList<>();

                            for (String categoryId : categoryIds)
                                categories.add(snapshot.child(categoryId.trim())
                                        .child("name").getValue(String.class));

                            productsCategories.add(TextUtils.join(", ", categories));
                        }

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            productCategories.add(new IconOption(dataSnapshot.child("name").getValue(String.class), 0));
                            productCategoriesId.add(dataSnapshot.getKey());

                            if (selectedCategoryId != null && selectedCategoryId.equals(dataSnapshot.getKey())) {
                                selectedCategoryIndex = productCategoriesId.size() - 1;
                                tvSelectedCategory.setText(productCategories.get(selectedCategoryIndex).getLabelName());
                            }
                        }
                    }

                    productsCopy.clear();
                    productsCategoriesCopy.clear();

                    productsCopy.addAll(products);
                    productsCategoriesCopy.addAll(productsCategories);

                    filterProducts();

                    iconOptionAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "productCategoriesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the product categories.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterProducts() {
        List<Product> productsTemp = new ArrayList<>(productsCopy);
        List<String> productsCategoriesTemp = new ArrayList<>(productsCategoriesCopy);

        products.clear();

        for (int i = 0; i < productsTemp.size(); i++) {
            List<String> categoriesId = new ArrayList<>(productsTemp.get(i).getCategories().values());

            boolean isSelectedCategory = selectedCategoryId == null || selectedCategoryIndex == 0 ||
                    categoriesId.contains(selectedCategoryId);

            boolean isSearchedValue = searchValue == null || searchValue.trim().length() == 0 ||
                    productsTemp.get(i).getName().toLowerCase().contains(searchValue.trim().toLowerCase()) ||
                    productsCategoriesTemp.get(i).toLowerCase().contains(searchValue.trim().toLowerCase());

            if (isSelectedCategory && isSearchedValue) {
                products.add(productsTemp.get(i));
                productsCategories.add(productsCategoriesTemp.get(i));
            }
        }

        if (products.size() == 0)
            tvProductCaption.setVisibility(View.VISIBLE);
        else tvProductCaption.setVisibility(View.GONE);
        tvProductCaption.bringToFront();

        adminProductAdapter.notifyDataSetChanged();
    }

    public void onBackPressed() {
        if (currentStep > 0)
            currentStep--;
        else
            super.onBackPressed();

        if (currentStep == 0) {
            productCategoriesLayout.setVisibility(View.GONE);
            productsLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        productsQuery.addValueEventListener(getProdCatValueListener());

        super.onResume();
    }

    @Override
    public void onStop() {
        isListening = false;

        super.onStop();
    }

    @Override
    public void onDestroy() {
        isListening = false;

        super.onDestroy();
    }
}