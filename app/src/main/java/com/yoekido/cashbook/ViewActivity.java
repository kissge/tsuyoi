package com.yoekido.cashbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewActivity extends Activity
{
    static final int LIMIT = 100;

    int offset = 0;
    List<Entry> list;
    ListView listView;
    boolean loadCompleted = false;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_database);

        list = new ArrayList<Entry>();

        final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}
            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3)
            {
                if (!loadCompleted && i3 == i + i2)
                    load();
            }
        });
        registerForContextMenu(listView);
    }

    Entry selectedItem;
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, view, menuInfo);
        AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;

        selectedItem = (Entry) listView.getItemAtPosition(adapterInfo.position);
        menu.setHeaderTitle(selectedItem.toString());
        menu.add(1, 0, 0, R.string.menu_edit);
        menu.add(1, 1, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item)
    {

        switch (item.getItemId())
        {
            case 0:
                Intent intent = new Intent(ViewActivity.this, EditActivity.class);
                intent.putExtra("mode", "update");
                intent.putExtra("id", selectedItem.id);
                intent.putExtra("time", selectedItem.time);
                intent.putExtra("content", selectedItem.content);
                intent.putExtra("amount", selectedItem.amount);
                intent.putExtra("category", selectedItem.categoryId);
                startActivityForResult(intent, 0);
                return true;
            case 1:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setTitle(selectedItem.toString());
                builder.setMessage(getString(R.string.dialog_delete));
                builder.setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                selectedItem.delete();
                                list.remove(selectedItem);
                                ((StableArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                                toast(R.string.message_deleted);
                            }
                        });
                builder.setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton) {}
                        });
                builder.create().show();
        }
        return false;
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0 && resultCode == RESULT_OK)
        {
            Bundle extras = intent.getExtras();
            selectedItem.time = extras.getLong("time");
            selectedItem.content = extras.getString("content");
            selectedItem.amount = extras.getInt("amount");
            selectedItem.categoryId = extras.getInt("categoryId");
            selectedItem.category = extras.getString("category");
            ((StableArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
    }

    protected void load ()
    {
        DBHelper dbHelper = new DBHelper(this);
        List<Entry> load = dbHelper.getEntries(offset++, LIMIT);
        if (load.isEmpty())
        {
            if (list.isEmpty())
                toast(R.string.error_no_data_to_load);
            loadCompleted = true;
        }
        else
        {
            list.addAll(load);
            ((StableArrayAdapter) listView.getAdapter()).addAll(load).notifyDataSetChanged();
        }
    }

    private void toast (int id)
    {
        Toast.makeText(ViewActivity.this, getText(id), Toast.LENGTH_LONG)
                .show();
    }

    private class StableArrayAdapter<T> extends ArrayAdapter<T>
    {
        int i = 0;
        HashMap<T, Integer> hashMap = new HashMap<T, Integer>();

        public StableArrayAdapter (Context context, int id, List<T> objects)
        {
            super(context, id, objects);
            for (T s: objects)
                hashMap.put(s, i++);
        }

        public StableArrayAdapter addAll (List<T> objects)
        {
            for (T s: objects)
                hashMap.put(s, i++);
            return this;
        }

        public void clear ()
        {
            hashMap.clear();
            i = 0;
        }

        @Override
        public long getItemId (int pos)
        {
            T item = getItem(pos);
            return hashMap.get(item);
        }

        @Override
        public boolean hasStableIds ()
        {
            return true;
        }
    }
}
