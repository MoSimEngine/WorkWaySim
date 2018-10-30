package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanArriveByBustBusStopHomeEvent extends AbstractSimEventDelegator<Human>{

	public HumanArriveByBustBusStopHomeEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		m.hasToKeepAlive = false;
		
		human.arriveAtBusStopHomeByDriving();
		human.setCollected(false);
		human.calculateDrivingTime();
		HumanWalkFromBusStopHomeToHomeEvent e = new HumanWalkFromBusStopHomeToHomeEvent(this.getModel(), "Human walks home");
		m.getComponent().synchronisedAdvancedTime(0, e, human);
		
	}

}
