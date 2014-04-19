package com.example.myfirstapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText numberText;
	private EditText contentText;
	private TextView textview;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		
		textview=(TextView) this.findViewById(R.id.display);
		numberText=(EditText) this.findViewById(R.id.To);
		contentText=(EditText) this.findViewById(R.id.edit_message);
		Button button=(Button) this.findViewById(R.id.button_send);
		button.setOnClickListener(new ButtonClickListener());
	}

	public void sendMessage(View view){
	   	String message=contentText.getText().toString();
		if(textview!=null){
			textview.append("address£º ");
			textview.append(message.trim());
			textview.append("\n");
		} else {
			System.out.println("textview is null!");
		}
	}
	
	private final class ButtonClickListener implements View.OnClickListener{

		public void onClick(View v){
			String number=numberText.getText().toString();
			String content=contentText.getText().toString();
			SmsManager manager=SmsManager.getDefault();
			ArrayList<String> texts=manager.divideMessage(content);
			for(String text:texts){
				manager.sendTextMessage(number,null,text,null,null);
			}
			sendMessage(v);
			Toast.makeText(MainActivity.this,R.string.success,Toast.LENGTH_LONG).show();;
		}

	}




	/*public final static String EXTRA_MESSAGE="com.example.myfirstapp.MESSAGE";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	//called when the user clicks the send button
	public void sendMessage(View view){
		//do something in response to button
		Intent intent=new Intent(this, DisplayMessageActivity.class);
		EditText editText=(EditText) findViewById(R.id.edit_message);
		String message=editText.getText().toString();
		intent.putExtra(EXTRA_MESSAGE,message);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	/*public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}*/

}