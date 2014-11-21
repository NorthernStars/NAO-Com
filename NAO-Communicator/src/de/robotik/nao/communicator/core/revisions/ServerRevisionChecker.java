package de.robotik.nao.communicator.core.revisions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.robotik.nao.communicator.MainActivity;

public class ServerRevisionChecker implements Runnable {

	private static final String releasesURL = "https://api.github.com/repos/NorthernStars/NAO-Communication-server/releases";
	
	@Override
	public void run() {
		
		HttpClient vHttpClient = new DefaultHttpClient();
		HttpResponse vHttpResponse;
		String vResponseString = null;
		
		// get data from url
		try {
			
			vHttpResponse = vHttpClient.execute( new HttpGet(releasesURL) );
			if( vHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK ){
				
				// get content of response
				ByteArrayOutputStream vOutStream = new ByteArrayOutputStream();
				vHttpResponse.getEntity().writeTo(vOutStream);
				vOutStream.close();
				vResponseString = vOutStream.toString();
				
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// convert response to JSON and get latest revision
		if( vResponseString != null ){
			ServerRevision vLatestRevision = null;
			for( ServerRevision vRevision : parseJsonData(vResponseString) ){
				if( vLatestRevision == null || vRevision.getRevision() > vLatestRevision.getRevision() ){
					vLatestRevision = vRevision;
				}
			}
			
			// set latest online revision
			MainActivity.getInstance().setOnlineRevision(vLatestRevision);
		}
		
	}
	
	private List<ServerRevision> parseJsonData(String data){
		
		List<ServerRevision> vRevisions = new ArrayList<ServerRevision>();
		JsonArray vJson = (new JsonParser()).parse(data).getAsJsonArray();
		
		// get all available revisions
		for( int nRevision=0; nRevision < vJson.size(); nRevision++ ){
			JsonObject vRelease = vJson.get(nRevision).getAsJsonObject();
			
			// get name
			String vName = vRelease.get("name").getAsString();
			
			// get revision
			long vRevision = -1;
			String vData = vRelease.get("created_at").getAsString();
			try {
				vRevision = (new SimpleDateFormat("yyyy-MM-dd'T'HH:ss:ss'Z'", Locale.US)).parse(vData).getTime();
			} catch (ParseException e1) {}
			
			// get assets
			String vUrl = null;
			JsonArray vAssets = vRelease.get("assets").getAsJsonArray();
			for( int nAsset=0; nAsset < vAssets.size(); nAsset++ ){
				
				// get url
				JsonObject vAsset = vAssets.get(nAsset).getAsJsonObject();
				String vAssetUrl = vAsset.get("browser_download_url").getAsString();
				
				if( vAssetUrl.matches(".*\\.tar\\.gz$") ){
					vUrl = vAssetUrl;
					break;
				}
				
			}
			
			// get prerelease
			boolean vPrerelease = vRelease.get("prerelease").getAsBoolean();
			
			// get body
			String vBody = vRelease.get("body").getAsString();
			
			// add new revision
			if( vName != null && vUrl != null && vRevision >= 0 && vBody != null ){
				vRevisions.add( new ServerRevision(vName, vRevision, vUrl, vPrerelease, vBody) );
			}
		}
		
		
		return vRevisions;
	}

}
