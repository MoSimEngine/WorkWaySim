package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimulationModel;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationConfig;
import de.uka.ipd.sdq.simulation.preferences.SimulationPreferencesHelper;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.HumanEntersBusEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.HumanExitsBusEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.TravelToNextEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.CSVHandler;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;


public class WorkwayModel extends AbstractSimulationModel implements Runnable{

	 private LinkedList<BusStop> stops;

	 private double startTime; 
	 
	 public int modelRun;
	 public LinkedList<Double> durations;
	 
	 private LinkedList<Human> humans;
	 
	 private WorkwayFederate component;
	 
	 private boolean scanning;
	 
	 private LinkedList<WorkwayModel> models;
	 private Human human;
	 private int id;
	 
	 public boolean hasToKeepAlive = false;
	 
	 private boolean finished = false;

	public WorkwayModel(ISimulationConfig config, ISimEngineFactory factory) {
		super(config, factory);
		humans = new LinkedList<Human>();
		stops = new LinkedList<BusStop>();
	}
	
	public void init() {
		
		startTime = System.nanoTime();
   
		try {
			component.runFederate("WorkwayFed" + id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void finalise() {
		
		
		try {
			component.getRTIAmb().resignFederationExecution(ResignAction.DELETE_OBJECTS);
		} catch (InvalidResignAction | OwnershipAcquisitionPending | FederateOwnsAttributes | FederateNotExecutionMember
				| NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		finished = true;
		
		int countFinished = 0;
		
	
		if(this.getId() == 0){
			
		while(countFinished != HumanSimValues.NUM_HUMANS){
			try {
				java.util.concurrent.TimeUnit.SECONDS.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			countFinished = 0;
			
			for (WorkwayModel model : models) {
				if(model.finished){
						countFinished++;
				}
			}
		}
		
		
		
//		System.out.println("Writing Data");
	 	Double finalTime = (System.nanoTime() - startTime) / Math.pow(10, 9);
	 	System.out.println("Time taken");
		String file_header = "";
		String csvAway = "";
		String csvWaitingAtStation = "";
		String csvDrivingTimes = "";
		String behaviourMarker = "";
		String csvFreeTimes = "";
		
		int getMaxNumValues = 0;
	 	
	 	for (WorkwayModel model : models) {
	 		Human human = model.getHuman();
	 		file_header += human.getName() + CSVHandler.CSV_DELIMITER;
	 		behaviourMarker += human.getBehaviour().toString() + CSVHandler.CSV_DELIMITER;
	 		System.out.println("Human " + human.getName() + " is in State " + human.getState() + " and is "+ human.getBehaviour().toString());
	 		if(getMaxNumValues < human.getAwayFromHomeTimes().size()){
	 			getMaxNumValues = human.getAwayFromHomeTimes().size();
	 		}
		}
	 	
	 	file_header += CSVHandler.NEWLINE;
	 	behaviourMarker += CSVHandler.NEWLINE;
	 	
	 	for(int i = 0; i < getMaxNumValues; i++){
	 		
	 		for(int j = 0; j < models.size(); j++){
	 			
	 			Human human = models.get(j).getHuman();
	 			ArrayList<Duration> away = human.getAwayFromHomeTimes();
			 	ArrayList<Duration> driven = human.getDrivingTimes();
			 	ArrayList<Duration> waited = human.getBusWaitingTimes();
			 	ArrayList<Duration> free = human.getFreeTimes();
			 	
			 	if(away.size() > i){
			 		csvAway += away.get(i).toSeconds().value();
			 		csvDrivingTimes += driven.get(i).toSeconds().value();
			 		csvWaitingAtStation += waited.get(i).toSeconds().value();
			 		csvFreeTimes += free.get(i).toSeconds().value();
			 	} else {
			 		csvAway += "-1";
			 		csvDrivingTimes += "-1";
			 		csvWaitingAtStation += "-1";
			 		csvFreeTimes += "-1";
			 	}
			 	
			 	if(j < models.size()-1){
			 		csvAway += CSVHandler.CSV_DELIMITER;
			 		csvDrivingTimes += CSVHandler.CSV_DELIMITER;
			 		csvWaitingAtStation += CSVHandler.CSV_DELIMITER;
			 		csvFreeTimes += CSVHandler.CSV_DELIMITER;
			 	}
			 	
	 		}
	 		
	 		csvAway += CSVHandler.NEWLINE;
	 		csvDrivingTimes += CSVHandler.NEWLINE;
	 		csvWaitingAtStation += CSVHandler.NEWLINE;
	 		csvFreeTimes += CSVHandler.NEWLINE;
	 	}
	 	
		
		 
//		 for (Human human : this.humans) {
//			 	
//			 	//Duration[] away = (Duration[]) human.getAwayFromHomeTimes().toArray();
//			 	
//			 	ArrayList<Duration> away = human.getAwayFromHomeTimes();
//			 	ArrayList<Duration> driven = human.getDrivingTimes();
//			 	ArrayList<Duration> waited = human.getBusWaitingTimes();
//			 	
//			 	
//			 	for (int i = 0; i < away.size(); i++){
//			 		
//			 		
//			 		System.out.println("Day " + i + ": Away From Home (Hours)" + Math.round(away.get(i).toHours().value()*100.00) /100.00 
//			 				+ "; Driven (Minutes): " + Math.round(driven.get(i).toMinutes().value() * 100.00)/100.00 
//			 				+ "; Waiting at Stations (Minutes): " + waited.get(i).toMinutes().value());
//				}
//			 	
//			 	
//		        System.out.println("-----------------------------");
//		}
	       	
	       	
	       	String[] csvs = {csvAway, csvDrivingTimes, csvWaitingAtStation, csvFreeTimes};
	       	
	       	for(int i = 0; i < csvs.length; i++){
	       		String s = "";
	       		
	       		switch (i) {
				case 0:
					s = "AwayTimes";
					break;
				case 1:
					s = "DrivingTimes";
					break;
				case 2: 
					s = "BusStationWaitingTimes";
					break;
				case 3:
					s = "FreeTimes";
					break;

				default:
					throw new IllegalStateException("More than expected files");
				}	
	       		
	       		csvs[i] = csvs[i].replace('.', ',');
	       		
	       		
	       		CSVHandler.writeCSVFile(s, file_header + csvs[i]);
	       		
	        }      	
	       	
	       	CSVHandler.writeCSVFile("HumanBehaviour", behaviourMarker);
	       	
	       	Double d =  Math.round(finalTime*100.00)/100.00;
	       	String s = d.toString();
	       	
	       	s = s.replace('.', ',');
	       	
	       	CSVHandler.readCSVAndAppend("ExecutionTimes", s + CSVHandler.CSV_DELIMITER);
	       	
	       	System.out.println("Done writing data");
	      
	      
				try {
					getComponent().endExecution();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	       	
		}
		
		System.out.println("Finalized in " + getId());
	
	}

    /**
     * Creates the simulation model for the specified configuration.
     * 
     * @param config
     *            the simulation configuration
     * @return the created simulation model
     */
    public static WorkwayModel create(final WorkwaySimConfig config) {
        // load factory for the preferred simulation engine
        ISimEngineFactory factory = SimulationPreferencesHelper.getPreferredSimulationEngine();
        if (factory == null) {
            throw new RuntimeException("There is no simulation engine available. Install at least one engine.");
        }

        // create and return simulation model
        final WorkwayModel model = new WorkwayModel(config, factory);

        return model;
    }
    
    public void startSimulation(){
    	  new TravelToNextEvent(this, human.getName() + "starts travelling").schedule(human, component.getCurrentFedTime());
    }

	public WorkwayFederate getComponent() {
		return component;
	}

	public void setComponent(WorkwayFederate component) {
		this.component = component;
	}
	
	public LinkedList<BusStop> getStops(){
		return stops;
	}
	
	public void setStops(LinkedList<BusStop> stops){
		this.stops = stops;
	}
	
	public LinkedList<Human> getHumans(){
		return humans;
	}
	
	public void addBusStop(BusStop stop){
		
		for (BusStop bs : stops) {
			if(stop.getOih().equals(bs.getOih())){
				System.out.println("Stop already there");
				return;
			}
			
		}
		stops.add(stop);

	}
	
	public void scheduleHumanEntersEvent(String humanName, String busStopName, double passedTime){
		
			if(human.getName().equals(humanName)){
				for(BusStop busStop : stops){
					if(busStop.getName().equals(busStopName)){
						Utils.log(human, "Scheduling Enters Event on " + getSimulationControl().getCurrentSimulationTime() + "for Time:" + passedTime);
//						Utils.log(human, "Handle Enters-Event on " + getComponent().getCurrentFedTime());
						HumanEntersBusEvent e = new HumanEntersBusEvent(this, "HumanEntersBus");
						Utils.log(human, "Time to Pass: " + passedTime + " SimulationTime: " + getSimulationControl().getCurrentSimulationTime());
						double diff = passedTime - getSimulationControl().getCurrentSimulationTime();
						BigDecimal st = BigDecimal.valueOf(getSimulationControl().getCurrentSimulationTime());
						BigDecimal pt = BigDecimal.valueOf(passedTime);
						BigDecimal d2 = pt.subtract(st);
						
						Utils.log(human, "BC: " + "Time to Pass: " + pt.doubleValue() + " SimulationTime: " + st.doubleValue() + " Res:" + d2.doubleValue() + " would result in : " + st.add(d2));
						
						diff = Utils.roundTo4Decimals(diff);
						Utils.log(human, "Bridging timediff:" + diff);
			
						BigDecimal d = BigDecimal.valueOf(diff);
						
						st.add(d);
						
						Utils.log(human, "BigDecimal: " + st.add(d).doubleValue());
						
						double reach = getSimulationControl().getCurrentSimulationTime() + diff;
						Utils.log(human, "Time with diff: " + reach);
						
						e.schedule(human, d2.doubleValue());
						return;
					}
				
			}
			
		}
	}
	
	public void scheduleHumanExitsEvent(String humanName, String busStopName, double passedTime){

		
			if(human.getName().equals(humanName)){
				
				for(BusStop busStop : stops){
					//System.out.println("FoundBusStop");
					if(busStop.getName().equals(busStopName)){
						
						Utils.log(human, "Scheduling Exit Event on " + getComponent().getCurrentFedTime() + "for Time:" + passedTime);
						HumanExitsBusEvent e = new HumanExitsBusEvent(this, "HumanExitsBus");
						double diff = passedTime - getSimulationControl().getCurrentSimulationTime();
						BigDecimal st = BigDecimal.valueOf(getSimulationControl().getCurrentSimulationTime());
						BigDecimal pt = BigDecimal.valueOf(passedTime);
						BigDecimal d2 = pt.subtract(st);
						Utils.log(human, "BC: " + "Time to Pass: " + pt.doubleValue() + " SimulationTime: " + st.doubleValue() + " Res:" + d2.doubleValue() );
//						diff = Utils.roundTo4Decimals(diff);
						Utils.log(human, "Bridging timediff:" + diff);
						//Utils.log(human, "Scheduling Exit Event for Human: " + human.getName() + " exit on BusStop: " + busStop.getName() );
						e.schedule(human, d2.doubleValue());
						return;
					}
				}
			
			
		}
	}
	
	public void initialiseHumans(){
			BusStop[] tmpStops = new BusStop[3];
			
			for ( BusStop stop : stops) {
			
				switch (stop.getName()) {
				case "Stop1":
					tmpStops[0] = stop;
					break;
				case "Stop2":
					tmpStops[1] = stop;
					break;
				case "Stop3":
					tmpStops[2] = stop;
					break;
				default:
					break;
				}
				
			}
			
			stops.clear();
			stops.add(tmpStops[0]);
			stops.add(tmpStops[1]);
			stops.add(tmpStops[2]);
			
			
    		//new HumanProcess(new Human(stop1, stop3, this, "Bob" + i), bus).scheduleAt(0);
    		int homeBS = 0;
    		int workBS = 0;
    		
    	
    		
    		
    		if(HumanSimValues.STOCHASTIC){
    		while(homeBS == workBS){
    			homeBS = new Random().nextInt(HumanSimValues.NUM_BUSSTOPS);
    			workBS = new Random().nextInt(HumanSimValues.NUM_BUSSTOPS);
    		}
    		} else {
    			homeBS = this.id % 2;
    			workBS = (this.id % 2) + 1;
    		}
    		
    		
    		this.human = new Human(stops.get(homeBS), stops.get(workBS), this, "Bob" + id);
	}

	@Override
	public void run() {
		getSimulationControl().start();	
	}

	public LinkedList<WorkwayModel> getModels() {
		return models;
	}

	public void setModels(LinkedList<WorkwayModel> models) {
		this.models = models;
	}

	public Human getHuman() {
		return human;
	}

	public void setHuman(Human human) {
		this.human = human;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void registerHumanAtBusStop(Human human, BusStop busStop, BusStop destination){
		try {
			this.component.sendRegisterInteraction(human, busStop.getName(), destination.getName());
		} catch (RTIexception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
}
