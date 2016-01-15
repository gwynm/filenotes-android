package sbs20.filenotes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class EditActivity extends ThemedActivity {

	private Note note;

	private Typeface getTypeface() {
		String fontFace = this.getFilenotesApplication()
				.getPreferences()
				.getString(PreferenceSettingsActivity.KEY_FONTFACE, "monospace");

		if (fontFace.compareTo("monospace") == 0) {
			return Typeface.MONOSPACE;
		} else if (fontFace.compareTo("sansserif") == 0) {
			return Typeface.SANS_SERIF;
		} else if (fontFace.compareTo("serif") == 0) {
			return Typeface.SERIF;
		}

		return Typeface.MONOSPACE;
	}
	
	private float getTextSize() {
		SharedPreferences sharedPref = this.getFilenotesApplication().getPreferences();
		int fontSize = Integer.parseInt(sharedPref.getString(PreferenceSettingsActivity.KEY_FONTSIZE, "16"));
		return fontSize;
	}

    private void updateNote() {
        final EditText edit = (EditText) this.findViewById(R.id.note);
        this.note.setText(edit.getText().toString());
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_edit);

		// Keep a note of this
		final EditActivity activity = this;

		// Show the Up button in the action bar.
		setupActionBar();

		// Load the note
		this.note = Current.getSelectedNote();
		final EditText edit = (EditText) this.findViewById(R.id.note);
		edit.setText(this.note.getText());
		this.setTitle(this.note.getName());

		// Listen for changes so we can mark this as dirty
		edit.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence one, int a, int b, int c) {
                activity.updateNote();
				if (activity.note.isDirty()) {
					String title = activity.getTitle().toString();
					if (!title.startsWith("* ")) {
						activity.setTitle("* " + title);
					}
				}
			}

			// complete the interface
			public void afterTextChanged(Editable s) { }
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		});

		edit.setTypeface(this.getTypeface());
		edit.setTextSize(this.getTextSize());
	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				this.startClose();
                return true;

			case R.id.action_save:
				this.save();
				return true;

			case R.id.action_delete:
				this.delete();
				return true;

			case R.id.action_rename:
				this.rename();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	this.startClose();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	public void startClose() {

        this.updateNote();
		final EditActivity activity = this;

		if (this.note.isDirty()) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int result) {
					switch (result){
						case DialogInterface.BUTTON_POSITIVE:
							activity.save();
                            activity.finishClose();
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							activity.finishClose();
							break;

						case DialogInterface.BUTTON_NEUTRAL:
							break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want to save your changes?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener)
				.setNeutralButton("Cancel", dialogClickListener)
				.show();
		} else {
			// This is not dirty. Nothing to save. Just close
			this.finishClose();
		}
	}

	public void finishClose() {
		Current.setSelectedNote(null);
		NavUtils.navigateUpFromSameTask(this);
	}

	public void rename() {
		final EditActivity activity = this;
		final EditText editText = new EditText(this);
		boolean isRenamed = false;

		editText.setText(this.note.getName());

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int result) {
				switch (result){
					case DialogInterface.BUTTON_POSITIVE:
						// Yes button clicked
						boolean succeeded = activity.getFilenotesApplication()
								.getStorageManager()
								.renameNote(activity.note, editText.getText().toString());

                        if (succeeded) {
                            activity.setTitle(activity.note.getName());
                        } else {
							activity.getFilenotesApplication().toast("Rename failed: name already exists?");
						}
						break;

					case DialogInterface.BUTTON_NEUTRAL:
						// Cancel button clicked - don't do anything further
						break;
				}
			}
		};

		new AlertDialog.Builder(this)
				.setMessage("Rename")
				.setView(editText)
				.setPositiveButton("Yes", dialogClickListener)
				.setNeutralButton("Cancel", dialogClickListener)
				.show();
	}
	
	public void save() {
		EditText editText = (EditText) this.findViewById(R.id.note);
		String content = editText.getText().toString();
		this.note.setText(content);
		this.getFilenotesApplication().getStorageManager().writeToStorage(this.note);
        this.setTitle(this.note.getName());
	}
	
	public void delete() {
		this.getFilenotesApplication().getStorageManager().deleteNote(this.note);
		this.finishClose();
	}
}
