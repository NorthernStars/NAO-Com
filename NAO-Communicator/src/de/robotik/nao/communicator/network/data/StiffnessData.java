package de.robotik.nao.communicator.network.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Class storing data about joint stiffness
 * @author Hannes Eilers
 *
 */
public class StiffnessData {

	private boolean leftHandOpen = false;
	private boolean rightHandOpen = false;
	private Map<NAOJoints, Float> jointStiffness = new HashMap<NAOJoints, Float>();
	
	/**
	 * @param joint {@link NAOJoints}
	 * @return {@link Float} between 0.0 (no stiffness) and 1.0 (full stiffness) of the {@code joint}
	 */
	public float getStiffness(NAOJoints joint){
		if( jointStiffness.containsKey(joint) ){
			return jointStiffness.get(joint);
		}
		
		return 0.0f;
	}
	
	public String toString(){
		String ret = "";
		for( NAOJoints joint : jointStiffness.keySet() ){
			ret += "\t" + joint + ":" + jointStiffness.get(joint) + "\n";
		}
		return ret;
	}

	/**
	 * @return {@link Map} of {@link NAOJoints} and their stiffnesst values as {@link Float}.
	 */
	public Map<NAOJoints, Float> getJointStiffness() {
		return jointStiffness;
	}

	/**
	 * @return	{@code true} if NAO left hand is opened
	 */
	public boolean isLeftHandOpen() {
		return leftHandOpen;
	}

	/**
	 * @return	{@code true} if NAO right hand is opened
	 */
	public boolean isRightHandOpen() {
		return rightHandOpen;
	}
	
}
