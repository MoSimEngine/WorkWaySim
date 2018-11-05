package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanEntersBusEvent extends AbstractSimEventDelegator<Human>{

	public HumanEntersBusEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		// TODO Auto-generated method stub
		WorkwayModel m = (WorkwayModel)this.getModel();
//		Utils.log(human, "Human Enters Bus Event");
		human.setCollected(true);
		human.calculateWaitedTime();
		human.travellingToNext();
		human.humanIsCollected();
		Utils.log(human, human.getName() + " enters bus at " + human.getPosition().getName() );
//		Utils.log(human, "Human sits in bus, time for next waiting!");
		DrivingTimeoOutEvent e = new DrivingTimeoOutEvent(getModel(), "Driving Timeout Event");
		m.getComponent().synchronisedAdvancedTime(Duration.minutes(20).toSeconds().value(), e, human);
		return;
	}

}
