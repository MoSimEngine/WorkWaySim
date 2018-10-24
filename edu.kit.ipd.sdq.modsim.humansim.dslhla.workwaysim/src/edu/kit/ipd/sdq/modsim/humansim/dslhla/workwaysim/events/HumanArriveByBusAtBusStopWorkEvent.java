package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanArriveByBusAtBusStopWorkEvent extends AbstractSimEventDelegator<Human>{

	public HumanArriveByBusAtBusStopWorkEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
		Utils.log(human, "In ArrivingAtBS");
		
		m.hasToKeepAlive = false;
		
		human.arriveAtBusStopWorkByDriving();
		human.setCollected(false);
		human.calculateDrivingTime();
		HumanWalkFromBusStopToWorkEvent e = new HumanWalkFromBusStopToWorkEvent(this.getModel(), "Human walks from bus stop work to work");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
	}

}
