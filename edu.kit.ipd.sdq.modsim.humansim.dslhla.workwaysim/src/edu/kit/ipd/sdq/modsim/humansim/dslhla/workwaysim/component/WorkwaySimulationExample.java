package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.util.LinkedList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration.TimeUnit;


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
	public synchronized Object start(IApplicationContext context) throws Exception {
		
		BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
       
        int spincounter = 0;
        for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(models.get(i));
			models.get(i).setModels(models);
			threads[i].start();
			
			if(i != 0 && (i % 4) == 0 ){
				System.out.println("Started " + i + " Human Models");
				java.util.concurrent.TimeUnit.SECONDS.sleep(20);
			}
			
		
		}
        System.out.println("Spawned all models - let the simulating commence!");
        
        int closed = 0;
        while(closed != HumanSimValues.NUM_HUMANS){
        	java.util.concurrent.TimeUnit.MINUTES.sleep(2);
        	closed = 0;
        	for (int i = 0; i < HumanSimValues.NUM_HUMANS; i++) {
    			if(!threads[i].isAlive()){
    				closed++;
    			}
    		}
        }
       
        System.out.println("All Workway Simulations Closed");
        // run the simulation
       

        return EXIT_OK;
	}


	public void stop() {
		// nothing to do;
	}

}
