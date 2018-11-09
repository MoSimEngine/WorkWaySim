package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization;

import java.math.BigDecimal;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;

public abstract class SynchroniseToken implements Comparable<SynchroniseToken>{

	private AbstractSimEventDelegator returnEvent;
	private AbstractSimEntityDelegator entity;
	private BigDecimal timestep;
	private BigDecimal currentTimepoint;
	private double resultingTimepoint;
	private double returnEventTimestep;
	
	public enum SynchronisedActionTypen{
		RTI_ACTION,
		ADVANCE_TIME
	}
	
	private SynchronisedActionTypen tokenSynchroType;
	
	public SynchroniseToken(AbstractSimEventDelegator returnEvent, 
			AbstractSimEntityDelegator entity, 
			SynchronisedActionTypen type,
			double timestep,
			double currentTimepoint,
			double returnEventTimestep) {
		this.returnEvent = returnEvent;
		this.entity = entity;
		this.tokenSynchroType = type;
		this.timestep = BigDecimal.valueOf(timestep);
		this.currentTimepoint = BigDecimal.valueOf(currentTimepoint);
		this.returnEventTimestep = returnEventTimestep;
		this.resultingTimepoint = this.currentTimepoint.add(this.timestep).doubleValue();
	}

	public AbstractSimEntityDelegator getEntity() {
		return entity;
	}

	public AbstractSimEventDelegator getReturnEvent() {
		return returnEvent;
	}
	
	public SynchronisedActionTypen getTokenSynchroType() {
		return tokenSynchroType;
	}

	public double getResultingTimepoint() {
		return resultingTimepoint;
	}
	
	public double getTimeStep() {
		return timestep.doubleValue();
	}
	
	public double getReturnEventTimestep() {
		return returnEventTimestep;
	}
	
	public double getCurrentTimepoint() {
		return currentTimepoint.doubleValue();
	}
	
	public void reduceTimestep(double reductor) {
		double tmp = timestep.doubleValue();
		this.timestep = timestep.subtract(BigDecimal.valueOf(reductor));
		
		if(timestep.doubleValue() < 0.0) {
			timestep = BigDecimal.ZERO;
		}
		
		Utils.log("Reduced Timestep from: " + tmp + "to: " + timestep.doubleValue());
	}

	@Override
	public int compareTo(SynchroniseToken o) {
		return Double.compare(this.resultingTimepoint, o.getResultingTimepoint());
	}
	
	public abstract void executeAction();
	
}
