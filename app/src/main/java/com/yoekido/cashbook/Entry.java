package com.yoekido.cashbook;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by yk on 14/02/21.
 */
public class Entry
{
    int id;
    long time;
    String content;
    int amount;
    int categoryId;
    String category;
    Context context;

    public Entry (int id, long time, String content, int amount, int category, Context context)
    {
        this.id = id;
        this.time = time;
        this.content = content;
        this.amount = amount;
        this.categoryId = category;
        this.category = Category.fromId(category, context);
        this.context = context;
    }

    public Entry (Calendar calendar, String content, int amount, int category, Context context)
    {
        this(-1, calendar.getTimeInMillis(), content, amount, category, context);
    }

    public String toString ()
    {
        return String.format("%s\\%d%s \n %s / %s",
                content.length() == 0 ? "" : content + ": ",
                Math.abs(amount),
                amount < 0 ? " [収入]" : "",
                new SimpleDateFormat("yyyy/MM/dd HH:mm").format(time),
                category);
    }

    public String toCSVRow ()
    {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm").format(time) + "," +
                toCSVCell(content) + "," +
                Integer.toString(amount) + "," +
                toCSVCell(category) + "\r\n";
    }

    private String toCSVCell (String s)
    {
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r"))
            s = "\"" + s.replaceAll("\"", "\"\"") + "\"";
        return s;
    }

    public long insert ()
    {
        return new DBHelper(context).newEntry(content, amount, time, categoryId);
    }

    public long update (int id)
    {
        return new DBHelper(context).updateEntry(content, amount, time, categoryId, id);
    }

    public int delete ()
    {
        return new DBHelper(context).deleteEntry(id);
    }
}
