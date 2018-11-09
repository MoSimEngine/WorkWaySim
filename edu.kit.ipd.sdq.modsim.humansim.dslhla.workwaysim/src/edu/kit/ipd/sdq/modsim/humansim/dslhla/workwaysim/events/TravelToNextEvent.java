package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class TravelToNextEvent extends AbstractSimEventDelegator<Human>{

	public TravelToNextEvent(ISimulationModel model, String name) {
		super(model, name);
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		PositionType posType = human.getPosition().getPositionType();
		PositionType destType = human.getDestination().getPositionType();
		Utils.log(human, "Travel to next event");
		Duration travelTime;
		String eventName = "";
		
		switch (posType) {
		case BUSSTOP:
			
			switch (destType) {
			case HOME:
				travelTime = human.HOME_TO_STATION.toSeconds();
				eventName = "Walk from station to home";
				human.walkToNext();
				break;
			case WORK: 
				travelTime = human.WORK_TO_STATION.toSeconds();
				eventName = "Walk from station to work";
				human.walkToNext();
				break;
			default:
				throw new IllegalStateException("No way from busStop to destination");
			}
			
			break;
			
		case HOME:
			
			switch(destType) {
			case BUSSTOP:
				travelTime = human.HOME_TO_STATION.toSeconds();
				eventName = "Walk from home to station";
				human.walkToNext();
				break;
				
			case WORK:
				travelTime = human.WALK_DIRECTLY.toSeconds();
				eventName = "Walk from home directly to work";
				human.walkToNext();
				break;
			default:
				throw new IllegalStateException("No way from home to destination");
			}
		
			break;
		
		case WORK: 
			
			switch(destType) {
			case BUSSTOP:
				travelTime = human.HOME_TO_STATION.toSeconds();
				eventName = "Walk from home to station";
				human.walkToNext();
				break;
				
			case HOME:
				travelTime = human.WALK_DIRECTLY.toSeconds();
				eventName = "Walk from work directly to home";
				human.walkToNext();
				break;
			default:
				throw new IllegalStateException("No way from work to destination");
			}
			
			break;
			
		default:
			throw new IllegalStateException("No valid position  to travel to");
		}
		
		ArriveAtNextEvent e = new ArriveAtNextEvent(getModel(), eventName);
		TimeAdvanceToken token = new TimeAdvanceToken(e, human, travelTime.toSeconds().value());
		m.getTimelineSynchronizer().putToken(token);
	}
}
