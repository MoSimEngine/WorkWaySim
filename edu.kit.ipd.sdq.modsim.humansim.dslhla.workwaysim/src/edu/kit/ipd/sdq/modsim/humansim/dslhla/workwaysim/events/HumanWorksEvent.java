package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class HumanWorksEvent extends AbstractSimEventDelegator<Human>{

	protected HumanWorksEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Human human) {
		WorkwayModel m = (WorkwayModel)this.getModel();
		
		Utils.log(human, human.getName() + " works and works.");
		double working = human.WORKTIME.toSeconds().value();
		HumanEndsWorkingEvent e = new HumanEndsWorkingEvent(this.getModel(), "Human stops working");
//		e.schedule(human, working);
		m.getComponent().synchronisedAdvancedTime(working, e, human);
		
		
	}

}
