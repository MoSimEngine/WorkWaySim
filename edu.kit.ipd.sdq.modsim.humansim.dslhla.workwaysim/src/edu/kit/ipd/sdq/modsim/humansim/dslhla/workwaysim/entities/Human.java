package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities;

import java.util.ArrayList;
import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.Duration;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.HumanSimValues;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component.WorkwayModel;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Position.PositionType;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;


public class Human extends AbstractSimEntityDelegator {

	public static enum HumanState {
		AT_HOME, 
		AT_WORK, 
		GO_TO_BUSSTOP_HOME, 
		GO_TO_BUSSTOP_WORK, 
		AT_BUSSTOP_HOME, 
		AT_BUSSTOP_WORK,
		DRIVING_HOME,
		DRIVING_TO_WORK,
		WALK_HOME_FROM_BUSSTOP,
		WALK_TO_WORK_FROM_BUSSTOP,
//		
		WALKING_DIRECTLY_TO_WORK,
		WALKING_DIRECTLY_HOME,
		
		DRIVING_BY_TAXI_TO_WORK,
		DRIVING_BY_TAXI_HOME,
		WAITING_FOR_TAXI,
	}
	
	
	public enum HumanBehaviour{
		DRIVING_BY_BUS,
		WALKING,
	}
	private ObjectInstanceHandle oih;

	private ObjectClassHandle och;

	private HumanState state;
	private HumanBehaviour behaviour;

	private BusStop homeBusStop;

	private BusStop workBusStop;

	private Position position;

	private Position destination;
	
	private volatile boolean collected;

	public Duration HOME_TO_STATION; 

	public Duration WORK_TO_STATION;
	
	public Duration WALK_DIRECTLY;
	
	public  final Duration WORKTIME = Duration.hours(8);
	
	private ArrayList<Position> workway = new ArrayList<Position>();
	
	private int positionIndex = 0;
	
	
	
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
		homeBusStop = home;
		workBusStop = work;
		
		workway.add(new Position(model, "Home", PositionType.HOME));
		
		
		// start at home
		setPosition(home);
		state = HumanState.AT_HOME;
		if(HumanSimValues.WALKING_ENABLED){
		behaviour = HumanBehaviour.values()[new Random().nextInt(2)];
		} else {
			behaviour = HumanBehaviour.DRIVING_BY_BUS;
		}
		
		if(!behaviour.equals(HumanBehaviour.WALKING)) {
			workway.add(home);
			workway.add(work);
		}
		
		workway.add(new Position(model, "Work", PositionType.WORK));
		
		
		if(HumanSimValues.RANDOMIZED_HUMAN_VALUES){
			HOME_TO_STATION = Duration.minutes(new Random().nextInt(60) + 1);
			WORK_TO_STATION = Duration.minutes(new Random().nextInt(60) + 1);
			WALK_DIRECTLY = Duration.minutes(Duration.minutes(new Random().nextInt(200) + 1).value());
		} else {
			HOME_TO_STATION = Duration.minutes(30);
			WORK_TO_STATION = Duration.minutes(30);
			WALK_DIRECTLY = Duration.minutes(90);
		}
		
		
		destination = workway.get(1);
		
		awayFromHomeTimes = new ArrayList<Duration>();
		busWaitingTimes = new ArrayList<Duration>();
		drivingTimes = new ArrayList<Duration>();
		freeTimes = new ArrayList<Duration>();	
		
		String workwaystring = workway.get(0).getName() + "->";

		for(int i = 1; i < workway.size() - 1; i++) {
			workwaystring = workwaystring + workway.get(i).getName() + "->";
		}
		
		workwaystring = workwaystring + workway.get(workway.size()-1).getName();
		
		Utils.log(this, "Person:" + this.getName() + " Path: " + workwaystring);
		
	
	}

	public Human(BusStop home, BusStop work, ISimulationModel model, String name, ObjectInstanceHandle oih, ObjectClassHandle och){
		super(model, name);
		homeBusStop = home;
		workBusStop = work;
		
		// start at home
		setPosition(home);
		state = HumanState.AT_HOME;
		
		behaviour = HumanBehaviour.values()[new Random().nextInt(2)];

		
		destination = workBusStop;
		
		awayFromHomeTimes = new ArrayList<Duration>();
		busWaitingTimes = new ArrayList<Duration>();
		drivingTimes = new ArrayList<Duration>();
		freeTimes = new ArrayList<Duration>();	
			
		this.oih = oih;
		this.och = och;
		
		System.out.println("Person: " + this.getName() + "HomeBS: " + home.getName() + " WorkBS:" + work.getName());
	
	}


	//BusDriving state changes
	
	public void walkToBusStopAtHome() {
		if (behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.AT_HOME))
			state = HumanState.GO_TO_BUSSTOP_HOME;
		else
			throw new IllegalStateException("Human don't want to drive! He will walk!!!"+ " CurrentState: " + this.state.toString());
	}
	
	public void arriveAtBusStopHome() {
		if (behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.GO_TO_BUSSTOP_HOME)){
			state = HumanState.AT_BUSSTOP_HOME;
			setPosition(homeBusStop);
			//arriveAtBusStopWalkingTimePointLog();
		}
		else
			throw new IllegalStateException("Human is lost! At least not at the Bus Stop at home"+ " CurrentState: " + this.state.toString());
	}

	public void driveToBusStopAtWork() {
		if(behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.AT_BUSSTOP_HOME)){
			state = HumanState.DRIVING_TO_WORK;
			setPosition(null);
		}
		else
			throw new IllegalStateException("Human cannot drive to work!"+ " CurrentState: " + this.state.toString());
	}
	
	public void arriveAtBusStopWorkByDriving(){
		if(behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.DRIVING_TO_WORK)){
			state = HumanState.AT_BUSSTOP_WORK;
			setPosition(workBusStop);
		}
		else 
			throw new IllegalStateException("Human cannot arrive at work by car!"+ " CurrentState: " + this.state.toString());
	}
	
	public void walkToWorkFromBusStop() {
		if (behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.AT_BUSSTOP_WORK)){
			state = HumanState.WALK_TO_WORK_FROM_BUSSTOP;
			setPosition(null);
		}
		else
			throw new IllegalStateException("Cannot walk from bus stop to work..."+ " CurrentState: " + this.state.toString());
	}
	
	public void arriveAtWorkBus() {
		if(behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.WALK_TO_WORK_FROM_BUSSTOP)){
			state = HumanState.AT_WORK;
			this.setDestination(this.getHomeBusStop());
		}
		else 
			throw new IllegalStateException("Already at work"+ " CurrentState: " + this.state.toString());
	}
	
	
	public void walkToBusStopAtWork() {
		if (behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.AT_WORK))
			state = HumanState.GO_TO_BUSSTOP_WORK;
		else
			throw new IllegalStateException("Human don't want to drive! He will walk!!!"+ " CurrentState: " + this.state.toString());
	}

	public void arriveAtBusStopWork() {
		if (behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.GO_TO_BUSSTOP_WORK)){
			state = HumanState.AT_BUSSTOP_WORK;
			setPosition(workBusStop);
			//arriveAtBusStopWalkingTimePointLog();
			
		}
		else
			throw new IllegalStateException("Human is lost! At least not at the Bus Stop at work"+ " CurrentState: " + this.state.toString());
	}
	
	public void driveToBusStopAtHome() {
		if(behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.AT_BUSSTOP_WORK)){
			state = HumanState.DRIVING_HOME;
			setPosition(null);
		}
		else
			throw new IllegalStateException("Human cannot drive home!"+ " CurrentState: " + this.state.toString());
	}
	
	public void arriveAtBusStopHomeByDriving(){
		if(behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.DRIVING_HOME)){
			state = HumanState.AT_BUSSTOP_HOME;
			setPosition(homeBusStop);
		}
		else 
			throw new IllegalStateException("Human cannot arrive at work by car!"+ " CurrentState: " + this.state.toString());
	}

	public void walkHomeFromBusStop() {
		if (behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.AT_BUSSTOP_HOME)){
			state = HumanState.WALK_HOME_FROM_BUSSTOP;
			setPosition(null);
		}
		else
			throw new IllegalStateException("Cannot walk home from bus stop..."+ " CurrentState: " + this.state.toString());
	}
	
	public void arriveHomeBus() {
		if(behaviour.equals(HumanBehaviour.DRIVING_BY_BUS) && state.equals(HumanState.WALK_HOME_FROM_BUSSTOP)){
			state = HumanState.AT_HOME;
			this.setDestination(workBusStop);
		}
		else 
			throw new IllegalStateException("Already at home"+ " CurrentState: " + this.state.toString());
	}
	
	
	
	//Walking state changes
	
	
	public void arriveAtWorkDirectlyWalking(){
		if(behaviour.equals(HumanBehaviour.WALKING) && state.equals(HumanState.WALKING_DIRECTLY_TO_WORK))
			state = HumanState.AT_WORK;
		else 
			throw new IllegalStateException("Human is not walking!!!"+ " CurrentState: " + this.state.toString());
	}
	
	public void arriveAtHomeDirectlyWalking(){
		if(behaviour.equals(HumanBehaviour.WALKING) && state.equals(HumanState.WALKING_DIRECTLY_HOME))
			state = HumanState.AT_HOME;
		else 
			throw new IllegalStateException("Human is not walking!!!"+ " CurrentState: " + this.state.toString());
	}
	
	public void walkToWorkDirectly(){
		if(behaviour.equals(HumanBehaviour.WALKING) && state.equals(HumanState.AT_HOME))
			state = HumanState.WALKING_DIRECTLY_TO_WORK;
		else 
			throw new IllegalStateException("Human is lost, but not at home" + " CurrentState: " + this.state.toString());
	}
	
	public void walkHomeDirectly(){
		if(behaviour.equals(HumanBehaviour.WALKING) && state.equals(HumanState.AT_WORK))
			state = HumanState.WALKING_DIRECTLY_HOME;
		else 
			throw new IllegalStateException("Human is lost, but not at work" + " CurrentState: " + this.state.toString() );
	}
	
	//Taxi state change
	
	




	
	public ArrayList<Duration> getFreeTimes(){
		return freeTimes;
	}
	
	public Position getPosition(){
		return this.position;
	}
	
	public void setPosition(BusStop position){
		this.position = position;
	}
	
	public Position getDestination(){
		return this.destination;
	}
	
	public void setDestination(BusStop destination){
		this.destination = destination;
	}
	
	public BusStop getHomeBusStop(){
		return this.homeBusStop;
	}
	
	public BusStop getWorkBusStop(){
		return this.workBusStop;
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

	public Duration getTimeDriven() {
		return timeDriven;
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
	
	public void setTimeDriven(Duration timeDriven) {
		this.timeDriven = timeDriven;
	}
	
	public boolean willWalk(){
		return behaviour.equals(HumanBehaviour.WALKING);
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
		//Utils.log(this, "Caluclated New Waitingtime: " + timeWaitedAtBusStop.toSeconds().value() );
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
		//System.out.println("Human" + getName() + "New Time Driven" + timeDrivenEvent.toSeconds().value() + " at time " + getModel().getSimulationControl().getCurrentSimulationTime());
		//Utils.log(this, "Caluclated New Drivingtime: " + timeDrivenEvent.toSeconds().value() );
	}
	
	
	public void calculateFreeTime(){
		Duration onTheWay = Duration.seconds(0);
		if(behaviour.equals(HumanBehaviour.WALKING)){
			onTheWay = Duration.seconds(WORKTIME.toSeconds().value() + 2*WALK_DIRECTLY.toSeconds().value()); 
		} else if (behaviour.equals(HumanBehaviour.DRIVING_BY_BUS)){
			onTheWay = Duration.seconds(WORKTIME.toSeconds().value() + 2*HOME_TO_STATION.toSeconds().value() + 2* WORK_TO_STATION.toSeconds().value() + timeDriven.toSeconds().value() + timeWaitedAtBusStop.value());
		}

			

		
		
		//Utils.log(this, "On the way:" + onTheWay.toHours().value() + " hours. Waited " + timeWaitedAtBusStop.toMinutes().value() + " minutes at bus stops");
		
		
		
		
		
		Duration newDrivingTime = Duration.seconds(timeDriven.toSeconds().value());
		Duration newWaitingTime = Duration.seconds(timePointAtBusStop);
		
		busWaitingTimes.add(timeWaitedAtBusStop);
		drivingTimes.add(timeDriven);
		awayFromHomeTimes.add(onTheWay);

		
		
		double total= 24 - onTheWay.toHours().value();
		FREETIME = Duration.hours(total);
		freeTimes.add(FREETIME);
		//System.out.println(FREETIME.toSeconds().value());
		if(FREETIME.toSeconds().value() < 0.0){
//			System.out.println(this.getName());
//			System.out.println(timeWaitedAtBusStop.toSeconds().value());
//			System.out.println(timeDriven.toSeconds().value());
//			System.out.println(onTheWay.toSeconds().value());
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
		this.positionIndex++;
		this.destination = this.workway.get(positionIndex);
		
		return this.position;
		
	}
	

}