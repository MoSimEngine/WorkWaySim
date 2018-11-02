package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
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
		
		TravelToNextEvent e = new TravelToNextEvent(getModel(), "Travels from Work to Home");
		if(HumanSimValues.FULL_SYNC) {
			m.getComponent().synchronisedAdvancedTime(0, e, human);
		} else {
			e.schedule(human, 0);
		}
		
//		
		
		
	}

}
