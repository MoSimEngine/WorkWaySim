package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class ArriveAtNextEvent extends AbstractSimEventDelegator<Human>{

	protected ArriveAtNextEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		
		Position currPos = human.nextPosition();
		PositionType posType = currPos.getPositionType();
		PositionType destType = human.getDestination().getPositionType();
		
		switch (posType) {
		case BUSSTOP:
			
			if(destType.equals(PositionType.HOME)) {
				
			} else if (destType.equals(PositionType.WORK) ) {
			
			} else if (destType.equals(PositionType.BUSSTOP)) {
				RegisterAtBusStopEvent e = new RegisterAtBusStopEvent(getModel(), "Register at BusStop");
				e.schedule(human, 0);
			} else {
				Utils.log(human, "Destination Type not known");
			}
			
			break;
			
			// Now At Home 
		case HOME:
			
			Utils.log(human, human.getName() + " arrives at home. Afterwork Party!");
			HumanLivingHisLifeEvent e = new HumanLivingHisLifeEvent(human.getModel(), "Human is living his life");
			e.schedule(human, 0);
			
			break;
			
		case WORK:
			
			Utils.log(human, human.getName() + " arrives at work.");
			
			
			break;

		default:
			break;
		}
		
		
	}
	
}
