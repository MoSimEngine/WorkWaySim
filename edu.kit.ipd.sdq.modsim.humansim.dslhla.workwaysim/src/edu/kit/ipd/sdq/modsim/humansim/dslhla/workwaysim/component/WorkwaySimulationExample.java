package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.util.LinkedList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;


public class WorkwaySimulationExample implements IApplication {

	private WorkwaySimConfig config;
	private WorkwayModel model;
	private ISimulationControl simControl;
	private WorkwayFederate component;
	private LinkedList<WorkwayModel> models;
	private Thread[] threads = new Thread[HumanSimValues.NUM_HUMANS];
	
	 private static final Duration MAX_SIMULATION_TIME = HumanSimValues.MAX_SIM_TIME;
	
	
	public WorkwaySimulationExample() {
		
		this.models = new LinkedList<WorkwayModel>();
		
		for(int i = 0; i < HumanSimValues.NUM_HUMANS; i++){
			WorkwaySimConfig config = new WorkwaySimConfig();
			WorkwayModel model = WorkwayModel.create(config);
			ISimulationControl simControl = model.getSimulationControl();
			simControl.setMaxSimTime((long) MAX_SIMULATION_TIME.toSeconds().value());
			WorkwayFederate component = new WorkwayFederate(model);
			model.setComponent(component);
			model.setId(i);
			models.add(model);
		}
		
	}
	public Object start(IApplicationContext context) throws Exception {
		
		BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        
        for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(models.get(i));
			models.get(i).setModels(models);
			threads[i].start();
		}

        
        int closed = 0;
        while(closed != HumanSimValues.NUM_HUMANS){
        	closed = 0;
        	for (int i = 0; i < HumanSimValues.NUM_HUMANS; i++) {
    			if(!threads[i].isAlive()){
    				closed++;
    			}
    		}
        }
       
        System.out.println("All closed");
        // run the simulation
       

        return EXIT_OK;
	}


	public void stop() {
		// nothing to do;
	}

}
