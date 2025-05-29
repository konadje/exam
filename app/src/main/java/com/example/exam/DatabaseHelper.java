package com.example.exam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PasswordManager.db";
    private static final int DATABASE_VERSION = 1;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Таблица паролей
    private static final String TABLE_PASSWORDS = "passwords";
    private static final String COLUMN_PASSWORD_ID = "id";
    private static final String COLUMN_USER_ID_FK = "user_id";
    private static final String COLUMN_SERVICE = "service";
    private static final String COLUMN_LOGIN = "login";
    private static final String COLUMN_PASSWORD_VALUE = "password_value";
    private static final String COLUMN_NOTES = "notes";

    // Ключ для шифрования (в реальном приложении должен быть более безопасным)
    private static final String ENCRYPTION_KEY = "MySuperSecretKey123";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Создание таблицы паролей
        String CREATE_PASSWORDS_TABLE = "CREATE TABLE " + TABLE_PASSWORDS + "("
                + COLUMN_PASSWORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID_FK + " INTEGER,"
                + COLUMN_SERVICE + " TEXT,"
                + COLUMN_LOGIN + " TEXT,"
                + COLUMN_PASSWORD_VALUE + " TEXT,"
                + COLUMN_NOTES + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USER_ID + ")" + ")";
        db.execSQL(CREATE_PASSWORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSWORDS);
        onCreate(db);
    }

    // Регистрация пользователя
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Проверка на существование пользователя
        if (checkUserExists(username)) {
            return false;
        }

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashPassword(password));

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Проверка пользователя
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, hashPassword(password)};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }

    // Проверка существования пользователя
    private boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }

    // Хеширование пароля
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Добавление пароля
    public boolean addPassword(int userId, String service, String login, String password, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            String encryptedPassword = encrypt(password);
            values.put(COLUMN_USER_ID_FK, userId);
            values.put(COLUMN_SERVICE, service);
            values.put(COLUMN_LOGIN, login);
            values.put(COLUMN_PASSWORD_VALUE, encryptedPassword);
            values.put(COLUMN_NOTES, notes);

            long result = db.insert(TABLE_PASSWORDS, null, values);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получение всех паролей пользователя
    public List<PasswordEntry> getAllPasswords(int userId) {
        List<PasswordEntry> passwordList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_PASSWORD_ID, COLUMN_SERVICE, COLUMN_LOGIN, COLUMN_PASSWORD_VALUE, COLUMN_NOTES};
        String selection = COLUMN_USER_ID_FK + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(TABLE_PASSWORDS, columns, selection, selectionArgs, null, null, COLUMN_SERVICE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                try {
                    PasswordEntry entry = new PasswordEntry();
                    entry.setId(cursor.getInt(0));
                    entry.setService(cursor.getString(1));
                    entry.setLogin(cursor.getString(2));
                    entry.setPassword(decrypt(cursor.getString(3)));
                    entry.setNotes(cursor.getString(4));
                    passwordList.add(entry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return passwordList;
    }

    // Шифрование
    private String encrypt(String data) throws Exception {
        SecretKeySpec key = generateKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }

    // Дешифрование
    private String decrypt(String encryptedData) throws Exception {
        SecretKeySpec key = generateKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedData = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData);
    }

    // Генерация ключа
    private SecretKeySpec generateKey() throws Exception {
        byte[] key = ENCRYPTION_KEY.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        return new SecretKeySpec(key, "AES");
    }
}