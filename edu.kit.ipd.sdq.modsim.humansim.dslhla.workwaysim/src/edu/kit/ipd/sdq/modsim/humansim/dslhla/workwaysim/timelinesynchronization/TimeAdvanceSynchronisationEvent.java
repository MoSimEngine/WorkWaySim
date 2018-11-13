package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.IEntity;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;

public class TimeAdvanceSynchronisationEvent extends AbstractSimEventDelegator{

	private AbstractSimEventDelegator event;
	private double timestep;
	
	public TimeAdvanceSynchronisationEvent(ISimulationModel model, String name, AbstractSimEventDelegator event, double timestep) {
		super(model, name);
		this.event = event;
		this.timestep = timestep;
	}

	@Override
	public void eventRoutine(IEntity entity) {
		
		AbstractSimEntityDelegator e = (AbstractSimEntityDelegator)entity;
		
		WorkwayModel m = ((WorkwayModel)e.getModel());
		Human h = (Human) e;
		
		if(h.getTaToken() != null) {
		m.getTimelineSynchronizer().revokeToken(h.getTaToken());
		}
	
		
		TimeAdvanceToken token = new TimeAdvanceToken(event, e, timestep);
//		Utils.log(e, "Executing TASEvent with return event: " + event.getName() + "to be scheduled for: " + token.getReturnEventTimepoint());
		m.getTimelineSynchronizer().putToken(token);
		
	}

}
