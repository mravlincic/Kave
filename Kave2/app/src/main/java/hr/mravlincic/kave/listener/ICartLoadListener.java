package hr.mravlincic.kave.listener;

import java.util.List;

import hr.mravlincic.kave.model.CartModel;
import hr.mravlincic.kave.model.DrinkModel;

public interface ICartLoadListener {
    void onCartLoadSuccess(List<CartModel> cartModelList);
    void onCartLoadFailed(String message);
}
