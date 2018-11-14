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
//		Utils.log(entity, "Want to Advance from: " + entity.getModel().getSimulationControl().getCurrentSimulationTime() + " to: " + getResultingTimepoint());
	}

	@Override
	public void executeAction() {
			WorkwayModel m = (WorkwayModel)getEntity().getModel();
			
			double fedTime = m.getComponent().getCurrentFedTime();

			double resTime = this.getResultingTimepoint();
		
			double targetTime = fedTime + this.getTimeStep();
			double diff = 0; 
			
//			if(fedTime > resTime) {
//				Utils.log(this.getEntity(), "Dont advance time, would advance over my time> " + fedTime + ":" + resTime);
//				return;
//			}
//			
			
			if(targetTime > resTime) {
				diff = targetTime - resTime;
			}
			

			m.getComponent().synchronisedAdvancedTime(getTimeStep() - diff);
	
	}

}
