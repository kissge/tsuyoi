package com.yoekido.cashbook;

import android.content.Context;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yk on 14/02/21.
 */
public class Category
{
    static SparseArray<String> categories;

    static public void add (String name, Context context)
    {
        categories.put((int) new DBHelper(context).newCategory(name), name);
    }
    static public String fromId (int id, Context context)
    {
        String category;
        if (categories != null && (category = categories.get(id)) != null)
            return category;
        categories = new DBHelper(context).getCategories();
        return categories.get(id);
    }

    static public List<String> getCategories (Context context)
    {
        if (categories == null)
            categories = new DBHelper(context).getCategories();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < categories.size(); i++)
            list.add(categories.valueAt(i));
        return list;
    }
}
