package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanEndsWorkingEvent extends AbstractSimEventDelegator<Human>{

	protected HumanEndsWorkingEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		Utils.log(human, "Finally its over..." + human.getName() + " stops working.");
		
		if(human.willWalk()){
			HumanWalksDirectlyHomeEvent e = new HumanWalksDirectlyHomeEvent(this.getModel(), "human walks home directly");
//			e.schedule(human, 0);
			m.getComponent().synchronisedAdvancedTime(0, e, human);
		} else {
			HumanWalksFromWorkToBusStopWorkEvent e = new HumanWalksFromWorkToBusStopWorkEvent(this.getModel(), "human walks to bus stop work from work");
//			e.schedule(human, 0);
			m.getComponent().synchronisedAdvancedTime(0, e, human);
		}
		
		
	}

}
