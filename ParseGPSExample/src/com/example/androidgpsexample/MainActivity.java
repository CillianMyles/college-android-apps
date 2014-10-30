package com.example.androidgpsexample;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends FragmentActivity implements
		android.location.LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private LocationManager locationManager;
	private SupportMapFragment mapFragment;
    private long minUpdatePeriod = 10000; // 10 seconds
    private float minDistance = 10.0f; // 10 meters
    private ListView locationList;
    private String android_id;
    private ParseUser user;
	private double signalDb;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private ArrayAdapter<String> locationListAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Parse.initialize(this, "RqMk771nkjsYFgnvvUKcWVRPL7KclL1GBgnay08z", "v32L0jE6rYveWl8ONGNrXakPPYHt0a9CI1BDd0ef");
		android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
		
		if (ParseUser.getCurrentUser() == null) {
			user = new ParseUser();
			user.setUsername(android_id);
			user.setPassword(android_id);
			
			try {
				user.signUp();
			} catch (com.parse.ParseException e) {
				e.printStackTrace();
				try {
					ParseUser.logIn(android_id, android_id);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		} else {
			user = ParseUser.getCurrentUser();
		}
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				
		// flag the screen to stay turned on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		locationList = (ListView) findViewById(R.id.posts_listview);

		// request updates here when location changes and other criteria met
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minUpdatePeriod, minDistance, 
													(android.location.LocationListener) this);
		
		telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		phoneStateSetup();


		// set up the map fragment
		mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
		
		// enable the current location "blue dot"
		mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(true);

		// center the map on my current location
		mapFragment.getMap().setMyLocationEnabled(true);
		
		// center map on engineering building
		CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(53.2837721,-9.0642949));
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

		final GoogleMap gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
		gMap.moveCamera(center);
		gMap.animateCamera(zoom);
		
		// create array to store the queried values
		final ArrayList<String> previousLocationsList = new ArrayList<String>();
		locationListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, previousLocationsList);
		locationList.setAdapter(locationListAdapter);

		// query locations from parse for this user
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
		query.whereEqualTo("user", user);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> locations, com.parse.ParseException e) {
				if (locations != null) { 
					for (ParseObject location : locations) {
						double latitude = location.getDouble("latitude");
						double longtitude = location.getDouble("longtitude");
					
						String locationStr = null;
						NumberFormat formatter = new DecimalFormat("##0.00000");
			        
						String formattedLatitude = formatter.format(latitude);
						String formattedLongtitude = formatter.format(longtitude);
			        
						locationStr = "Lat: " + formattedLatitude + " ; Lon: " + formattedLongtitude;						
						previousLocationsList.add(locationStr);
						
						locationListAdapter.notifyDataSetChanged();					
						gMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longtitude)).title("I've been here."));
					}
				}
			}
		});	
	}
	
	// called when the listener is notified with a location update from the GPS
	public void onLocationChanged(Location location) {
		
		// add location to parse
		ParseObject post = new ParseObject("Location");
		post.put("latitude", location.getLatitude());
		post.put("longtitude", location.getLongitude());
		post.put("signal", signalDb);
		post.put("user", user);
		post.saveInBackground();
		
		// add marker of my current location to map here
		GoogleMap gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
		gMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), 
														    location.getLongitude())).title("I am here!"));
	}
	
	private void phoneStateSetup(){
		
		phoneStateListener = new PhoneStateListener() {
			
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				signalDb = signalStrength.getEvdoDbm();
			}
		};
		
		try{
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// called when the GPS provider is turned off (user turning off the GPS on the phone)
	public void onProviderDisabled(String provider) {
		Log.e("GPS", "provider disabled " + provider);
	}

	// called when the GPS provider is turned on (user turning on the GPS on the phone)
	public void onProviderEnabled(String provider) {
		Log.e("GPS", "provider enabled " + provider);
	}

	// called when the status of the GPS provider changes
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.e("GPS", "status changed to " + provider + " [" + status + "]");
	}

	public void onConnectionFailed(ConnectionResult arg0) {}
	public void onConnected(Bundle arg0) {}
	public void onDisconnected() {}
}