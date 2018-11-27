package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;

public class Position extends AbstractSimEntityDelegator{

	public static enum PositionType{
		QUEUE
	}
	
	private PositionType positionType;
	
	public Position(ISimulationModel model, String name, PositionType type) {
		super(model, name);
		this.positionType = type;
	}

	public PositionType getPositionType() {
		return positionType;
	}

}