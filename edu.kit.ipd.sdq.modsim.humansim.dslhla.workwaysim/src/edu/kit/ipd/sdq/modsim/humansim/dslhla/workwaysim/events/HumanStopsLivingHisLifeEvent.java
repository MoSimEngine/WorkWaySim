package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanStopsLivingHisLifeEvent extends AbstractSimEventDelegator<Human>{

	protected HumanStopsLivingHisLifeEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		// TODO Auto-generated method stub
		Utils.log(human, "Oh boy, time flies by... " + human.getName() + " stops living his life.");
		
		if(human.willWalk()){
			HumanWalksDirectlyToWorkEvent e = new HumanWalksDirectlyToWorkEvent(this.getModel(), "Human walks to work directly");
//			e.schedule(human, 0);
			m.getComponent().synchronisedAdvancedTime(0, e, human);
		} else {
		WalkToBusStopAtHomeEvent e = new WalkToBusStopAtHomeEvent(human.getModel(), "Human walks to bus stop at home");
//		e.schedule(human, 0);
		m.getComponent().synchronisedAdvancedTime(0, e, human);
		}
	}

}
