package org.b3studios.bible.slidingmenu.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.b3studios.bible.R;
import org.b3studios.bible.SplashScreen;
import org.b3studios.bible.helper.ThemeHelper;
import org.b3studios.bible.model.Highlight;
import org.b3studios.bible.slidingmenu.BibleFragment;

import java.util.ArrayList;

public class MainListViewAdapter extends ArrayAdapter<Spannable> {
    private final Context context;
    LayoutInflater inflater;
    private final ArrayList<Spannable> values;
    private SparseBooleanArray mSelectedItemsIds;

    public MainListViewAdapter(Context context, ArrayList<Spannable> values) {
        super(context, R.xml.main_listview_item, values);
        mSelectedItemsIds = new SparseBooleanArray();
        this.context = context;
        this.values = values;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.xml.main_listview_item, parent, false);
        final TextView textView;
        if (rowView != null) {
            textView = (TextView) rowView.findViewById(R.id.textview_item);
            textView.setText(values.get(position));
            textView.setTextSize(BibleFragment.settings.getMainViewTextSize());

            Highlight hl = new Highlight();

            hl.setBook(BibleFragment.settings.getCurrentBook());
            hl.setChapter(BibleFragment.settings.getCurrentChapter());
            hl.setVerse(position);

            String t = textView.getText().toString();
            Spannable spanRange = new SpannableString(t);

            if (SplashScreen.db.isHighlight(hl)) {


                // Add Bold text to the verse
                spanRange.setSpan(new StyleSpan(Typeface.BOLD), 0, t.indexOf(" "),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                BackgroundColorSpan backColour = new BackgroundColorSpan(Color.YELLOW);

                spanRange.setSpan(backColour, t.indexOf(" ") + 1, t.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (BibleFragment.settings.nightMode) {
                    ForegroundColorSpan foreColourText = new ForegroundColorSpan(Color.BLACK);
                    ForegroundColorSpan foreColourVerse = new ForegroundColorSpan(Color.WHITE);

                    spanRange.setSpan(foreColourVerse, 0, t.indexOf(" "),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanRange.setSpan(foreColourText, t.indexOf(" ") + 1, t.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                textView.setText(spanRange);

            }

            // set theme color for textView
            ThemeHelper themeHelper = new ThemeHelper(context);

            TypedArray ta = context.obtainStyledAttributes(themeHelper.getmTheme(), themeHelper.attrs);

            int textColor = ta.getColor(themeHelper.TEXT_COLOR, Color.BLACK);

            textView.setTextColor(textColor);

            textView.setTypeface(BibleFragment.settings.getTypeface());

        }

        return rowView;
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, value);
        } else {
            mSelectedItemsIds.delete(position);
        }

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {

        return mSelectedItemsIds;
    }

    public void highlight(int i) {
        Highlight hl = new Highlight();

        hl.setBook(BibleFragment.settings.getCurrentBook());
        hl.setChapter(BibleFragment.settings.getCurrentChapter());
        hl.setVerse(i);

        Highlight highlightToUpdate = SplashScreen.db.getHighlight(hl);

        if (highlightToUpdate != null) {
            highlightToUpdate.setHighlight(highlightToUpdate.getHighlight() == 1 ? 0 : 1);
            SplashScreen.db.updateHighlight(highlightToUpdate);
        } else {
            hl.setHighlight(1);
            SplashScreen.db.addHighlight(hl);
        }
    }

    public String getSelectedText() {

        StringBuilder textToShare = new StringBuilder();
        ArrayList<String> verses = new ArrayList<String>();

        int len = BibleFragment.mainListView.getCount();
        SparseBooleanArray checked = BibleFragment.mainListView.getCheckedItemPositions();

        for (int i = 0; i < len; i++) {

            if (checked.get(i)) {

                String text = BibleFragment.mainListView.getItemAtPosition(i).toString();

                String[] split = text.split(" ");

                verses.add(split[0]);

                textToShare.append(text).append("\n");
            }


        }

        textToShare.append("(").
                append(BibleFragment.settings.getCurrentBook()).
                append(" ").append(BibleFragment.settings.getCurrentChapter()).append(":").
                append(simplifyVerses(verses)).append(")");

        return textToShare.toString();
    }

    private String simplifyVerses(ArrayList<String> verses) {
        StringBuilder verseFinal = new StringBuilder();

        for (int i = 0; i < verses.size(); i++) {

            verseFinal.append(verses.get(i)).append(",");
        }

        return verseFinal.toString().substring(0, verseFinal.toString().length() - 1);
    }
}