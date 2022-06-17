package com.example.mobilepalengke.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.mobilepalengke.Activities.AdminActivity;
import com.example.mobilepalengke.Activities.MealPlanCategoriesActivity;
import com.example.mobilepalengke.Activities.ProductCategoriesActivity;
import com.example.mobilepalengke.Activities.ProductDetailsActivity;
import com.example.mobilepalengke.Activities.ProductsActivity;
import com.example.mobilepalengke.Adapters.MealPlanCategoryAdapter;
import com.example.mobilepalengke.Adapters.ProductCategoryAdapter;
import com.example.mobilepalengke.DataClasses.MealPlanCategory;
import com.example.mobilepalengke.DataClasses.Product;
import com.example.mobilepalengke.DataClasses.ProductCategory;
import com.example.mobilepalengke.DataClasses.Role;
import com.example.mobilepalengke.DataClasses.User;
import com.example.mobilepalengke.DialogClasses.LoadingDialog;
import com.example.mobilepalengke.DialogClasses.MessageDialog;
import com.example.mobilepalengke.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {

    ImageSlider imageSliderBanner;
    CardView adminCardView;
    ImageView imgNewProd, imgNewProd2, imgNewProd3;
    ImageView imgFeatProd, imgFeatProd2, imgFeatProd3;
    ImageView imgFacebook, imgTwitter, imgInstagram;
    RecyclerView recyclerView, recyclerView2;
    Button btnDashboard, btnViewAll, btnViewAll2, btnViewAll3, btnViewAll4;
    TextView tvFullName, tvRoles, tvNewProdCaption, tvFeatProdCaption, tvProdCategoryCaption,
            tvMealPlanCaption, tvNewProd, tvNewProd2, tvNewProd3, tvFeatProd, tvFeatProd2, tvFeatProd3;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    boolean isListening = true;

    Query homeSliderImagesQuery, userQuery, adminRolesQuery, featuredProductsQuery, newProductsQuery,
            productCategoriesQuery, mealPlanCategoriesQuery;

    String uid;
    User user;

    List<SlideModel> slideModels = new ArrayList<>();
    List<Product> featuredProducts = new ArrayList<>(), newProducts = new ArrayList<>();
    List<ProductCategory> productCategories = new ArrayList<>();
    List<MealPlanCategory> mealPlanCategories = new ArrayList<>();

    ProductCategoryAdapter productCategoryAdapter;
    MealPlanCategoryAdapter mealPlanCategoryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imageSliderBanner = view.findViewById(R.id.imageSliderBanner);

        adminCardView = view.findViewById(R.id.adminCardView);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvRoles = view.findViewById(R.id.tvRoles);
        btnDashboard = view.findViewById(R.id.btnDashboard);

        imgNewProd = view.findViewById(R.id.imgNewProd);
        imgNewProd2 = view.findViewById(R.id.imgNewProd2);
        imgNewProd3 = view.findViewById(R.id.imgNewProd3);
        tvNewProd = view.findViewById(R.id.tvNewProd);
        tvNewProd2 = view.findViewById(R.id.tvNewProd2);
        tvNewProd3 = view.findViewById(R.id.tvNewProd3);
        btnViewAll = view.findViewById(R.id.btnViewAll);
        tvNewProdCaption = view.findViewById(R.id.tvNewProdCaption);

        imgFeatProd = view.findViewById(R.id.imgFeatProd);
        imgFeatProd2 = view.findViewById(R.id.imgFeatProd2);
        imgFeatProd3 = view.findViewById(R.id.imgFeatProd3);
        tvFeatProd = view.findViewById(R.id.tvFeatProd);
        tvFeatProd2 = view.findViewById(R.id.tvFeatProd2);
        tvFeatProd3 = view.findViewById(R.id.tvFeatProd3);
        btnViewAll2 = view.findViewById(R.id.btnViewAll2);
        tvFeatProdCaption = view.findViewById(R.id.tvFeatProdCaption);

        recyclerView = view.findViewById(R.id.recyclerView);
        btnViewAll3 = view.findViewById(R.id.btnViewAll3);
        tvProdCategoryCaption = view.findViewById(R.id.tvProdCategoryCaption);

        recyclerView2 = view.findViewById(R.id.recyclerView2);
        btnViewAll4 = view.findViewById(R.id.btnViewAll4);
        tvMealPlanCaption = view.findViewById(R.id.tvMealPlanCaption);

        imgFacebook = view.findViewById(R.id.imgFacebook);
        imgTwitter = view.findViewById(R.id.imgTwitter);
        imgInstagram = view.findViewById(R.id.imgInstagram);

        context = getContext();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            uid = firebaseUser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));

        homeSliderImagesQuery = firebaseDatabase.getReference("dynamicContents").child("homeSliderImages");
        userQuery = firebaseDatabase.getReference("users").child(uid);
        adminRolesQuery = firebaseDatabase.getReference("roles").child("adminRoles");
        featuredProductsQuery = firebaseDatabase.getReference("products").orderByChild("id");
        newProductsQuery = firebaseDatabase.getReference("products").orderByChild("id");
        productCategoriesQuery = firebaseDatabase.getReference("productCategories").orderByChild("id");
        mealPlanCategoriesQuery = firebaseDatabase.getReference("mealPlanCategories").orderByChild("id");

        loadingDialog.showDialog();
        isListening = true;
        homeSliderImagesQuery.addValueEventListener(getHSImagesValueListener());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false);
        productCategoryAdapter = new ProductCategoryAdapter(context, productCategories);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(productCategoryAdapter);

        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false);
        mealPlanCategoryAdapter = new MealPlanCategoryAdapter(context, mealPlanCategories);
        recyclerView2.setLayoutManager(gridLayoutManager2);
        recyclerView2.setAdapter(mealPlanCategoryAdapter);

        btnDashboard.setOnClickListener(view12 -> {
            Intent intent = new Intent(context, AdminActivity.class);
            startActivity(intent);
        });

        btnViewAll.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, ProductsActivity.class);
            startActivity(intent);
        });

        btnViewAll2.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, ProductsActivity.class);
            startActivity(intent);
        });

        btnViewAll3.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, ProductCategoriesActivity.class);
            startActivity(intent);
        });

        btnViewAll4.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, MealPlanCategoriesActivity.class);
            startActivity(intent);
        });

        imgFacebook.setOnClickListener(view1 -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.mp_facebook_url)));
            startActivity(intent);
        });

        imgTwitter.setOnClickListener(view1 -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.mp_twitter_url)));
            startActivity(intent);
        });

        imgInstagram.setOnClickListener(view1 -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.mp_instagram_url)));
            startActivity(intent);
        });

        return view;
    }

    private ValueEventListener getHSImagesValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    slideModels.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String image = dataSnapshot.getValue(String.class);
                            if (image != null)
                                slideModels.add(new SlideModel(image, null));
                        }
                    }

                    imageSliderBanner.setImageList(slideModels, ScaleTypes.CENTER_INSIDE);

                    userQuery.addValueEventListener(getUserValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "homeSliderImagesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the images.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getUserValueListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User.class);
                        if (user != null) {
                            String fullName = user.getLastName() + " " + user.getFirstName();
                            tvFullName.setText(fullName);
                        }
                    }

                    adminRolesQuery.addValueEventListener(getARValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "userQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the user's data.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getARValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists()) {
                        List<String> roleIds = new ArrayList<>(user.getRoles().values());

                        String roles = "";
                        for (String roleId : roleIds)
                            if (roleId.contains("ar")) {
                                Role role = snapshot.child(roleId.trim()).getValue(Role.class);
                                if (role != null)
                                    roles += "• " + role.getName() + "\n";
                            }

                        if (roles.trim().length() > 0) {
                            tvRoles.setText(roles.trim());
                            adminCardView.setVisibility(View.VISIBLE);
                        } else {
                            tvRoles.setText(getString(R.string.none));
                            adminCardView.setVisibility(View.GONE);
                        }
                    }

                    featuredProductsQuery.addValueEventListener(getFeatProdValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "adminRolesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the admin roles.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getFeatProdValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    featuredProducts.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null && !product.isDeactivated())
                                featuredProducts.add(product);
                        }
                    }

                    Collections.shuffle(featuredProducts);

                    if (featuredProducts.size() >= 1) {
                        Picasso.get().load(featuredProducts.get(0).getImg()).placeholder(R.drawable.ic_image_blue)
                                .error(R.drawable.ic_broken_image_red).into(imgFeatProd);
                        tvFeatProd.setText(featuredProducts.get(0).getName());

                        imgFeatProd.setVisibility(View.VISIBLE);
                        tvFeatProd.setVisibility(View.VISIBLE);
                        tvFeatProdCaption.setVisibility(View.GONE);

                        imgFeatProd.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ProductDetailsActivity.class);
                            intent.putExtra("productId", featuredProducts.get(0).getId());
                            context.startActivity(intent);
                        });
                    } else {
                        imgFeatProd.setVisibility(View.GONE);
                        tvFeatProd.setVisibility(View.GONE);
                        tvFeatProdCaption.setVisibility(View.VISIBLE);
                    }
                    if (featuredProducts.size() >= 2) {
                        Picasso.get().load(featuredProducts.get(1).getImg()).placeholder(R.drawable.ic_image_blue)
                                .error(R.drawable.ic_broken_image_red).into(imgFeatProd2);
                        tvFeatProd2.setText(featuredProducts.get(1).getName());

                        imgFeatProd2.setVisibility(View.VISIBLE);
                        tvFeatProd2.setVisibility(View.VISIBLE);

                        imgFeatProd2.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ProductDetailsActivity.class);
                            intent.putExtra("productId", featuredProducts.get(1).getId());
                            context.startActivity(intent);
                        });
                    } else {
                        imgFeatProd2.setVisibility(View.GONE);
                        tvFeatProd2.setVisibility(View.GONE);
                    }
                    if (featuredProducts.size() >= 3) {
                        Picasso.get().load(featuredProducts.get(2).getImg()).placeholder(R.drawable.ic_image_blue)
                                .error(R.drawable.ic_broken_image_red).into(imgFeatProd3);
                        tvFeatProd3.setText(featuredProducts.get(2).getName());

                        imgFeatProd3.setVisibility(View.VISIBLE);
                        tvFeatProd3.setVisibility(View.VISIBLE);

                        imgFeatProd3.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ProductDetailsActivity.class);
                            intent.putExtra("productId", featuredProducts.get(2).getId());
                            context.startActivity(intent);
                        });
                    } else {
                        imgFeatProd3.setVisibility(View.GONE);
                        tvFeatProd3.setVisibility(View.GONE);
                    }

                    newProductsQuery.addValueEventListener(getNewProdValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "featuredProductsQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the featured products.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getNewProdValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    newProducts.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Product product = dataSnapshot.getValue(Product.class);
                            if (product != null && !product.isDeactivated())
                                newProducts.add(product);
                        }
                    }

                    Collections.reverse(newProducts);

                    if (newProducts.size() >= 1) {
                        Picasso.get().load(newProducts.get(0).getImg()).placeholder(R.drawable.ic_image_blue)
                                .error(R.drawable.ic_broken_image_red).into(imgNewProd);
                        tvNewProd.setText(newProducts.get(0).getName());

                        imgNewProd.setVisibility(View.VISIBLE);
                        tvNewProd.setVisibility(View.VISIBLE);
                        tvNewProdCaption.setVisibility(View.GONE);

                        imgNewProd.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ProductDetailsActivity.class);
                            intent.putExtra("productId", newProducts.get(0).getId());
                            context.startActivity(intent);
                        });
                    } else {
                        imgNewProd.setVisibility(View.GONE);
                        tvNewProd.setVisibility(View.GONE);
                        tvNewProdCaption.setVisibility(View.VISIBLE);
                    }
                    if (newProducts.size() >= 2) {
                        Picasso.get().load(newProducts.get(1).getImg()).placeholder(R.drawable.ic_image_blue)
                                .error(R.drawable.ic_broken_image_red).into(imgNewProd2);
                        tvNewProd2.setText(newProducts.get(1).getName());

                        imgNewProd2.setVisibility(View.VISIBLE);
                        tvNewProd2.setVisibility(View.VISIBLE);

                        imgNewProd2.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ProductDetailsActivity.class);
                            intent.putExtra("productId", newProducts.get(1).getId());
                            context.startActivity(intent);
                        });
                    } else {
                        imgNewProd2.setVisibility(View.GONE);
                        tvNewProd2.setVisibility(View.GONE);
                    }
                    if (newProducts.size() >= 3) {
                        Picasso.get().load(newProducts.get(2).getImg()).placeholder(R.drawable.ic_image_blue)
                                .error(R.drawable.ic_broken_image_red).into(imgNewProd3);
                        tvNewProd3.setText(newProducts.get(2).getName());

                        imgNewProd3.setVisibility(View.VISIBLE);
                        tvNewProd3.setVisibility(View.VISIBLE);

                        imgNewProd3.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ProductDetailsActivity.class);
                            intent.putExtra("productId", newProducts.get(2).getId());
                            context.startActivity(intent);
                        });
                    } else {
                        imgNewProd3.setVisibility(View.GONE);
                        tvNewProd3.setVisibility(View.GONE);
                    }

                    productCategoriesQuery.addValueEventListener(getProdCatValueListener());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "newProductsQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the new products.");
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

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ProductCategory productCategory = dataSnapshot.getValue(ProductCategory.class);
                            if (productCategory != null)
                                productCategories.add(productCategory);
                        }
                    }

                    Collections.shuffle(productCategories);

                    List<ProductCategory> productCategoriesCopy = new ArrayList<>(productCategories);
                    productCategories.clear();
                    for (int i = 0; i < Math.min(3, productCategoriesCopy.size()); i++)
                        productCategories.add(productCategoriesCopy.get(i));

                    if (productCategories.size() == 0)
                        tvProdCategoryCaption.setVisibility(View.VISIBLE);
                    else
                        tvProdCategoryCaption.setVisibility(View.GONE);

                    productCategoryAdapter.notifyDataSetChanged();

                    mealPlanCategoriesQuery.addValueEventListener(getMealPlanCatValueListener());
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

    private ValueEventListener getMealPlanCatValueListener() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    mealPlanCategories.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MealPlanCategory mealPlanCategory = dataSnapshot.getValue(MealPlanCategory.class);
                            if (mealPlanCategory != null)
                                mealPlanCategories.add(mealPlanCategory);
                        }
                    }

                    Collections.shuffle(mealPlanCategories);

                    List<MealPlanCategory> mealPlanCategoriesCopy = new ArrayList<>(mealPlanCategories);
                    mealPlanCategories.clear();
                    for (int i = 0; i < Math.min(3, mealPlanCategoriesCopy.size()); i++)
                        mealPlanCategories.add(mealPlanCategoriesCopy.get(i));

                    if (mealPlanCategories.size() == 0)
                        tvMealPlanCaption.setVisibility(View.VISIBLE);
                    else
                        tvMealPlanCaption.setVisibility(View.GONE);

                    mealPlanCategoryAdapter.notifyDataSetChanged();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "mealPlanCategoriesQuery:onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setTextCaption("Failed to get the meal plan categories.");
                messageDialog.setTextType(2);
                messageDialog.showDialog();
            }
        };
    }

    @Override
    public void onResume() {
        isListening = true;
        homeSliderImagesQuery.addValueEventListener(getHSImagesValueListener());

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