/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * OFFLINE MODE
 * A helper class that uses SQLite to insert and read data during offline account creation and login.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DBName = "Login.db";
    public static final String Table_Name = "users";
    public static final String EMAIL = "email";

    public DBHelper(Context context) {
        super(context, DBName, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase LoginDB) {
        LoginDB.execSQL("create Table  users (email TEXT primary key, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase LoginDB, int oldVersion, int newVersion) {
        LoginDB.execSQL("drop Table if exists users");
    }

    //A function to insert email, and password values then validate if they exist in the database.
    public Boolean insert(String email, String password) {
        SQLiteDatabase LoginDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("password", password);
        long result = LoginDB.insert("users", null, contentValues);

        return result != -1;
    }

    //Check if email exists in database.
    public Boolean checkEmail(String email) {
        SQLiteDatabase LoginDB = this.getWritableDatabase();
        Cursor cursor = LoginDB.rawQuery("Select * from users where email = ?", new String[] {email});
        return cursor.getCount() > 0;
    }

    //A function to check the email, and password values then validate if they exist in the database.
    public Boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase LoginDB = this.getWritableDatabase();
        Cursor cursor = LoginDB.rawQuery("Select * from users where email = ? and password = ?", new String[] {email, password});
        return cursor.getCount() > 0;
    }

    public Cursor getData(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + Table_Name + " where email =?", new String[] {email});
        return res;
    }
}
