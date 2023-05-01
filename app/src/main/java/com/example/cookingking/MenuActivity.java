package com.example.cookingking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cookingking.placeholder.BlankFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class MenuActivity extends AppCompatActivity {
    private int Status = 0;
    private boolean showRandomRecipes = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        randomRecipes(showRandomRecipes); // Choisit des recettes aléatoires
    }

    @Override
    public void onBackPressed() {
        if (Status==0) {
            super.onBackPressed();
            Intent otherActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(otherActivity);
            finish();
        }
        if (Status==1) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout,new BlankFragment());
            fragmentTransaction.commit();
            Status = 0;
        }
    }



    public void onButtonSearchClicked(View view) {

        LinearLayout containerLayout = findViewById(R.id.containerLayout);
        containerLayout.removeAllViews();

        // Cette variable permet d'empêcher l'app d'essayer d'afficher à la fois les recettes aléatoires
        // et les recettes cherchées par l'utilisateur.
        showRandomRecipes = false;

        EditText editText = (EditText) findViewById(R.id.searchEditText);
        String inputText = editText.getText().toString();

        searchForRecipes(inputText);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void makeApiRequest(String input, final ApiResponseCallback callback) {
        String url = "https://www.themealdb.com/api/json/v1/1/search.php?s=" + input;
        RequestQueue queue = Volley.newRequestQueue(this);

        // Crée une requête HTTP
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Invoke the callback with the response
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });

        // Ajoute la requête à la file
        queue.add(jsonObjectRequest);
    }

    interface ApiResponseCallback {
        void onSuccess(JSONObject response);
        void onError(VolleyError error);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Génère une liste de recettes aléatoires parmi une liste
    // en attendant que l'utilisateur recherche une recette.
    public void randomRecipes(boolean addRandomRecipes) {
        if (addRandomRecipes) {
        String[] liste = {"rice", "salad", "cheese", "pie", "mango", "sorbet", "lasagna", "yogurt", "pizza", "burger"};
        Random random = new Random();
        ArrayList<String> randomList = new ArrayList<>(Arrays.asList(liste));
        Collections.shuffle(randomList);
        String[] rngRecipes = randomList.subList(0, 10).toArray(new String[0]);

        LinearLayout containerLayout = findViewById(R.id.containerLayout);

        for (String recipe : rngRecipes) {
            makeApiRequest(recipe, new ApiResponseCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        JSONArray meals = response.getJSONArray("meals");
                        if (meals.length() > 0) {
                            JSONObject meal = meals.getJSONObject(0);

                            String mealName = meal.getString("strMeal");
                            String mealThumbUrl = meal.getString("strMealThumb");
                            String mealArea = meal.getString("strArea");
                            String mealIngredients = "";
                            for (int j = 1; j <= 20; j++) {
                                String ingredient = meal.getString("strIngredient" + j);
                                String measurement = meal.getString("strMeasure" + j);
                                if (ingredient != null && !ingredient.isEmpty() && !ingredient.equalsIgnoreCase("null") && !ingredient.equalsIgnoreCase("undefined")
                                        && measurement != null && !measurement.isEmpty() && !measurement.equalsIgnoreCase("null") && !measurement.equalsIgnoreCase("undefined")) {
                                    if (!mealIngredients.isEmpty()) {
                                        mealIngredients += "\n";
                                    }
                                    mealIngredients += ingredient + ": " + measurement;
                                }
                            }
                            String mealInstructions = meal.getString("strInstructions");

                            // Crée un RelativeLayout pour mettre les informations à placer dans le fragment
                            RelativeLayout recipeLayout = new RelativeLayout(MenuActivity.this);
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
                            int margin = getResources().getDimensionPixelSize(R.dimen.margin);
                            layoutParams.setMargins(margin, margin, margin, margin);
                            recipeLayout.setLayoutParams(layoutParams);

                            String finalMealIngredients = mealIngredients;
                            recipeLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                    // Crée une nouvelle instance d'InfoFragment

                                    InfoFragment infoFragment = new InfoFragment();


                                    // Create a Bundle to pass the text and image data
                                    Bundle bundle = new Bundle();
                                    bundle.putString("mealName", mealName);
                                    bundle.putString("mealThumbUrl", mealThumbUrl);
                                    bundle.putString("mealArea", mealArea);
                                    bundle.putString("mealIngredients", finalMealIngredients);
                                    bundle.putString("mealInstructions", mealInstructions);

                                    // Arguments d'InfoFragment

                                        infoFragment.setArguments(bundle);

                                        fragmentTransaction.replace(R.id.frameLayout, infoFragment);
                                        fragmentTransaction.commit();
                                        Status = 1;

                                }
                            });

                            // ImageView pour l'image
                            ImageView imageView = new ImageView(MenuActivity.this);
                            RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    dpToPx(150)); // Convertit dp en pixels
                            imageView.setLayoutParams(imageLayoutParams);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            Picasso.get().load(mealThumbUrl).into(imageView);

                            // TextView pour le nom de la recette
                            TextView textView = new TextView(MenuActivity.this);
                            RelativeLayout.LayoutParams textLayoutParams = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
                            textView.setLayoutParams(textLayoutParams);
                            textView.setBackgroundColor(Color.parseColor("#9900661a"));
                            textView.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
                            textView.setText(mealName); // Nom de la recette
                            textView.setTextColor(Color.WHITE);

                            // Ajoute ImageView et TextView au recipe layout
                            recipeLayout.addView(imageView);
                            recipeLayout.addView(textView);

                            // Ajoute recipe layout au container layout
                            containerLayout.addView(recipeLayout);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    Log.d("MenuActivity", "I GOT AN ERROR");
                }
            });
        }
        }
        // Quand le bouton 'rechercher' est appuyé, le code passe ici.
        // On ne met rien pour pas que cela crée de conflit en tentant d'afficher les randomRecipes et les résultats de recherche
    }


    public void searchForRecipes(String inputText){
        // Affiche toutes les recettes trouvées
        LinearLayout containerLayout = findViewById(R.id.containerLayout);
        makeApiRequest(inputText, new ApiResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray meals = response.getJSONArray("meals");
                    for (int x = 0; x < meals.length(); x++) {
                        JSONObject meal = meals.getJSONObject(x);

                        String mealName = meal.getString("strMeal");
                        String mealThumbUrl = meal.getString("strMealThumb");
                        String mealArea = meal.getString("strArea");
                        String mealIngredients = "";
                        for (int j = 1; j <= 20; j++) {
                            String ingredient = meal.getString("strIngredient" + j);
                            String measurement = meal.getString("strMeasure" + j);
                            if (ingredient != null && !ingredient.isEmpty() && !ingredient.equalsIgnoreCase("null") && !ingredient.equalsIgnoreCase("undefined")
                                    && measurement != null && !measurement.isEmpty() && !measurement.equalsIgnoreCase("null") && !measurement.equalsIgnoreCase("undefined")) {
                                if (!mealIngredients.isEmpty()) {
                                    mealIngredients += "\n";
                                }
                                mealIngredients += ingredient + ": " + measurement;
                            }
                        }
                        String mealInstructions = meal.getString("strInstructions");

                        // Crée un nouveau RelativeLayout pour contenir la recette
                        RelativeLayout recipeLayout = new RelativeLayout(MenuActivity.this);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        int margin = getResources().getDimensionPixelSize(R.dimen.margin);
                        layoutParams.setMargins(margin, margin, margin, margin);
                        recipeLayout.setLayoutParams(layoutParams);

                        String finalMealIngredients = mealIngredients;
                        recipeLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                // Nouvelle instance d'InfoFragment

                                InfoFragment infoFragment = new InfoFragment();


                                // On 'pack' tous les détails qu'on veut ajouter au fragment
                                Bundle bundle = new Bundle();
                                bundle.putString("mealName", mealName);
                                bundle.putString("mealThumbUrl", mealThumbUrl);
                                bundle.putString("mealArea", mealArea);
                                bundle.putString("mealIngredients", finalMealIngredients);
                                bundle.putString("mealInstructions", mealInstructions);

                                // On passe ce 'pack' comme argument du fragment

                                infoFragment.setArguments(bundle);

                                fragmentTransaction.replace(R.id.frameLayout, infoFragment);
                                fragmentTransaction.commit();
                                Status = 1;

                            }
                        });

                        // ImageView pour l'image du fragment
                        ImageView imageView = new ImageView(MenuActivity.this);
                        RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                dpToPx(150)); // Convert dp to pixels
                        imageView.setLayoutParams(imageLayoutParams);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Picasso.get().load(mealThumbUrl).into(imageView); // Load the image using Picasso

                        // TextView pour le titre du fragment
                        TextView textView = new TextView(MenuActivity.this);
                        RelativeLayout.LayoutParams textLayoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        textView.setLayoutParams(textLayoutParams);
                        textView.setBackgroundColor(Color.parseColor("#9900661a")); // Set background color
                        textView.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8)); // Set padding
                        textView.setText(mealName); // Set recipe name
                        textView.setTextColor(Color.WHITE);

                        recipeLayout.addView(imageView);
                        recipeLayout.addView(textView);

                        // Ajout du recipe layout au container layout
                        containerLayout.addView(recipeLayout);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.d("MenuActivity", "I GOT AN ERROR");
            }
        });
    };
}