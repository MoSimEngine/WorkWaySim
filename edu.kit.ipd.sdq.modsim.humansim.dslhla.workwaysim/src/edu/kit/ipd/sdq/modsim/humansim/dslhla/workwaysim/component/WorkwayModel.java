package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.HumanWalksDirectlyToWorkEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.events.WalkToBusStopAtHomeEvent;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.CSVHandler;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.exceptions.RTIexception;


public class WorkwayModel extends AbstractSimulationModel{

	 private boolean PROCESS_ORIENTED = false;
	 
	 private LinkedList<BusStop> stops;
	 
	 private double startTime; 
	 
	 public int modelRun;
	 public LinkedList<Double> durations;
	 
	 private LinkedList<Human> humans;
	 
	 private WorkwayFederate component;
	 
	 private boolean scanning;

	public WorkwayModel(ISimulationConfig config, ISimEngineFactory factory) {
		super(config, factory);
		humans = new LinkedList<Human>();
		stops = new LinkedList<BusStop>();
	}
	
	public void init() {
		
		startTime = System.nanoTime();
   
        
            // schedule a process for each bus
            
        	
        	// schedule a process for each human
		try {
			component.runFederate("WorkwayFed");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void finalise() {
		try {
			component.endExecution();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Writing Data");
	 	Double finalTime = (System.nanoTime() - startTime) / Math.pow(10, 9);
		String file_header = "";
		String csvAway = "";
		String csvWaitingAtStation = "";
		String csvDrivingTimes = "";
		String behaviourMarker = "";
		String csvFreeTimes = "";
		
		int getMaxNumValues = 0;
	 	
	 	for (Human human : humans) {
	 		
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
	 		
	 		for(int j = 0; j < humans.size(); j++){
	 			ArrayList<Duration> away = humans.get(j).getAwayFromHomeTimes();
			 	ArrayList<Duration> driven = humans.get(j).getDrivingTimes();
			 	ArrayList<Duration> waited = humans.get(j).getBusWaitingTimes();
			 	ArrayList<Duration> free = humans.get(j).getFreeTimes();
			 	
			 	if(away.size() > i){
			 		csvAway += Math.round(away.get(i).toHours().value()*100.00)/100.00;
			 		csvDrivingTimes += Math.round(driven.get(i).toMinutes().value()*100.00)/100.00;
			 		csvWaitingAtStation += Math.round(waited.get(i).toMinutes().value()*100.00)/100.00;
			 		csvFreeTimes += Math.round(free.get(i).toMinutes().value()*100.00)/100.00;
			 	} else {
			 		csvAway += "-1";
			 		csvDrivingTimes += "-1";
			 		csvWaitingAtStation += "-1";
			 		csvFreeTimes += "-1";
			 	}
			 	
			 	if(j < humans.size()-1){
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
	       		
	       		
	       		CSVHandler.writeCSVFile(s, file_header + behaviourMarker + csvs[i]);
	       		
	        }      	
	       	
	       	Double d =  Math.round(finalTime*100.00)/100.00;
	       	String s = d.toString();
	       	
	       	s = s.replace('.', ',');
	       	
	       	CSVHandler.readCSVAndAppend("ExecutionTimes", s + CSVHandler.CSV_DELIMITER);
	       	
	       	
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
    	
    	for (Human human : humans) {
			
		
    	  if (PROCESS_ORIENTED) {
              // schedule a process for each bus
              
    	  } else {
    		  if(human.willWalk()){
      			new HumanWalksDirectlyToWorkEvent(this, human.getName() + "walks directly").schedule(human,0);
      		} else {
      			new WalkToBusStopAtHomeEvent(this, human.getName() + "walks to bus station").schedule(human ,0);
      		}
    	  }
    	}
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
	
	public void startScanningForHLAEvents(){
		
		this.scanning = true;
		
		while(scanning){
			try {
				component.advanceTime(HumanSimValues.BUSY_WAITING_TIME_STEP.toSeconds().value());
			} catch (RTIexception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void scheduleHumanEntersEvent(String humanName, String busStopName){
		for (Human human : humans) {
			if(human.getName().equals(humanName)){
				for(BusStop busStop : stops){
					if(busStop.getName().equals(busStopName)){

						HumanEntersBusEvent e = new HumanEntersBusEvent(this, "HumanEntersBus");
						e.schedule(human, 0);
						
						return;
					}
				}
			}
			
		}
	}
	
	public void scheduleHumanExitsEvent(String humanName, String busStopName){
		System.out.println("Scheduling Exit Event");
		for (Human human : humans) {
			if(human.getName().equals(humanName)){
				System.out.println("FoundHuman");
				for(BusStop busStop : stops){
					System.out.println("FoundBusStop");
					if(busStop.getName().equals(busStopName)){
						
						HumanExitsBusEvent e = new HumanExitsBusEvent(this, "HumanEntersBus");
						e.schedule(human, 0);
						System.out.println("Event Schedled");
						return;
					}
				}
			}
			
		}
	}

	public boolean isScanning() {
		return scanning;
	}

	public void setScanning(boolean scanning) {
		this.scanning = scanning;
	}
	
	public void initialiseHumans(){
		for(int i = 0; i < HumanSimValues.NUM_HUMANS; i++){
    		//new HumanProcess(new Human(stop1, stop3, this, "Bob" + i), bus).scheduleAt(0);
    		int homeBS = 0;
    		int workBS = 0;
    		
    		while(homeBS == workBS){
    			homeBS = new Random().nextInt(HumanSimValues.NUM_BUSSTOPS);
    			workBS = new Random().nextInt(HumanSimValues.NUM_BUSSTOPS);
    		}
    		
    		
    		Human hu = new Human(stops.get(homeBS), stops.get(workBS), this, "Bob" + i);
    		humans.add(hu);
    		//System.out.println("Added: " + hu.getName());
        //new PassengerArrivalEvent(Duration.seconds(2.0), this, "BS").schedule(stop1, 0);
		}
	}
	

	
}
