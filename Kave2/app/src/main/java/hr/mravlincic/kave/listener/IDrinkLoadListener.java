package hr.mravlincic.kave.listener;

import java.util.List;

import hr.mravlincic.kave.model.DrinkModel;

public interface IDrinkLoadListener {
    void onDrinkLoadSuccess(List<DrinkModel> drinkModelList);
    void onDrinkLoadFailed(String message);
}
