package com.example.exam;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddPasswordActivity extends AppCompatActivity {
    private EditText editTextService, editTextLogin, editTextPassword, editTextNotes;
    private Button buttonSave;
    private DatabaseHelper db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");
        initViews();
        setupListeners();
    }

    private void initViews() {
        editTextService = findViewById(R.id.editTextService);
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextNotes = findViewById(R.id.editTextNotes);
        buttonSave = findViewById(R.id.buttonSave);
    }

    private void setupListeners() {
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String service = editTextService.getText().toString().trim();
                String login = editTextLogin.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String notes = editTextNotes.getText().toString().trim();

                if (service.isEmpty() || login.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AddPasswordActivity.this,
                            "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean added = db.addPassword(1, service, login, password, notes); // В реальном приложении нужно передавать правильный user_id
                if (added) {
                    Toast.makeText(AddPasswordActivity.this, "Пароль сохранён", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddPasswordActivity.this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}