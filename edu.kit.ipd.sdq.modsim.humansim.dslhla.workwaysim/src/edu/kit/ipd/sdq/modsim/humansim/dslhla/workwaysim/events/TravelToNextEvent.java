package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;

public class TravelToNextEvent extends AbstractSimEventDelegator<Human>{

	protected TravelToNextEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		PositionType posType = human.getPosition().getPositionType();
		PositionType destType = human.getDestination().getPositionType();
		
		switch (posType) {
		case BUSSTOP:
			
			if(destType.equals(PositionType.HOME) || destType.equals(PositionType.WORK))
			
			break;

		default:
			break;
		}
		
	}

}
