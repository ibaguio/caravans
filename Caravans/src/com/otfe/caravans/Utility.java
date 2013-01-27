package com.otfe.caravans;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import group.pals.android.lib.ui.lockpattern.LockPatternActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * collection of methods that are used by
 * multiple classes
 * mostly for UI reusing
 * @author Ivan Dominic Baguio
 */
public final class Utility {
	private static final String TAG = "Utility";
	
	/**
	 * Simple Callback interface
	 */
	public interface Callback{
		void doIt();
	}
	
	/**
	 * Simple Callback interface
	 */
	public interface PasskeyReturn{
		void setPasskey(String passkey);
	}
	
	/**
	 * Setups the Custom Input Password Dialog
	 * 
	 * @param context a reference to the activity that calls 
	 * this method, DO NOT USE getApplicationContext(), instead
	 * use 'this'
	 * @param pwOKCallback Callback that would receive the password
	 */
	public static void showGetPasswordDialog(final Activity context,final PasskeyReturn pwOKCallback){
		
		LayoutInflater li = LayoutInflater.from(context);
		View pwDialogView = li.inflate(R.layout.password_dialog, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setView(pwDialogView);
		
		final EditText password = (EditText) pwDialogView
				.findViewById(R.id.inp_password);
		final EditText password2 = (EditText) pwDialogView
				.findViewById(R.id.inp_password_2);
		final CheckBox showPass = (CheckBox) pwDialogView
				.findViewById(R.id.chk_show_password);
		
		/* toggle viewing of password */
		showPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int itype = (isChecked)? InputType.TYPE_CLASS_TEXT | 
						InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD: 
						InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
				password.setInputType(itype);
				password2.setInputType(itype);
			}
		});
		
		alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			    @Override
				public void onClick(DialogInterface dialog,int id) {
			    	String pass = password.getText().toString();
					String pas2 = password2.getText().toString();
					if (pass.equals(pas2)){
						pwOKCallback.setPasskey(pass);
						Utility.selectedInput(context,R.id.password_btn);
					}else{
						password.setText(""); password2.setText("");
						Toast.makeText(context, "Password does not match", 
								Toast.LENGTH_LONG).show();
						//Utility.selectedInput(EncryptSingleActivity.this,Constants.CLEAR_BTN);
					}
			    	;
			    }
			})
			.setNegativeButton("Cancel",
			  new DialogInterface.OnClickListener() {
			    @Override
				public void onClick(DialogInterface dialog,int id) {
			    	dialog.cancel();
			    }
			}).show();
	}

	/**
	 * Calls the File Browser external activity to have
	 * the user select/browse for a file or folder
	 * @param context Reference to the calling activity
	 * @param task the task code, either Constants.BROWSE_FILE or Constants.BROWSE_FOLDER
	 */
	public static void browseFile(Activity context, int task){
		Intent intent = new Intent(context,FileChooserActivity.class);
    	intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Constants.SDCARD));
    	
		if (task == Constants.BROWSE_FILE) // browse for files only (target file) 
			context.startActivityForResult(intent, Constants.TASK_GET_TARGET);
		else if (task == Constants.BROWSE_FOLDER){ // browse for destination folder
			intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
			context.startActivityForResult(intent, Constants.TASK_GET_DEST_FOLDER);
		}
	}
	
	/**
	 * Toggles which button on security_input.xml is selected
	 * @param context Reference to the calling activity
	 * @param btn_id
	 * @see res/layout/security_input.xml
	 */
	public static void selectedInput(Activity context, int btn_id){
		Drawable default_button = context.findViewById(R.id.default_btn).getBackground();
		Button patt_btn = (Button) context.findViewById(R.id.pattern_btn);
		Button pass_btn = (Button) context.findViewById(R.id.password_btn);
		
		patt_btn.setBackgroundDrawable(default_button);
		pass_btn.setBackgroundDrawable(default_button);
		
		if (btn_id != Constants.CLEAR_BTN){
			Button selected = (Button) context.findViewById(btn_id);
			selected.setBackgroundResource(R.drawable.selected_button);
		}
	}
	
	/**
	 * Creates and shows a Dialog that prompts the user to create
	 * the destination folder, then calls the callback method once
	 * the folder/s are created
	 *  
	 * @param context Reference to the calling activity
	 * @param folder the folder that will be created
	 * @param callback method to be called when successuful folder creation
	 * @return
	 */
	public static void promptCreateFolderDialog(final Activity context,
			final File folder, final Callback callback){
		
		DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
			        case DialogInterface.BUTTON_POSITIVE: 
			        	try {
			        		folder.mkdirs();
							Log.d(TAG, "Dest folder created");
							if (!folder.isDirectory() || !folder.exists())
								throw new IOException();
							else
								callback.doIt(); //call the callback method
							
						} catch (IOException e) {
							Toast.makeText(context, "Failed to create folder", 
								Toast.LENGTH_SHORT).show();
						}
			            break;
		        }
		    }
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Destination folder does not exist. Create it?")
			.setPositiveButton("Create", dialogListener)
		    .setNegativeButton("Cancel", dialogListener).show();
	}
	
	public static void showCreateLockPattern(Activity context){
		Intent intent = new Intent(context,LockPatternActivity.class);
		intent.putExtra(LockPatternActivity._Mode, LockPatternActivity.LPMode.CreatePattern);
		context.startActivityForResult(intent, Constants.TASK_MAKE_PATTERN);
	}
}