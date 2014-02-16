package com.yoekido.cashbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper
{
    final SQLiteDatabase writable, readable;
    Context context;

    public DBHelper (Context context)
    {
        super(context, "db_cashbook", null, 1);
        this.context = context;
        writable = getWritableDatabase();
        readable = getReadableDatabase();
    }

    public long newEntry (String content, int amount, long time, int category)
    {
        ContentValues insertValues = new ContentValues();
        insertValues.put("content", content);
        insertValues.put("amount", amount);
        insertValues.put("time", time);
        insertValues.put("category", category);
        return writable.insert("entry", null, insertValues);
    }

    public long updateEntry (String content, int amount, long time, int category, int id)
    {
        ContentValues insertValues = new ContentValues();
        insertValues.put("content", content);
        insertValues.put("amount", amount);
        insertValues.put("time", time);
        insertValues.put("category", category);
        return writable.update("entry", insertValues, "id = ?", new String[] { Integer.toString(id) });
    }

    public int deleteEntry (int id)
    {
        return writable.delete("entry", "id = ?", new String[] { Integer.toString(id) });
    }

    public long newCategory (String name)
    {
        ContentValues insertValues = new ContentValues();
        insertValues.put("name", name);
        return writable.insert("category", null, insertValues);
    }

    public SparseArray<String> getCategories ()
    {
        Cursor c = readable.query("category", new String[] { "id", "name" }, null, null, null, null, null);
        SparseArray<String> categories = new SparseArray<String>();
        boolean mov = c.moveToFirst();
        while (mov)
        {
            categories.put(c.getInt(0), c.getString(1));
            mov = c.moveToNext();
        }
        c.close();
        return categories;
    }

    public List<Entry> getEntries (int offset, int limit)
    {
        Cursor c = readable.rawQuery("SELECT id, time, content, amount, category FROM entry " +
                                        "ORDER BY time DESC" +
                                        (limit > 0 ? " LIMIT " + (offset * limit) + ", " + Integer.toString(limit) : ""), null);

        List<Entry> list = new ArrayList<Entry>();

        boolean mov = c.moveToFirst();
        while (mov)
        {
            list.add(new Entry(c.getInt(0), c.getLong(1), c.getString(2), c.getInt(3), c.getInt(4), context));
            mov = c.moveToNext();
        }
        c.close();
        return list;
    }

    @Override
    public void onCreate (SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS entry (id INTEGER PRIMARY KEY, " +
                    "time INTEGER NOT NULL, content TEXT NOT NULL, " +
                    "amount INTEGER NOT NULL, category INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS category (id INTEGER PRIMARY KEY, name TEXT NOT NULL);");
        db.execSQL("INSERT INTO category (id, name) VALUES (0, '未分類');");
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

}