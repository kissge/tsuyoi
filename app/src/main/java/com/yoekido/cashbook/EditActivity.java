package com.yoekido.cashbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class EditActivity extends Activity
{
    static final String CSV_FILENAME = "tsuyoi.csv";
    int spinner_last;
    int updateId;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText contentText = (EditText) findViewById(R.id.editContent);
        final EditText amountText = (EditText) findViewById(R.id.editAmount);
        final CheckBox incomeBox = (CheckBox) findViewById(R.id.checkIncome);
        final Button dtPickerButton = (Button) findViewById(R.id.dtpicker);
        final Calendar calendar = Calendar.getInstance();
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);

        final Button entryButton = (Button) findViewById(R.id.insert);
        entryButton.setOnClickListener(new OnClickListener ()
        {
            @Override
            public void onClick (View v)
            {
                String content = contentText.getText().toString();
                String amount_raw = amountText.getText().toString();
                int amount;
                try
                {
                    amount = Integer.parseInt(amount_raw) * (incomeBox.isChecked() ? -1 : 1);
                }
                catch (Exception e)
                {
                    toast(R.string.error_invalid_amount);
                    return;
                }
                int category = spinner.getSelectedItemPosition();
                Entry entry = new Entry(calendar, content, amount, category, EditActivity.this);
                if (updateId == -1)
                {
                    entry.insert();
                    toast(R.string.message_saved);
                    contentText.setText("");
                    amountText.setText("");
                    calendar.setTime(Calendar.getInstance().getTime());
                    updateDtPickerButton(dtPickerButton, calendar);
                    incomeBox.setChecked(false);
                    spinner.setSelection(0);
                }
                else
                {
                    entry.update(updateId);
                    toast(R.string.message_saved);
                    Intent intent = new Intent();
                    intent.putExtra("time", calendar.getTimeInMillis());
                    intent.putExtra("content", content);
                    intent.putExtra("amount", amount);
                    intent.putExtra("categoryId", category);
                    intent.putExtra("category", (String) spinner.getSelectedItem());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        updateDtPickerButton(dtPickerButton, calendar);

        dtPickerButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LayoutInflater inflater = LayoutInflater.from(EditActivity.this);
                View view = inflater.inflate(R.layout.datetime_picker, null);
                final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
                final TimePicker timePicker = (TimePicker) view.findViewById(R.id.timePicker);

                datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

                new AlertDialog.Builder(EditActivity.this).setView(view)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                                                timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                                        updateDtPickerButton(dtPickerButton, calendar);
                                    }
                                })
                        .show();
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String cat: Category.getCategories(this)) adapter.add(cat);
        adapter.add(getString(R.string.menu_new_category));
        spinner_last = 0;
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected (AdapterView<?> parent, View view, int position, long id)
            {
                    if (position == adapter.getCount() - 1)
                    {
                        final EditText editText = new EditText(EditActivity.this);
                        editText.setSingleLine();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditActivity.this);
                        alertDialogBuilder.setTitle(R.string.dialog_new_category)
                                .setMessage(R.string.dialog_input_category_name)
                                .setView(editText)
                                .setPositiveButton(R.string.dialog_ok, null)
                                .setNegativeButton(R.string.dialog_cancel,
                                        new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog, int whichButton)
                                            {
                                                spinner.setSelection(spinner_last);
                                            }
                                        })
                                .create();
                        final AlertDialog alertDialog = alertDialogBuilder.show();
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                String input = editText.getText().toString();
                                if (input.length() == 0)
                                {
                                    toast(R.string.error_empty_category_name);
                                    spinner.setSelection(spinner_last);
                                }
                                else
                                {
                                    boolean duplicated = false;
                                    for (int i = 0; i < adapter.getCount(); i++)
                                        if (adapter.getItem(i).equals(input))
                                        {
                                            duplicated = true;
                                            break;
                                        }
                                    if (duplicated)
                                    {
                                        toast(R.string.error_duplicated);
                                        spinner.setSelection(spinner_last);
                                    }
                                    else
                                    {
                                        Category.add(input, EditActivity.this);
                                        spinner_last = adapter.getCount() - 1;
                                        adapter.insert(input, spinner_last);
                                        toast(R.string.message_category_added);
                                        alertDialog.dismiss();
                                    }
                                }
                            }
                        });
                    }
                    else
                        spinner_last = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        Intent intent = getIntent();
        if (intent.hasExtra("mode") && "update".equals(intent.getStringExtra("mode")))
        {
            updateId = intent.getIntExtra("id", 0);
            calendar.setTimeInMillis(intent.getLongExtra("time", Calendar.getInstance().getTimeInMillis()));
            updateDtPickerButton(dtPickerButton, calendar);
            entryButton.setText(getString(R.string.ui_update));
            contentText.setText(intent.getStringExtra("content"));
            amountText.setText(Integer.toString(intent.getIntExtra("amount", 0)));
            spinner.setSelection(intent.getIntExtra("category", 0));
        }
        else
            updateId = -1;
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        exportCSV();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        menu.add(0, 0, 0, getText(R.string.menu_csv_export)).setIcon(android.R.drawable.ic_menu_set_as);
        menu.add(0, 1, 0, getText(R.string.menu_view)).setIcon(android.R.drawable.ic_menu_view);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case 1:
                Intent intent = new Intent(EditActivity.this, ViewActivity.class);
                startActivity(intent);
                break;
            case 0:
                exportCSV();
        }
        return true;
    }

    public void exportCSV ()
    {
        String path = Environment.getExternalStorageDirectory().getPath() + "/cashbook";
        File exportDir = new File(path, "");
        if (!exportDir.exists())
            exportDir.mkdirs();

        try
        {
            PrintWriter writer = getPrintWriter(path + "/" + CSV_FILENAME);
            List<Entry> database = new DBHelper(this).getEntries(0, 0);
            for (Entry row: database)
                writer.write(row.toCSVRow());
            writer.close();
            toast(R.string.message_csv_exported);
        }
        catch (Exception e)
        {
            toast(R.string.error_write_sdcard);
        }
    }

    private PrintWriter getPrintWriter (String filename) throws IOException
    {
        OutputStream stream = new FileOutputStream(filename);
        return new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
    }

    private void toast (int id)
    {
        Toast.makeText(EditActivity.this, getText(id), Toast.LENGTH_LONG)
                .show();
    }

    private void updateDtPickerButton (Button btn, Calendar calendar)
    {
        btn.setText(new SimpleDateFormat("MM/dd E HH:mm").format(calendar.getTime()));
    }
}
