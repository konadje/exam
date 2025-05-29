package com.example.exam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView textViewWelcome, textViewNoPasswords;
    private Button buttonAddPassword;
    private ListView listViewPasswords;
    private DatabaseHelper db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");
        initViews();
        setupListeners();
        loadPasswords();
    }

    private void initViews() {
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewNoPasswords = findViewById(R.id.textViewNoPasswords);
        buttonAddPassword = findViewById(R.id.buttonAddPassword);
        listViewPasswords = findViewById(R.id.listViewPasswords);

        textViewWelcome.setText("Добро пожаловать, " + username + "!");
    }

    private void setupListeners() {
        buttonAddPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPasswordActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        listViewPasswords.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PasswordEntry entry = (PasswordEntry) parent.getItemAtPosition(position);
                Toast.makeText(MainActivity.this,
                        "Сервис: " + entry.getService() +
                                "\nЛогин: " + entry.getLogin() +
                                "\nПароль: " + entry.getPassword(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadPasswords() {
        // Здесь нужно получить user_id по username, но для простоты будем передавать username
        List<PasswordEntry> passwordList = db.getAllPasswords(1); // В реальном приложении нужно получить правильный user_id

        if (passwordList.isEmpty()) {
            textViewNoPasswords.setVisibility(View.VISIBLE);
            listViewPasswords.setVisibility(View.GONE);
        } else {
            textViewNoPasswords.setVisibility(View.GONE);
            listViewPasswords.setVisibility(View.VISIBLE);

            PasswordAdapter adapter = new PasswordAdapter(this, passwordList);
            listViewPasswords.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPasswords();
    }
}