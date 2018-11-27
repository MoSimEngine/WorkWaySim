package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Queue;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Token;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class ArriveAtNextEvent extends AbstractSimEventDelegator<Token>{

	protected ArriveAtNextEvent(ISimulationModel model, String name) {
		super(model, name);
	
	}

	@Override
	public void eventRoutine(Token human) {
		human.nextPosition();
		PositionType posType = human.getPosition().getPositionType();
		PositionType destType = human.getDestination().getPositionType();
//		Utils.log(human, "Arrives at " + human.getPosition().getName());
		
		switch (posType) {
		case QUEUE:
			destType = human.getDestination().getPositionType();
			if(destType.equals(PositionType.QUEUE)) {
				if(human.getDirection() > 0) {
					RegisterAtBusStopEvent e = new RegisterAtBusStopEvent(getModel(), "Register at Queue");
					e.schedule(human, 0);
					break;
				} else {
					TravelToNextEvent e = new TravelToNextEvent(getModel(), "Travel to next position");
					e.schedule(human, 0);
					break;            
				}
			}
		default:
			Utils.log(human, "Arrived at no known position");
			break;
		}
		
		
	}
	
}
