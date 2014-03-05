package de.robotik.nao.communicator.core.widgets;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.network.NetworkServiceHandler;
import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RemoteDevice extends LinearLayout implements NetworkServiceHandler {

	private RemoteNAO nao;
	private TextView txtName;
	private ImageView imgLogo;
	
	public RemoteDevice(Context context, NsdServiceInfo serviceInfo) {
		super(context);
		nao = new RemoteNAO(serviceInfo);
		
		// set layout
		setOrientation(HORIZONTAL);
		setLayoutParams( new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) );
		
		txtName = new TextView(context);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		layoutParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.activity_padding);
		txtName.setLayoutParams(layoutParams);
		
		imgLogo = new ImageView(context);
		
		addView(imgLogo);
		addView(txtName);
		
	}
	
	/**
	 * Updates view
	 */
	public void updateView(){		
		String name = nao.getName();
		int imgRes = R.drawable.robot_on;
		if( name == null ){
			name = getResources().getString(R.string.net_unknown_device);
			imgRes = R.drawable.unknown_device;
		}
		
		txtName.setText( nao.getName() );
		imgLogo.setImageResource(imgRes);
	}

	/**
	 * @return Underlaying {@link RemoteNAO} object
	 */
	public RemoteNAO getNao() {
		return nao;
	}
	
	

	@Override
	public void addNetworkService(NsdServiceInfo service) {
		nao.addNetworkService(service);
		updateView();
	}

	@Override
	public void removeNetworkService(NsdServiceInfo service) {
		nao.removeNetworkService(service);
		updateView();
	}

}
