package com.example.androidfileexample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	// onCreate method is called only when this activity is created, see activity life cycle diagram for details
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//must include super.onCreate so that any parent classes get a call to onCreate
		super.onCreate(savedInstanceState);

		//set the view to be "activity_main" as in the res/layout folder
		setContentView(R.layout.activity_main);
		
		//get a reference to the textview in the view, this is done by referring to the textview's id: outputText
		TextView t = (TextView) findViewById(R.id.outputText);
		//overwrite the text in the textview
		t.setText("This is the output from the Android File Example App\n");
		 
		//determine a name for the file we are going to write
		String fileName = "test_file.txt"; 

		//create a new file using the utility class: "FileUtility"
		FileUtility myFileUtil = new FileUtility();
		
		myFileUtil.createFile(this, fileName);
		
		myFileUtil.writeLine("This is line 1.");
		myFileUtil.writeLine("And here is line number 2.");
		t.append(myFileUtil.readAll());

		Log.i("INFO", "Android File Example Main Activity Completed");
	}
}
