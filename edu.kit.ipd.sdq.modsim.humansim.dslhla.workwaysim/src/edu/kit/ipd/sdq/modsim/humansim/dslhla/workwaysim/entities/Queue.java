package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.SynchroniseToken;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class Queue extends Position implements Comparable<Queue>{

	private ObjectInstanceHandle oih;
	private ObjectClassHandle och;
	
    public Queue(ISimulationModel model, String name) {
        super(model, name, PositionType.QUEUE);
    }


    @Override
    public String toString() {
        return getName();
    }


	public ObjectClassHandle getOch() {
		return och;
	}


	public void setOch(ObjectClassHandle och) {
		this.och = och;
	}


	public ObjectInstanceHandle getOih() {
		return oih;
	}


	public void setOih(ObjectInstanceHandle oih) {
		this.oih = oih;
	}

	@Override
	public int compareTo(Queue arg0) {
		return this.getName().compareTo(arg0.getName());
	}

}
