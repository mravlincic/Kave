package hr.mravlincic.kave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import hr.mravlincic.kave.model.User;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private Button registerUser;
    private EditText editTextFullName, editTextAge, editTextEmail, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        registerUser = (Button) findViewById(R.id.registerUser);
        registerUser.setOnClickListener(this);

        editTextFullName = (EditText) findViewById(R.id.fullName);
        editTextAge = (EditText) findViewById(R.id.age);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.registerUser:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String email=editTextEmail.getText().toString().trim();
        String password=editTextPassword.getText().toString().trim();
        String fullName=editTextFullName.getText().toString().trim();
        String age=editTextAge.getText().toString().trim();

        if(fullName.isEmpty()){
            editTextFullName.setError("Ime i prezime je obavezno!");
            editTextFullName.requestFocus();
            return;}

        if(age.isEmpty()){
            editTextAge.setError("Godine su obavezne!");
            editTextAge.requestFocus();
            return;}

        if(email.isEmpty()){
            editTextEmail.setError("Email je obavezan!");
            editTextEmail.requestFocus();
            return;}

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Unesite ispravnu email adresu!");
            editTextEmail.requestFocus();
            return; }

        if(password.isEmpty()){
            editTextPassword.setError("Lozinka je obavezna!");
            editTextPassword.requestFocus();
            return;}

        if(password.length() < 6){
            editTextPassword.setError("Lozinka mora sadržavati najmanje 6 znakova!");
            editTextPassword.requestFocus();
            return;
        }

        //stvaranje korisnickog racuna
        mAuth.createUserWithEmailAndPassword(email, password)

                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //ako korisnik nije registrirani
                        if(task.isSuccessful()){
                            User user = new User(fullName, age, email);

                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            //ako je korisnik registrirani i podaci su dodani u bazu
                                if(task.isSuccessful()){
                                    Toast.makeText(RegisterUser.this, "Registracija je uspješna!", Toast.LENGTH_LONG).show();
                                }
                                else
                                    Toast.makeText(RegisterUser.this, "Registracija nije uspješna, pokušajte ponovo!", Toast.LENGTH_LONG).show();

                            }
                        });
                        }
                        else
                            Toast.makeText(RegisterUser.this, "Korisnički račun s tim podacima već postoji!", Toast.LENGTH_LONG).show();
                    }
                });

    }
}