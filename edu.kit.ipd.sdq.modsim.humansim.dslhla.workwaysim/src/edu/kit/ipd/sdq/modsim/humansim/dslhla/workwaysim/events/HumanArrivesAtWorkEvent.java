package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanArrivesAtWorkEvent extends AbstractSimEventDelegator<Human>{

	protected HumanArrivesAtWorkEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		if(human.willWalk()){
			human.arriveAtWorkDirectlyWalking();
		} else {
			human.arriveAtWorkBus();
		}
	
		Utils.log(human, human.getName() + " starts to work.");
		HumanWorksEvent e = new HumanWorksEvent(this.getModel(), "Human Works");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
	}

}
