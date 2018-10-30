package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;

public class Position extends AbstractSimEntityDelegator{

	public static enum PositionType{
		BUSSTOP,
		HOME,
		WORK
	}
	
	private PositionType positionType;
	
	protected Position(ISimulationModel model, String name, PositionType type) {
		super(model, name);
		this.positionType = type;
	}

	public PositionType getPositionType() {
		return positionType;
	}

//	private void setPositionType(PositionType posType) {
//		this.positionType = posType;
//	}
	
	
	
}