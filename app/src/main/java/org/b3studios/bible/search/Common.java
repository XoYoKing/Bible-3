package org.b3studios.bible.search;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.b3studios.bible.search.adapter.SearchResultAdapter;
import org.b3studios.bible.helper.DatabaseHelper;
import org.b3studios.bible.slidingmenu.BibleFragment;

import java.util.ArrayList;

public class Common {

    private final int RESULT_TYPE;
    private Activity activity;
    private String query;
    private int id;

    public Common(Activity activity, String query, int RESULT_TYPE, int id) {

        this.activity = activity;
        this.query = query;
        this.RESULT_TYPE = RESULT_TYPE;
        this.id = id;
    }

    public void populateListView() {

        new Thread(new Runnable() {

            public void run() {

                ArrayList<Spannable> searchResult = new ArrayList<Spannable>();
                ArrayList<String> verseCallback = new ArrayList<String>();

                DatabaseHelper db = new DatabaseHelper(activity);
                Cursor cursor;

                if (RESULT_TYPE == 4) {
                    cursor = db.customQuery(RESULT_TYPE, query, BibleFragment.settings.getCurrentBook());
                } else {
                    cursor = db.customQuery(RESULT_TYPE, query, "");
                }

                if (cursor.moveToFirst()) {
                    do {

                        String passage = cursor.getString(3);
                        int startSpan, endSpan = 0;

                        String verse = cursor.getString(0) + " " + cursor.getString(1) + ":" + cursor.getString(2) + " ";

                        Spannable spanRange = new SpannableString(verse + passage);

                        while (true) {
                            startSpan = (verse + passage).toLowerCase().indexOf(query, endSpan);
                            BackgroundColorSpan backColour = new BackgroundColorSpan(Color.YELLOW);

                            // Need a NEW span object every loop, else it just moves the span
                            if (startSpan < 0)
                                break;
                            endSpan = startSpan + query.length();
                            spanRange.setSpan(backColour, startSpan, endSpan,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        // Add Bold text to the verse
                        spanRange.setSpan(new StyleSpan(Typeface.BOLD), 0, verse.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        searchResult.add(spanRange);
                        verseCallback.add(cursor.getString(0) + "-" + cursor.getString(1) + "-" + cursor.getString(2));

                    } while (cursor.moveToNext());
                } else {
                    searchResult.add(new SpannableString("No search result found for `" + query + "'"));
                    verseCallback.add("No search result found for `" + query + "'");
                }

                cursor.close();

                refreshList(searchResult, verseCallback);
            }

        }).start();
    }

    private void refreshList(final ArrayList<Spannable> searchResult, final ArrayList<String> verseCallback) {

        Runnable runnable = new Runnable() {
            public void run() {

                final ListView myListView = (ListView) activity.findViewById(id);

                SearchResultAdapter arrayListViewAdapter = new SearchResultAdapter(activity, searchResult);

                if (myListView != null) {
                    myListView.setAdapter(arrayListViewAdapter);

                    myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                            final String[] verse = verseCallback.get(position).split("-");

                            if (verse.length == 3) {

                                BibleFragment.settings.setCurrentBook(verse[0]);
                                BibleFragment.settings.setCurrentChapter(Integer.parseInt(verse[1]));
                                BibleFragment.settings.position = Integer.parseInt(verse[2]);

                                BibleFragment.bookTextView.setText(BibleFragment.settings.getCurrentBook() + " " + BibleFragment.settings.getCurrentChapter() + " \u25BC");

                                SharedPreferences settings = activity.getSharedPreferences("UserBibleInfo", 0);

                                SharedPreferences.Editor editor = settings.edit();

                                editor.putInt("position", Integer.parseInt(verse[2]));

                                editor.commit();

                                activity.finish();

                                Toast.makeText(activity, verse[0] + " " + verse[1] + ":" + verse[2], Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                }
                synchronized (this) {
                    this.notify();
                }
            }
        };
//
        startOnUiAndWait(runnable);
    }

    /**
     * Start runnable on UI thread and wait until finished
     */
    public void startOnUiAndWait(Runnable runnable) {
        synchronized (runnable) {
            // Execute code on UI thread
            activity.runOnUiThread(runnable);

            // Wait until runnable finished
            try {
                runnable.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}