package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;

public class TimeAdvanceToken extends SynchroniseToken {

	public TimeAdvanceToken(AbstractSimEventDelegator returnEvent,
			AbstractSimEntityDelegator entity, 
			double timestep) {
		super(returnEvent, entity, SynchronisedActionTypen.ADVANCE_TIME, timestep, entity.getModel().getSimulationControl().getCurrentSimulationTime(), timestep);
		Utils.log(entity, "Want to Advance from: " + entity.getModel().getSimulationControl().getCurrentSimulationTime() + " to: " + getResultingTimepoint());
	}

	@Override
	public void executeAction() {
	
			((WorkwayModel)(getEntity().getModel())).getComponent().synchronisedAdvancedTime(getTimeStep());
	
	}

}
