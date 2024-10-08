package hr.mravlincic.kave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hr.mravlincic.kave.adapter.MyCartAdapter;
import hr.mravlincic.kave.eventbus.MyUpdateCartEvent;
import hr.mravlincic.kave.listener.ICartLoadListener;
import hr.mravlincic.kave.model.CartModel;

public class CartActivity extends AppCompatActivity implements ICartLoadListener {

    @BindView(R.id.recycler_cart)
    RecyclerView recyclerCart;
    @BindView(R.id.mainLayout)
    RelativeLayout mainLayout;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTotal)
    TextView txtTotal;

    private TextView countDownText;
    private Button countDownButton;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeft = 600000;

    ICartLoadListener cartLoadListener;

    @Override
    protected void onStart() {//event za brojanje obavijesti za kosaricu
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(MyUpdateCartEvent.class))
            EventBus.getDefault().removeStickyEvent(MyUpdateCartEvent.class);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onUpdateCart(MyUpdateCartEvent event)
    {
        loadCartFromFirebase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        countDownText = (TextView) findViewById(R.id.countdown);
        countDownButton = (Button) findViewById(R.id.reserve);

        init();
        loadCartFromFirebase();
    }

    private void startReset() {
        if(timerRunning)
        {
            stopTimer();
        }
        else
            startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {

            }
        }.start();
        timerRunning=true;
        countDownButton.setText("OTKAŽI REZERVACIJU");
    }

    private void updateTimer() {
        int minutes = (int) timeLeft/1000/60;
        int seconds = (int) timeLeft/1000%60;

        String timeLeftText;
        timeLeftText= "" + minutes;
        timeLeftText += " : ";
        if(seconds<10) timeLeftText += "0";
        timeLeftText += seconds;

        countDownText.setText("Valjanost rezervacije: " + timeLeftText);

    }

    private void stopTimer() {
        countDownTimer.cancel();
        timeLeft=600000;
        timerRunning=false;
        countDownButton.setText("REZERVIRAJ PROIZVODE");
        countDownText.setText("");
    }

    private void loadCartFromFirebase() {
        List<CartModel> cartModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child("UNIQUE_USER_ID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            for(DataSnapshot cartSnapshot:snapshot.getChildren())
                            {
                                CartModel cartModel = cartSnapshot.getValue(CartModel.class);
                                cartModel.setKey(cartSnapshot.getKey());
                                cartModels.add(cartModel);
                            }
                            cartLoadListener.onCartLoadSuccess(cartModels);

                            countDownButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    startReset();

                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(CartActivity.this, "Košarica je prazna!", Toast.LENGTH_SHORT).show();
                            countDownButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Toast.makeText(CartActivity.this, "Dodajte proizvode u košaricu kako bi ih mogli rezervirati!", Toast.LENGTH_LONG).show();



                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cartLoadListener.onCartLoadFailed(error.getMessage());
                    }
                });
    }

    private void init(){
        ButterKnife.bind(this);

        cartLoadListener = this;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerCart.setLayoutManager(layoutManager);
        recyclerCart.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onCartLoadSuccess(List<CartModel> cartModelList) {
        double sum = 0;
        for(CartModel cartModel:cartModelList){
            sum += cartModel.getTotalPrice();
        }
        txtTotal.setText(new StringBuilder().append(sum));
        MyCartAdapter adapter = new MyCartAdapter(this, cartModelList);
        recyclerCart.setAdapter(adapter);
    }

    @Override
    public void onCartLoadFailed(String message) {
        Snackbar.make(mainLayout,message,Snackbar.LENGTH_SHORT).show();
    }
}