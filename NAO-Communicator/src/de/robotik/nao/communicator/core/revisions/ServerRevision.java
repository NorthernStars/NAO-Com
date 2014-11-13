package de.robotik.nao.communicator.core.revisions;

/**
 * Class to represent revision information.
 * @author hannes
 *
 */
public class ServerRevision {

	private String name = "";
	private long revision = -1;
	private String downloadUrl = "";
	private boolean prerelease = true;
	private String body = "";
	
	/**
	 * Constructor
	 */
	public ServerRevision(){}
	
	/**
	 * Constructor
	 * @param aName			{@link String} name of revision.
	 * @param aRevision		{@link Integer} revision.
	 * @param aUrl			{@link String} url of file download.
	 * @param aPrerelease	{@link Boolean} if release is a pre-release {@code true}, {@code false} otherwise.
	 * @param aBody			{@link String} of release body text.
	 */
	public ServerRevision(String aName, long aRevision, String aUrl, boolean aPrerelease, String aBody) {
		name = aName;
		revision = aRevision;
		downloadUrl = aUrl;
		prerelease = aPrerelease;
		body = aBody;
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
	public long getRevision() {
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

	/**
	 * @return the prerelease
	 */
	public boolean isPrerelease() {
		return prerelease;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}
	
}
