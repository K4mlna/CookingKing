package com.example.cookingking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

public class InfoFragment extends Fragment {
    private TextView textView;
    private ImageView imageView;
    private TextView areaView;
    private TextView ingredientsView;
    private TextView instructionsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        imageView = view.findViewById(R.id.imageView);
        textView = view.findViewById(R.id.tvTitle);
        areaView = view.findViewById(R.id.Area);
        ingredientsView = view.findViewById(R.id.tvIngredients);
        instructionsView = view.findViewById(R.id.tvMethod);


        // récupère les argmuents passés au fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            String mealName = arguments.getString("mealName");
            String mealThumbUrl = arguments.getString("mealThumbUrl");
            String mealArea = arguments.getString("mealArea");
            String mealIngedients = arguments.getString("mealIngredients");
            String mealInstructions = arguments.getString("mealInstructions");

            // permet d'afficher l'image et le texte
            textView.setText(mealName);
            Picasso.get().load(mealThumbUrl).into(imageView);
            areaView.setText("This recipe is " + mealArea);
            ingredientsView.setText(mealIngedients);
            instructionsView.setText(mealInstructions);

        }

        return view;
    }
}