package org.b3studios.bible.slidingmenu;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.b3studios.bible.R;
import org.b3studios.bible.SplashScreen;
import org.b3studios.bible.slidingmenu.adapter.BookmarkListViewAdapter;

import java.util.ArrayList;

public class BookmarkFragment extends Fragment {

    private ListView bookmarkListView;

    public BookmarkFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);

        View rootView = inflater.inflate(R.layout.fragment_bookmark, container, false);

        bookmarkListView = (ListView) rootView.findViewById(R.id.bookmarkListView);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        updateBookmarkTextView();

        return rootView;
    }


    public boolean onOptionsItemSelected(MenuItem item) {

        return true;

    }

    private void updateBookmarkTextView() {
        new Thread(new Runnable() {
            public void run() {

                ArrayList<Spannable> highlights = SplashScreen.db.getAllHighlightsSpannable();

                setBookmarkTextView(highlights);
            }
        }).start();
    }

    @SuppressWarnings("ConstantConditions")
    public void setBookmarkTextView(final ArrayList<Spannable> highlights) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final BookmarkListViewAdapter adapter = new BookmarkListViewAdapter(getActivity(), highlights);

                bookmarkListView.setAdapter(adapter);

            }
        });
    }
}