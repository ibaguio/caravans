package com.otfe.caravans;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DecryptSingle extends Activity{
	private final int GET_DECRYPT_TARGET = 0;
	private final int GET_DEST_FOLDER = 1;
	private String password;
	private File dest_folder;
	private File target;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.decrypt_single);
		Spinner spinner = (Spinner)findViewById(R.id.dec_algorithms);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.algorithms_with_auto,android.R.layout.simple_spinner_dropdown_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}
	
	public void onClick(View view){
		Intent intent;
		switch(view.getId()){
			case R.id.browse_for_decrypt_btn:
			case R.id.browse_for_dest_btn:
				intent = new Intent(this,FileBrowser.class);
		    	intent.putExtra(FileBrowser.FILEPATH, FileBrowser.EXTERNAL_STORAGE);
		    	if (view.getId()==R.id.browse_for_decrypt_btn)
		    		this.startActivityForResult(intent, GET_DECRYPT_TARGET);
		    	else if (view.getId()==R.id.browse_for_dest_btn){
		    		intent.putExtra(FileBrowser.GET_DIRECTORY, true);
		    		this.startActivityForResult(intent, GET_DEST_FOLDER);
		    	}
				break;
			case R.id.single_decrypt_btn:
				password = ((TextView)this.findViewById(R.id.decrypt_password)).getText().toString();
				Log.d("DecryptSingle","password: "+password);
				if (password.equals(""))
					Toast.makeText(getApplicationContext(), "Enter your password", Toast.LENGTH_SHORT).show();
				else{
					String algo = ((Spinner)findViewById(R.id.dec_algorithms)).getSelectedItem().toString();
					String algorithm = "";
					
					if (algo.equals("AES"))
						algorithm = OnTheFlyUtils.AES;
					else if (algo.equals("Two Fish"))
						algorithm = OnTheFlyUtils.TWO_FISH;
					else if (algo.equals("Serpent"))
						algorithm = OnTheFlyUtils.SERPENT;
					else if (algo.equals("Auto"))
						algorithm = verifyAlgorithm();
					
					OnTheFlyDecryptor otfd = new OnTheFlyDecryptor(password, 
							target.getAbsolutePath(), dest_folder.getAbsolutePath(), algorithm);
					if (otfd.correctPassword()){
						Toast.makeText(getApplicationContext(), "Password is correct", Toast.LENGTH_SHORT).show();
						intent = new Intent(this, DecryptionService.class);
						intent.putExtra(DecryptionService.SRC_FILEPATH, target.getAbsolutePath());
						intent.putExtra(DecryptionService.DEST_FILEPATH, dest_folder.getAbsolutePath());
						intent.putExtra(DecryptionService.PASSWORD, password);
						Log.d("DecryptSingle","Starting Service");
						startService(intent);
					}else
						Toast.makeText(getApplicationContext(), "Decryption Failed", Toast.LENGTH_SHORT).show();
				}
				break;
		}
	}
																																																			
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		switch(requestCode){
			case GET_DECRYPT_TARGET:
				target = new File(data.getStringExtra(FileBrowser.FILEPATH));
				if (!target.isFile())
					Log.d("DecryptSingle","Target received is not a valid TARGET FILE");
				else
					Log.d("DecryptSingle","Target received OK");
				TextView tv1 = (TextView)findViewById(R.id.file_to_decrypt);
				tv1.setText(target.getAbsolutePath());
				break;
			case GET_DEST_FOLDER:
				dest_folder = new File(data.getStringExtra(FileBrowser.FILEPATH));
				if (!target.isDirectory())
					Log.d("DecryptSingle","Dest Folder received is not a valid FOLDER");
				else
					Log.d("DecryptSingle","Dest Folder received OK");
				TextView tv2 = (TextView)findViewById(R.id.decrypt_dest);
				tv2.setText(dest_folder.getAbsolutePath());
				break;
		}
	}
	/**
	 * tries to find the encrypting algorithm
	 * that was used for to encrypt target
	 * @return
	 */
	private String verifyAlgorithm(){
		SQLiteDatabase db = this.openOrCreateDatabase(FileLogHelper.DATABASE_NAME,
				FileLogHelper.DATABASE_VERSION, null);
		String query = "SELECT algorithm FROM folderlogger WHERE foldername='"+getParentFolder()+"';";
		Log.d("DecryptSingle","Query: "+query);
		String algo="";
		try{
		Cursor c = db.rawQuery(query,null);
		c.moveToFirst();
		Log.d("","got cursor "+c.getCount());
		if (c.getCount()==1)
			algo = c.getString(0);
		}catch(Exception e){
			Log.d("DecryptSingle",""+e.getMessage());
		}
		Log.d("DecryptSingle","Algo: "+algo);
		return algo;
	}

}