package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

public class BusStop extends AbstractSimEntityDelegator {

	private ObjectInstanceHandle oih;

	private ObjectClassHandle och;
    public BusStop(ISimulationModel model, String name) {
        super(model, name);
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

}
