package com.example.androidgpsexample;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

public class MainActivity extends FragmentActivity implements
		android.location.LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private LocationManager lm;
	private FileUtility myFile;
	private SupportMapFragment mapFragment;
    private long minUpdatePeriod = 10000; // 10 seconds
    private float minDistance = 10.0f; // 10 meters
    private ListView locationList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				
		// flag the screen to stay turned on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		locationList = (ListView) findViewById(R.id.posts_listview);

		// request updates here when location changes and other criteria met
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minUpdatePeriod, minDistance, 
													(android.location.LocationListener) this);

		// set up the map fragment
		mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
		
		// enable the current location "blue dot"
		mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(true);

		// center the map on my current location
		mapFragment.getMap().setMyLocationEnabled(true);
		
		// center map on engineering building
		CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(53.2837721,-9.0642949));
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

		GoogleMap gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
		gMap.moveCamera(center);
		gMap.animateCamera(zoom);

		// create a new file using the utility class: "FileUtility"
		myFile = new FileUtility();
		String fileName = "test_file_gps.txt";
		
		// create the file if it does not exist
		myFile.createFile(getApplicationContext(), fileName);
		
		String fileContents = myFile.readAll();
		String[] locations = fileContents.split("\n");
		
		locationList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, locations));
		
		if (fileContents.length() > 0) {
			for (int i = 0; i < locations.length; i++) {
				
				// add previously saved locations to map here
				String[] coordinates = locations[i].split(";");
				
				double latitude = Double.parseDouble(coordinates[0]); 
				double longtitude = Double.parseDouble(coordinates[1]);
				
				gMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longtitude)).title("I've been here."));
			}
		}
	}
	
	// called when the listener is notified with a location update from the GPS
	public void onLocationChanged(Location location) {
		
		// add marker of my current location to map here
		GoogleMap gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
		gMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), 
														    location.getLongitude())).title("I am here!"));
		
		// add reference to my current location to text file here
		String str = null;
        NumberFormat formatter = new DecimalFormat("##0.00000");
        
        String fLat = formatter.format(location.getLatitude());
        String fLon = formatter.format(location.getLongitude());
        
        str = fLat + ";" + fLon;
        
        Log.d("GPS", str);
        myFile.writeLine(str);
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