package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class WalkToBusStopAtHomeEvent extends AbstractSimEventDelegator<Human>{

	public WalkToBusStopAtHomeEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		human.walkToBusStopAtHome();
		Utils.log(human, human.getName() + " walks to home busstop:" + human.getHomeBusStop().getName() + ".  I don't like workdays ...");
		double walkToBusStopHomeDuration = human.HOME_TO_STATION.toSeconds().value();
		ArriveAtBusStopAtHomeEvent e = new ArriveAtBusStopAtHomeEvent(this.getModel(), "Arrive at BusStop Home");
//		e.schedule(human, walkToBusStopHomeDuration);
		m.getComponent().synchronisedAdvancedTime(walkToBusStopHomeDuration, e, human);
		
	}

}
