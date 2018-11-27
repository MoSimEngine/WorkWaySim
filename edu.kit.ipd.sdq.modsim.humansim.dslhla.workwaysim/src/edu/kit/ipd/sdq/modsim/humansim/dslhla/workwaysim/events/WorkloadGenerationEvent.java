package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Token;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public class WorkloadGenerationEvent extends AbstractSimEventDelegator<Token>{

	public WorkloadGenerationEvent(ISimulationModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void eventRoutine(Token arg0) {
		WorkwayModel m = (WorkwayModel) getModel();
		
		
		if( m.getTokensCount() < HumanSimValues.NUM_HUMANS) {
		m.generateToken();
		new WorkloadGenerationEvent(getModel(), "Workload Generation").schedule(arg0, HumanSimValues.interarrivalTime.toSeconds().value());
		}
	}

}
