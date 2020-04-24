package com.example.bc_eats;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;


public class FoodList{
    private static FoodList sFoodList;
    private List<Food> mFoods;

    public FoodList(Context context){
        mFoods = new ArrayList<>();
    }

    public static FoodList get(Context context){
        if(sFoodList == null){
            sFoodList = new FoodList(context);
        }
        return sFoodList;
    }

    public List<Food> getFoods(){
        return mFoods;
    }

}
