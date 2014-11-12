package de.robotik.nao.communicator.core.revisions;

/**
 * Class to represent revision information.
 * @author hannes
 *
 */
public class ServerRevision {

	private String name = "";
	private int revision = -1;
	private String downloadUrl = "";
	
	/**
	 * Constructor
	 */
	public ServerRevision(){}
	
	/**
	 * Constructor
	 * @param aName			{@link String} name of revision.
	 * @param aRevision		{@link Integer} revision.
	 * @param aUrl			{@link String} url of file download.
	 */
	public ServerRevision(String aName, int aRevision, String aUrl) {
		name = aName;
		revision = aRevision;
		downloadUrl = aUrl;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the revision
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * @return the downloadUrl
	 */
	public String getDownloadUrl() {
		return downloadUrl;
	}
	
	public String toString(){
		return "Revision " + revision + ": " + name
						+ "\nURL: " + downloadUrl;
	}
	
}
