
package org.clc.android.app.redbox;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.actionbarcompat.ActionBarActivity;

import org.clc.android.app.redbox.data.DataManager;
import org.clc.android.app.redbox.data.SuggestionsManager.OnSuggestionChangedListener;

public class RedBoxSuggestionActivity extends ActionBarActivity implements
        OnSuggestionChangedListener {
    public static final String SUGGESTION_EXTRA = "suggestion_extra";
    private static final String DEFAULT_SUGGESTIONS_LOADED = "default_suggestions_loaded";
    private ListView mListView;
    private SuggestionAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_redboxsuggestionactivity);

        final SharedPreferences prefs = getSharedPreferences(DEFAULT_SUGGESTIONS_LOADED,
                MODE_PRIVATE);
        boolean loaded = prefs.getBoolean(DEFAULT_SUGGESTIONS_LOADED, false);
        if (!loaded) {
            String[] defaultSuggestions = getResources().getStringArray(R.array.auto_sms_preset);
            for (String suggestion : defaultSuggestions) {
                DataManager.getInstance().addSuggestion(suggestion);
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(DEFAULT_SUGGESTIONS_LOADED, true);
            editor.commit();
        }

        DataManager.getInstance().setOnSuggestionsChangedListener(this);

        mListView = (ListView) findViewById(R.id.suggestionsListView);
        mAdapter = new SuggestionAdapter();
        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.redbox_suggestion_activity_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_suggestion_add:
                showSuggestionEditDialog("", -1);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSuggestionEditDialog(final String currentSuggestion, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setText(currentSuggestion);
        if (position == -1) {
            builder.setTitle(R.string.suggestion_add_dialog_title);
        } else {
            builder.setTitle(R.string.suggestion_edit_dialog_title);
        }
        builder.setView(input);

        builder.setPositiveButton(R.string.dialog_positive_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String text = input.getText().toString();
                        if (text.length() == 0) {
                            Toast.makeText(RedBoxSuggestionActivity.this,
                                    R.string.warning_suggestion_should_not_empty,
                                    Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            return;
                        }
                        if (position == -1) {
                            DataManager.getInstance().addSuggestion(text);
                        } else {
                            DataManager.getInstance().updateSuggestion(position, text);
                        }
                    }
                });
        builder.setNegativeButton(R.string.dialog_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    @Override
    public void onSuggestionChanged() {
        mAdapter.notifyDataSetChanged();
    }

    private class SuggestionAdapter extends BaseAdapter {

        View.OnClickListener mEditButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SuggestionViewHolder holder = getViewHolderFromParent(v);
                final int position = holder.mPosition;
                showSuggestionEditDialog(DataManager.getInstance().getSuggestion(position),
                        position);
            }
        };
        View.OnClickListener mDeleteButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SuggestionViewHolder holder = getViewHolderFromParent(v);
                DataManager.getInstance().deleteSuggestion(holder.mPosition);
            }
        };
        View.OnClickListener mSuggestionClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SuggestionViewHolder holder = getViewHolderFromParent(v);
                final String suggestion = DataManager.getInstance().getSuggestion(holder.mPosition);
                DataManager.getInstance().updateSuggestionPriority(holder.mPosition);
                Intent intent = new Intent();
                intent.putExtra(SUGGESTION_EXTRA, suggestion);
                setResult(RESULT_OK, intent);
                finish();
            }
        };

        private SuggestionViewHolder getViewHolderFromParent(View v) {
            final View parent = (View) v.getParent();
            return (SuggestionViewHolder) parent.getTag();
        }

        @Override
        public int getCount() {
            return DataManager.getInstance().getSuggestionsCount();
        }

        @Override
        public Object getItem(int position) {
            return DataManager.getInstance().getSuggestion(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SuggestionViewHolder holder = null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.suggestion_list, null, false);
                final TextView suggestionTextView = (TextView) convertView
                        .findViewById(R.id.suggestionTextView);
                final ImageButton editButton = (ImageButton) convertView
                        .findViewById(R.id.suggestionEditImageButton);
                final ImageButton deleteButton = (ImageButton) convertView
                        .findViewById(R.id.suggestionDeleteButton);

                editButton.setOnClickListener(mEditButtonClickListener);
                deleteButton.setOnClickListener(mDeleteButtonClickListener);
                suggestionTextView.setOnClickListener(mSuggestionClickListener);

                holder = new SuggestionViewHolder(suggestionTextView);
                convertView.setTag(holder);
            } else {
                holder = (SuggestionViewHolder) convertView.getTag();
            }
            String suggestion = DataManager.getInstance().getSuggestion(position);
            holder.mSuggestionTextView.setText(suggestion);
            holder.mPosition = position;

            return convertView;
        }

        private class SuggestionViewHolder {
            public int mPosition = -1;
            public TextView mSuggestionTextView = null;

            public SuggestionViewHolder(TextView suggestionTextView) {
                mSuggestionTextView = suggestionTextView;
            }
        }
    }
}
