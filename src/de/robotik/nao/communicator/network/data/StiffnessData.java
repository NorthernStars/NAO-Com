package de.robotik.nao.communicator.network.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Class storing data about joint stiffness
 * @author Hannes Eilers
 *
 */
public class StiffnessData {

	private Map<NAOJoints, Float> jointStiffness = new HashMap<NAOJoints, Float>();
	
	/**
	 * @param joint {@link NAOJoints}
	 * @return {@link Float} between 0.0 (no stiffness) and 1.0 (full stiffness) of the {@code joint}
	 */
	public float getStiffness(NAOJoints joint){
		return jointStiffness.get(joint);
	}
	
}
