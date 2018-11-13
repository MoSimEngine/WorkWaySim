package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanEntersBusEvent extends AbstractSimEventDelegator<Human>{

	public HumanEntersBusEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		human.setCollected(true);
		human.calculateWaitedTime();
		human.travellingToNext();
		human.humanIsCollected();
		Utils.log(human, human.getName() + " enters bus at " + human.getPosition().getName() );
		
		DrivingTimeoOutEvent e = new DrivingTimeoOutEvent(getModel(), "Driving Timeout Event");
		TimeAdvanceToken token = new TimeAdvanceToken(e, human, Duration.minutes(20).toSeconds().value());
		
		if(human.getTaToken() != null) {
			m.getTimelineSynchronizer().revokeToken(human.getTaToken());
		}
		
		m.getTimelineSynchronizer().putToken(token);
		return;
	}

}
