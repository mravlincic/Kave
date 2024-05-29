package hr.mravlincic.kave.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import hr.mravlincic.kave.R;
import hr.mravlincic.kave.eventbus.MyUpdateCartEvent;
import hr.mravlincic.kave.model.CartModel;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MycartViewHolder> {

   private Context context;
   private List<CartModel> cartModelList;

    public MyCartAdapter(Context context, List<CartModel> cartModelList) {
        this.context = context;
        this.cartModelList = cartModelList;
    }

    @NonNull
    @Override
    public MycartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MycartViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_cart_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MycartViewHolder holder, int position) {
        Glide.with(context).load(cartModelList.get(position).getImage())
                .into(holder.imageView);
        holder.txtPrice.setText(new StringBuilder().append(cartModelList
                .get(position).getPrice()));
        holder.txtName.setText(new StringBuilder().append(cartModelList
                .get(position).getName()));
        holder.txtQuantity.setText(new StringBuilder().append(cartModelList
                .get(position).getQuantity()));

        //Event
        holder.btnMinus.setOnClickListener(v -> {
            minusCartItem(holder, cartModelList.get(position));
        });
        holder.btnPlus.setOnClickListener(v -> {
            plusCartItem(holder, cartModelList.get(position));
        });

        holder.btnDelete.setOnClickListener(v -> {

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Ukloni proizvod")
                    .setMessage("Želite li ukloniti ovaj proizvod iz košarice?")
                    .setNegativeButton("NE", (dialog1, which) -> dialog1.dismiss())
                    .setPositiveButton("DA", (dialog12, which) -> {

                        //Temp remove
                        notifyItemRemoved(position);

                        deleteFromFirebase(cartModelList.get(position));
                        dialog12.dismiss();
                    }).create();
            dialog.show();

        });

    }

    private void deleteFromFirebase(CartModel cartModel) {
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID")
                .child(cartModel.getKey())
                .removeValue()
                .addOnSuccessListener(aVoid -> EventBus.getDefault().postSticky(new MyUpdateCartEvent()));
    }

    private void plusCartItem(MycartViewHolder holder, CartModel cartModel) {
        cartModel.setQuantity(cartModel.getQuantity()+1);
        cartModel.setTotalPrice(cartModel.getQuantity()*Float.parseFloat(cartModel.getPrice()));

        holder.txtQuantity.setText(new StringBuilder().append(cartModel.getQuantity()));
        updateFirebase(cartModel);
    }

    private void minusCartItem(MycartViewHolder holder, CartModel cartModel) {

        if (cartModel.getQuantity() > 1)
        {
            cartModel.setQuantity(cartModel.getQuantity()-1);
            cartModel.setTotalPrice(cartModel.getQuantity()*Float.parseFloat(cartModel.getPrice()));

            //Update Quantity
            holder.txtQuantity.setText(new StringBuilder().append(cartModel.getQuantity()));
            updateFirebase(cartModel);
        }

    }

    private void updateFirebase(CartModel cartModel) {
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID")
                .child(cartModel.getKey())
                .setValue(cartModel)
                .addOnSuccessListener(aVoid -> EventBus.getDefault().postSticky(new MyUpdateCartEvent()));
    }

    @Override
    public int getItemCount() {
        return cartModelList.size();
    }

    public class MycartViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.btnMinus)
        ImageView btnMinus;
        @BindView(R.id.btnPlus)
        ImageView btnPlus;
        @BindView(R.id.btnDelete)
        ImageView btnDelete;
        @BindView(R.id.imageView)
        ImageView imageView;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtPrice)
        TextView txtPrice;
        @BindView(R.id.txtQuantity)
        TextView txtQuantity;

        Unbinder unbinder;


        public MycartViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
