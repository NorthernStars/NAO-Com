package de.robotik.nao.communicator.core.widgets;

import de.northernstars.naocom.R;
import de.robotik.nao.communicator.core.RemoteNAO;
import de.robotik.nao.communicator.network.hotspot.ClientScanResult;
import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RemoteDevice extends LinearLayout {

	private RemoteNAO nao;
	private TextView txtName;
	private ImageView imgLogo;
	
	public RemoteDevice(Context context, ClientScanResult client) {
		super(context);
		nao = new RemoteNAO(client);
		
		// set layout
		setOrientation(HORIZONTAL);
		setLayoutParams( new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) );
		
		txtName = new TextView(context);
		txtName.setText( client.getIpAddr() );
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		layoutParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.activity_padding);
		txtName.setLayoutParams(layoutParams);
		
		imgLogo = new ImageView(context);
		imgLogo.setImageResource(R.drawable.robot_on);
		
		addView(imgLogo);
		addView(txtName);
		
	}

	/**
	 * @return Underlaying {@link RemoteNAO} object
	 */
	public RemoteNAO getNao() {
		return nao;
	}

}
