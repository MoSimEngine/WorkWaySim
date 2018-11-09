package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.RegisterToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.SynchroniseToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.timelinesynchronization.TimeAdvanceToken;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;


public class Human extends AbstractSimEntityDelegator {

	public static enum HumanState {
		AT_HOME, 
		AT_WORK, 
		TRAVELLING,
		WALKING,
		AT_BUSSTOP,
	}

	public enum HumanBehaviour{
		DRIVING_BY_BUS,
		WALKING_DIRECTLY,
	}
	
	private ObjectInstanceHandle oih;

	private ObjectClassHandle och;

	private HumanState state;
	private HumanBehaviour behaviour;

	private Position position;

	private Position destination;
	
	private volatile boolean collected;

	public Duration HOME_TO_STATION; 

	public Duration WORK_TO_STATION;
	
	public Duration WALK_DIRECTLY;
	
	private SynchroniseToken taToken;
	
	private LinkedList<SynchroniseToken> regTokens;
	
	public  final Duration WORKTIME = Duration.hours(8);
	
	private ArrayList<Position> workway = new ArrayList<Position>();
	
	private int positionIndex = 0;
	//Regulates the index direction for traversal of the workway list 
	// Values: 1 -> forward; -1 -> backward;
	private int direction = 1;
	
	
	
	public   Duration FREETIME = Duration.hours(0); 
	private Duration timeDriven = Duration.seconds(0);
	private ArrayList<Duration> awayFromHomeTimes;
	private ArrayList<Duration> busWaitingTimes;
	private ArrayList<Duration> drivingTimes;
	private ArrayList<Duration> freeTimes;	
	
	private Duration timeWaitedAtBusStop = Duration.seconds(0);
	
	private double timePointAtBusStop = 0;
	private double timePointCollected = 0;

	public Human(BusStop home, BusStop work, ISimulationModel model, String name) {
		super(model, name);

	
		state = HumanState.AT_HOME;
		if(HumanSimValues.STOCHASTIC){
		behaviour = HumanBehaviour.values()[new Random().nextInt(2)];
		} else {
			behaviour = HumanBehaviour.DRIVING_BY_BUS;
		}
		
		workway.add(new Position(model, "Home", PositionType.HOME));
		
		if(!behaviour.equals(HumanBehaviour.WALKING_DIRECTLY)) {
			workway.add(home);
			workway.add(work);
		}

		workway.add(new Position(model, "Work", PositionType.WORK));
		
		position = workway.get(positionIndex);
		destination = workway.get(positionIndex+1);
		
		if(HumanSimValues.STOCHASTIC){
			HOME_TO_STATION = Duration.minutes(new Random().nextInt(60) + 1);
			WORK_TO_STATION = Duration.minutes(new Random().nextInt(60) + 1);
			WALK_DIRECTLY = Duration.minutes(Duration.minutes(new Random().nextInt(200) + 1).value());
		} else {
			HOME_TO_STATION = Duration.minutes(30);
			WORK_TO_STATION = Duration.minutes(30);
			WALK_DIRECTLY = Duration.minutes(90);
		}
		
		awayFromHomeTimes = new ArrayList<Duration>();
		busWaitingTimes = new ArrayList<Duration>();
		drivingTimes = new ArrayList<Duration>();
		freeTimes = new ArrayList<Duration>();	
		regTokens = new LinkedList<SynchroniseToken>();
	}

	public Human(BusStop home, BusStop work, ISimulationModel model, String name, ObjectInstanceHandle oih, ObjectClassHandle och){
		super(model, name);
	
		// start at home
		state = HumanState.AT_HOME;
		if(HumanSimValues.STOCHASTIC){
		behaviour = HumanBehaviour.values()[new Random().nextInt(2)];
		} else {
			behaviour = HumanBehaviour.DRIVING_BY_BUS;
		}
		
		workway.add(new Position(model, "Home", PositionType.HOME));
		
		if(!behaviour.equals(HumanBehaviour.WALKING_DIRECTLY)) {
			workway.add(home);
			workway.add(work);
		}

		workway.add(new Position(model, "Work", PositionType.WORK));
		
		position = workway.get(positionIndex);
		destination = workway.get(positionIndex+1);
		
		if(HumanSimValues.STOCHASTIC){
			HOME_TO_STATION = Duration.minutes(new Random().nextInt(60) + 1);
			WORK_TO_STATION = Duration.minutes(new Random().nextInt(60) + 1);
			WALK_DIRECTLY = Duration.minutes(Duration.minutes(new Random().nextInt(200) + 1).value());
		} else {
			HOME_TO_STATION = Duration.minutes(30);
			WORK_TO_STATION = Duration.minutes(30);
			WALK_DIRECTLY = Duration.minutes(90);
		}
		
		awayFromHomeTimes = new ArrayList<Duration>();
		busWaitingTimes = new ArrayList<Duration>();
		drivingTimes = new ArrayList<Duration>();
		freeTimes = new ArrayList<Duration>();	
		
		this.oih = oih;
		this.och = och;
		
	}


	//BusDriving state changes
	
	public void walkToNext() {
		if(state.equals(HumanState.AT_HOME) || state.equals(HumanState.AT_WORK) || state.equals(HumanState.AT_BUSSTOP)) {
			state = HumanState.WALKING;
		} else {
			throw new IllegalStateException("Human cannot walk!" + "CurrentState: " + this.state.toString());
		}
	}
	
	public void travellingToNext() {
		if(state.equals(HumanState.AT_BUSSTOP)) {
			state = HumanState.TRAVELLING;
		} else {
			throw new IllegalStateException("How to drive by bus when not at BusStop???" + "CurrentState: " + this.state.toString());
		}
	}
	
	public void arriveAtWork() {
		if(state.equals(HumanState.WALKING)) {
			state = HumanState.AT_WORK;
		} else {
			throw new IllegalStateException("There is no teleportation to work!" + "CurrentState: " + this.state.toString());
		}
	}
	
	public void arriveAtHome() {
		if(state.equals(HumanState.WALKING)) {
			state = HumanState.AT_HOME;
		} else {
			throw new IllegalStateException("There is no teleportation to home!" + "CurrentState: " + this.state.toString());
		}
	}
	
	public void arriveAtBusStop() {
		if(state.equals(HumanState.WALKING) || state.equals(HumanState.TRAVELLING)) {
			state = HumanState.AT_BUSSTOP;
		} else {
			throw new IllegalStateException("There is no teleportation to a BusStop!" + "CurrentState: " + this.state.toString());
		}
	}
	

	
	public ArrayList<Duration> getFreeTimes(){
		return freeTimes;
	}
	
	public Position getPosition(){
		return this.position;
	}
	
	public Position getDestination(){
		return this.destination;
	}

	public boolean isCollected() {
		return collected;
	}

	public void setCollected(boolean collected) {
		this.collected = collected;
	}
	
	public HumanState getState(){
		return this.state;
	}


	public HumanBehaviour getBehaviour(){
		return behaviour;
	}
	
	public ArrayList<Duration> getBusWaitingTimes(){
		return busWaitingTimes;
	}
	
	public ArrayList<Duration> getDrivingTimes(){
		return drivingTimes;
	}
	
	public ArrayList<Duration> getAwayFromHomeTimes(){
		return awayFromHomeTimes;
	}
	

	
	public void arriveAtBusStopWalkingTimePointLog(){
		
		if (timePointAtBusStop != 0.0)
			throw new IllegalStateException("time point arrived at bus stop was not zero");
		
		timePointAtBusStop = getModel().getSimulationControl().getCurrentSimulationTime();
//		timePointAtBusStop = ((WorkwayModel) getModel()).getComponent().getCurrentFedTime();
	}
	
	public void calculateWaitedTime(){
		timeWaitedAtBusStop = Duration.seconds(timeWaitedAtBusStop.toSeconds().value() + Duration.seconds(getModel().getSimulationControl().getCurrentSimulationTime() - timePointAtBusStop).value());
//		timeWaitedAtBusStop = Duration.seconds(timeWaitedAtBusStop.toSeconds().value() + Duration.seconds(((WorkwayModel) getModel()).getComponent().getCurrentFedTime() - timePointAtBusStop).value());
		timePointAtBusStop = 0.0;
	}
	
	public void humanIsCollected(){
		if (timePointCollected != 0.0)
			throw new IllegalStateException("time point arrived at bus stop was not zero, was:" + timePointCollected);
		
		timePointCollected = this.getModel().getSimulationControl().getCurrentSimulationTime();
//		timePointCollected = ((WorkwayModel) getModel()).getComponent().getCurrentFedTime();
		//System.out.println("Human" + this.getName() + "collected at" + timePointCollected);
	}
	
	public void calculateDrivingTime(){
		timeDriven = Duration.seconds(timeDriven.toSeconds().value() + Duration.seconds(getModel().getSimulationControl().getCurrentSimulationTime() - timePointCollected).value());
//		timeDriven = Duration.seconds(timeDriven.toSeconds().value() + Duration.seconds(((WorkwayModel) getModel()).getComponent().getCurrentFedTime() - timePointCollected).value());
		timePointCollected = 0.0;
	}
	
	
	public void calculateFreeTime(){
		Duration onTheWay = Duration.seconds(0);
		if(behaviour.equals(HumanBehaviour.WALKING_DIRECTLY)){
			onTheWay = Duration.seconds(WORKTIME.toSeconds().value() + 2*WALK_DIRECTLY.toSeconds().value()); 
		} else if (behaviour.equals(HumanBehaviour.DRIVING_BY_BUS)){
			onTheWay = Duration.seconds(WORKTIME.toSeconds().value() + 2*HOME_TO_STATION.toSeconds().value() + 2* WORK_TO_STATION.toSeconds().value() + timeDriven.toSeconds().value() + timeWaitedAtBusStop.value());
		}
		
		busWaitingTimes.add(timeWaitedAtBusStop);
		drivingTimes.add(timeDriven);
		awayFromHomeTimes.add(onTheWay);

		
		
		double total= 24.00 - onTheWay.toHours().value();
		FREETIME = Duration.hours(total);
		freeTimes.add(FREETIME);
		Utils.log(this, "Enjoys: " + FREETIME.toHours().value() + " of Freetime");
		if(FREETIME.toSeconds().value() < 0.0){
			Utils.log(this, this.getName() + " has no freetime :(");
			FREETIME = Duration.hours(0);
		}
		
		this.timeDriven = Duration.seconds(0);
		timePointAtBusStop = 0;
		timeWaitedAtBusStop = Duration.seconds(0);
	}

	public ObjectInstanceHandle getOih() {
		return oih;
	}

	public void setOih(ObjectInstanceHandle oih) {
		this.oih = oih;
	}

	public ObjectClassHandle getOch() {
		return och;
	}

	public void setOch(ObjectClassHandle och) {
		this.och = och;
	}

	
	public Position nextPosition() {
		
		this.position = this.destination;
		this.positionIndex += direction;
		
		if(positionIndex + direction >= workway.size()){
			direction = (-1);
		} else if (positionIndex + direction < 0) {
			direction = 1;
		}
		
		this.destination = this.workway.get(positionIndex + direction);
		
		
		
		return this.position;
		
	}

	public SynchroniseToken getTaToken() {
		return taToken;
	}

	public void setTaToken(SynchroniseToken token) {
		this.taToken = token;
	}

	public LinkedList<SynchroniseToken> getRegTokens() {
		return regTokens;
	}

	public void addRegToken(SynchroniseToken regToken) {
		regTokens.add(regToken);
	}
	
	public void removeRegToken(SynchroniseToken regToken) {
		regTokens.remove(regToken);
	}
	

}