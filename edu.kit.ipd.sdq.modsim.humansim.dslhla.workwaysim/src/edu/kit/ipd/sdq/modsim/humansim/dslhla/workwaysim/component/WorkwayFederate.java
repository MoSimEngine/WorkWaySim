package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Random;

import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEntityDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.AbstractSimEventDelegator;
import de.uka.ipd.sdq.simulation.abstractsimengine.SimulationElement;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.BusStop;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Human;
import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.util.Utils;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

public class WorkwayFederate{

	

	
	private RTIambassador rtiamb;
	public WorkwayFederateAmbassador fedamb; // created when we connect
	public HLAfloat64TimeFactory timeFactory; // set when we join
	protected EncoderFactory encoderFactory; // set when we join
	
	protected InteractionClassHandle registerAtBusStopHandle;
	protected InteractionClassHandle humanEntersBusHandle;
	protected InteractionClassHandle humanExitsBusHandle;
	protected InteractionClassHandle busStopReadyHandle;
	
	protected ParameterHandle humanNameExitBusHandle;
	protected ParameterHandle humanNameEnterBusHandle;
	protected ParameterHandle humanNameRegisterHandle;
	protected ParameterHandle busStopNameRegisterHandle;
	protected ParameterHandle busStopNameExitHandle;
	protected ParameterHandle busStopNameEnterHandle;
	
	protected ObjectClassHandle humanObjectClassHandle;
	protected AttributeHandle collectedHandle;
	protected AttributeHandle humanNameAttributeHandle;
	protected AttributeHandle destinationHandle;
	
	protected ObjectClassHandle busStopObjectClassHandle;
	protected AttributeHandle busStopNameAttributeHandle;
	
	protected ObjectInstanceHandle busObjectHandles;

	
	
	
	String fedName;
	String humanName;
	
	private final String fedInfoStr =  "!Workway Federate-Info!: ";
	private String federateName;
	
	
	public double executionTime = 0.0;
	public boolean finished = false;
	public boolean finishedCounted = false;
	private boolean isFedInit = false;
	private WorkwayModel simulation;
	
	
	double startTime;

	protected InteractionClassHandle humanReadyHandle;

	protected InteractionClassHandle busSimReadyHandle;
	
	protected int busStopInitialised = 0;
	
	protected boolean busReady = false;
	
	private boolean regulateTime = false;
	private boolean constrainTime = false;
	
	public LinkedList<ObjectInstanceHandle> busStopHandles;
	
	
	public WorkwayFederate(WorkwayModel simulation){
		this.simulation = simulation;
		
		busStopHandles = new LinkedList<ObjectInstanceHandle>();
	}
	 
	
	public void runFederate(String fedName) throws Exception{

		this.federateName = fedName;
//		log(fedInfoStr + "Creating RTIambassador");
		rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

	
		
//		log(fedInfoStr + "Connecting");
		fedamb = new WorkwayFederateAmbassador(this);
		rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);
		
//		log(fedInfoStr + "Creating Federation");
//		try {
//			URL[] modules = new URL[] { (new File("FOMS/HumanSimFOM.xml")).toURI().toURL() };
//
//			rtiamb.createFederationExecution("HumanSim", modules);
////			log(fedInfoStr + "Created Federation");
//		} catch (FederationExecutionAlreadyExists exists) {
////			log(fedInfoStr + "Didn't create federation, it already existed");
//		} catch (MalformedURLException urle) {
//			log(fedInfoStr + "Exception loading one of the FOM modules from disk: " + urle.getMessage());
//			urle.printStackTrace();
//			return;
//		}
//		
		URL[] joinModules = new URL[] { (new File("FOMS/HumanSimFOM.xml")).toURI().toURL() };
		rtiamb.joinFederationExecution(federateName, "HumanSim1", "HumanSim1", joinModules);
		
		
//		log(fedInfoStr + "Joined fedration as " + federateName);
		

		this.timeFactory = (HLAfloat64TimeFactory) rtiamb.getTimeFactory();

		rtiamb.registerFederationSynchronizationPoint(HumanSimValues.READY_TO_RUN, null);
	
		
		while (fedamb.isAnnounced == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}
		
		rtiamb.synchronizationPointAchieved(HumanSimValues.READY_TO_RUN);

//		log(fedInfoStr + "Before Time Policy Enable");

				
		
		
		
		regulateTime = true;
		constrainTime = true;
		runTimePolicyEnabling();
		
		rtiamb.enableCallbacks();
		
		while (fedamb.isReadyToRun == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}
		//log(fedInfoStr + "Time Policy Enabled");
		
		publishAndSubscribe();
		
		//log(fedInfoStr + "Published and Subscribed");
		
	
		
	
		
		while(simulation.getStops().size() != HumanSimValues.NUM_BUSSTOPS){
			advanceTime(1.0);
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
		}
		
			initialiseHuman();
			
			//divestCollectedOwnership();
			
			for (BusStop bs : simulation.getStops()) {
				log("BusStop: " + bs.getName());
			}
			
			
			simulation.startSimulation();
			
		}
	
	
	public void endExecution() throws Exception{
		
		rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
		log("Resigned from Federatin");
		
		try{
			rtiamb.destroyFederationExecution("HumanSim1");
			log("Destroyed HumanSim federation");
		} catch (FederationExecutionDoesNotExist fedne){
			log(" Federation does not exist");
		} catch (FederatesCurrentlyJoined fcj) {
			log("Federates still joined at HumanSim");
		}
	}

	
	private void enableTimePolicy() throws Exception {
		
		if(regulateTime){
		HLAfloat64Interval lookahead = timeFactory.makeInterval(fedamb.federateLookahead);
		this.rtiamb.enableTimeRegulation(lookahead);

		while (fedamb.isRegulating == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			}
		
		//log(fedInfoStr + "activated time regulation");
		}
		
		if(constrainTime){
		this.rtiamb.enableTimeConstrained();
			
		while (fedamb.isConstrained == false) {
			rtiamb.evokeMultipleCallbacks(0.1, 0.2);
			}
		//log(fedInfoStr + "activated time contrained");
		}
	}
	
	
	private void runTimePolicyEnabling() throws Exception{
		
		if(regulateTime && constrainTime){
			while(!fedamb.isConstrained && !fedamb.isRegulating){
				try{
				enableTimePolicy();
				} catch (Exception e){
					log(e.getMessage());
				}
			}
		} else if (regulateTime){
			while(!fedamb.isRegulating){
				try{
					enableTimePolicy();
					} catch (Exception e){
						log(e.getMessage());
					}
				}
			
		} else if (constrainTime){
			while(!fedamb.isConstrained){
				try{
					enableTimePolicy();
					} catch (Exception e){
						log(e.getMessage());
					}
				}
		} else {
			log("No time policy to enable");
		}
		
		
	}


	private void divestCollectedOwnership() throws Exception{
		
		AttributeHandleSet handles = rtiamb.getAttributeHandleSetFactory().create();
		handles.add(collectedHandle);
		for (Human human : simulation.getHumans()) {
			rtiamb.unconditionalAttributeOwnershipDivestiture(human.getOih(), handles);
		}
		
	}
	
	private void publishAndSubscribe() throws RTIexception {
		registerAtBusStopHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanRegistersAtBusStop");
		rtiamb.publishInteractionClass(registerAtBusStopHandle);

		humanEntersBusHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanEntersBus");
		rtiamb.subscribeInteractionClass(humanEntersBusHandle);
		
		humanExitsBusHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.HumanExitsBus");
		rtiamb.subscribeInteractionClass(humanExitsBusHandle);
		
			
		humanNameEnterBusHandle = rtiamb.getParameterHandle(humanEntersBusHandle, "HumanName");
		humanNameExitBusHandle = rtiamb.getParameterHandle(humanExitsBusHandle, "HumanName");
		humanNameRegisterHandle = rtiamb.getParameterHandle(registerAtBusStopHandle, "HumanName");
		
		busStopNameEnterHandle = rtiamb.getParameterHandle(humanEntersBusHandle, "BusStopName");
		busStopNameExitHandle = rtiamb.getParameterHandle(humanExitsBusHandle, "BusStopName");
		busStopNameRegisterHandle = rtiamb.getParameterHandle(registerAtBusStopHandle, "BusStopName");
		
		humanObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Human");
		humanNameAttributeHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "HumanName");
		collectedHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "HumanCollected");
		destinationHandle = rtiamb.getAttributeHandle(humanObjectClassHandle, "Destination");
		
		busStopObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.BusStop");
		busStopNameAttributeHandle = rtiamb.getAttributeHandle(busStopObjectClassHandle, "BusStopName");
		
		
		AttributeHandleSet humanSubscribedAttributes = rtiamb.getAttributeHandleSetFactory().create();
		humanSubscribedAttributes.add(collectedHandle);
	
		
		AttributeHandleSet humanPublishedAttributes = rtiamb.getAttributeHandleSetFactory().create();
		humanPublishedAttributes.add(humanNameAttributeHandle);
		humanPublishedAttributes.add(destinationHandle);

		
		
		
		rtiamb.publishObjectClassAttributes(humanObjectClassHandle, humanPublishedAttributes);
		rtiamb.subscribeObjectClassAttributes(humanObjectClassHandle, humanSubscribedAttributes);
//		
		busStopObjectClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.BusStop");
		busStopNameAttributeHandle = rtiamb.getAttributeHandle(busStopObjectClassHandle, "BusStopName");
		
//		
		AttributeHandleSet busStopAttributes = rtiamb.getAttributeHandleSetFactory().create();
		busStopAttributes.add(busStopNameAttributeHandle);
		rtiamb.subscribeObjectClassAttributes(busStopObjectClassHandle, busStopAttributes);
		
	}
	
	private ObjectInstanceHandle registerHumanObject() throws RTIexception{
		return rtiamb.registerObjectInstance(humanObjectClassHandle);
	}
	
	private byte[] generateTag() {
		return ("(timestamp) " + System.currentTimeMillis()).getBytes();
	}
	
	

	/**
	 * This method will request a time advance to the current time, plus the given
	 * timestep. It will then wait until a notification of the time advance grant
	 * has been received.
	 */
	public synchronized boolean advanceTime( double timestep ) throws RTIexception
	{
		
		double advancingTo = 0;
		double miniStep = 0.000000001;
		if(fedamb.federateTime + timestep <= HumanSimValues.MAX_SIM_TIME.toSeconds().value()){
			advancingTo = fedamb.federateTime + timestep;
		} else {
			Utils.log(simulation.getHuman(), "Sim overtime - wants to advance to: " + fedamb.federateTime + timestep + " current time: " + fedamb.federateTime);
			advancingTo =  HumanSimValues.MAX_SIM_TIME.toSeconds().value() + miniStep;
			return false;
		}
		// request the advance
		fedamb.isAdvancing = true;
		HLAfloat64Time time = timeFactory.makeTime( advancingTo );
		if(HumanSimValues.MESSAGE){
			try{
			rtiamb.nextMessageRequest( time );
			} catch (Exception e){
				log(e.getMessage());
				return false;
			}
			} else {
				try{
					rtiamb.timeAdvanceRequest( time );
					} catch (Exception e){
						log(e.getMessage());
						return false;
					}
			}
			
	
		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while( fedamb.isAdvancing )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
		}
//		System.out.println("New Fed Time: " + fedamb.federateTime);
		return true;
	}
	
	
	public synchronized void  synchronisedAdvancedTime(double timestep, AbstractSimEventDelegator simevent, AbstractSimEntityDelegator simentity ){
//		System.out.println("AbstractSimEngine Time:" + simulation.getSimulationControl().getCurrentSimulationTime());
//		System.out.println("Federate Time:" + fedamb.federateTime);
//		System.out.println("TimeStep:" + timestep);
//		
		double abstractSimEngineTime = simulation.getSimulationControl().getCurrentSimulationTime();
		double simEngineNextTime = abstractSimEngineTime  + timestep;
		double timeDiffFedTimeSETime = getCurrentFedTime() - abstractSimEngineTime;
		double realTimeStep = 0.0;
//		try {
//			advanceTime(timestep);
//		} catch (RTIexception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(timestep != 0.0){
//			while(simEngineNextTime > getCurrentFedTime()){
//				realTimeStep = simEngineNextTime - getCurrentFedTime();
//				try {
//					if(!advanceTime(realTimeStep)){
//						
//						System.out.println("Not Advancing Time");
//						//simulation.getSimulationControl().stop();
//						return;
//					}
//				} catch (RTIexception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//		}
		if(timestep != 0.0){
			
			try {
				if(!advanceTime(timestep)){
					
					System.out.println("Not Advancing Time");
				simulation.getSimulationControl().stop();
					return;
				}
			} catch (RTIexception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	
}
		
		simevent.schedule(simentity, timestep);
	}
	
	public void sendRegisterInteraction(Human human, BusStop busStop) throws RTIexception{
		
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
		HLAASCIIstring humanName = encoderFactory.createHLAASCIIstring(human.getName());
		HLAASCIIstring busStopName = encoderFactory.createHLAASCIIstring(busStop.getName());
		parameters.put(humanNameRegisterHandle, humanName.toByteArray());
		parameters.put(busStopNameRegisterHandle, busStopName.toByteArray());
		HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 1.0);

		//log(human, "Sending RegisterAction for Human:" + human.getName() + " to BusStop: " + busStop.getName());
		rtiamb.sendInteraction( registerAtBusStopHandle, parameters, generateTag(), time);
		
	}
	
	public void initialiseHuman() throws Exception{
	
		simulation.initialiseHumans();
		
		Human human = simulation.getHuman();

//		System.out.println("Initialising: " + human.getName());
		ObjectInstanceHandle oih = registerHumanObject();
		
		human.setOih(oih);
		human.setOch(humanObjectClassHandle);
//		System.out.println("Set Handles for: " + human.getName());
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
		HLAASCIIstring humanName = encoderFactory.createHLAASCIIstring(human.getName());
		attributes.put(humanNameAttributeHandle, humanName.toByteArray());
		HLAASCIIstring destinationString = encoderFactory.createHLAASCIIstring(human.getDestination().getName());
		attributes.put(destinationHandle, destinationString.toByteArray());
		HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 1.0);
//		System.out.print("Updating Values");
		rtiamb.updateAttributeValues(human.getOih(), attributes, generateTag(), time);
		
		
	}
	
	public void changeDestinationAttribute(Human human, BusStop destination) throws RTIexception{
		
		//log(human, "Changing destination to: " + destination.getName());
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
		
		HLAASCIIstring destinationString = encoderFactory.createHLAASCIIstring(destination.getName());
		attributes.put(destinationHandle, destinationString.toByteArray());
		HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + 1.0);
	
		rtiamb.updateAttributeValues(human.getOih(), attributes, generateTag(), time);
		
	}
	
	public double getCurrentFedTime(){
		return fedamb.federateTime;
	}
	
	public boolean timeOver(){
		
		if(fedamb == null){
			return false;
		}
		
		//log("Current FedTime:" + fedamb.federateTime);
		//log("Max sim time:" + maxSimTime.toSeconds().value());	
		if(fedamb.federateTime > HumanSimValues.MAX_SIM_TIME.toSeconds().value())
			return true;
		else
			return false;
	}
	
	public void log(String msg){
		 StringBuilder s = new StringBuilder();
    	 s.append(msg);
    	 System.out.println(s);
	}
	

	
	public void handleHumanEntersBusInteraction(String humanName, String busStopName) throws Exception{
		log("HandlingEnterInteraction");
		if(humanName.equals("") || busStopName.equals("")){
			log(fedInfoStr + " ERROR: Human or Bus empty");
		}
		
		simulation.scheduleHumanEntersEvent(humanName, busStopName);
	
	}
	
	
	public void handleHumanExitsBusInteraction(String humanName, String busStopName) throws Exception{
		log("HandlingExitInteraction");
		if(humanName.equals("") || busStopName.equals("")){
			log(fedInfoStr + " ERROR: Human or Bus empty");
		}
		
		simulation.scheduleHumanExitsEvent(humanName, busStopName);
	}
	
	public void handleBusStopAttributeUpdates(BusStop busStop, AttributeHandleValueMap attributes, ObjectInstanceHandle oih){

		String busStopName = "";
	
		//log("Received BusStop attribute updates");
		
		
		for(AttributeHandle handle : attributes.keySet()){
			if(handle.equals(busStopNameAttributeHandle)){
				busStopName = fedamb.decodeStringValues(attributes.get(busStopNameAttributeHandle));
			} else {
				log("Got more than expected");
			}
		}
		
		
		if(busStopName.equals("")){
			log(fedInfoStr + " ERROR: got empty name");
		} else {
			
			
			System.out.println("Got new busstop name, dont know what to do" );
			return;
		}
		
		
		
		log(fedInfoStr + "No corresponding busStop to handle found");
	
	}
	
	public void waitForUser() {
		log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			reader.readLine();
		} catch (Exception e) {
			log("Error while waiting for user input: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void handleHumanAttributesUpdate(Human human, AttributeHandleValueMap attributes) throws Exception{
		
		String collected = "";
		
		for(AttributeHandle handle : attributes.keySet()){
			if(handle.equals(collectedHandle)){
				collected = fedamb.decodeBoolean(attributes.get(collectedHandle));
			} else {
				log("Got more attributes than expected");
			}
		}
		//System.out.println("Received Collected at " + "Current time:" + fedamb.federateTime + "SimTime: " + simulation.getSimulationControl().getCurrentSimulationTime());
		//Utils.log(human, "Setting Collected to " + collected);
		switch (collected) {
		case "True":
			human.setCollected(true);
			//log(human, "setting achieved");
			break;
		case "False":
			human.setCollected(false);
			//log(human, "setting achieved");
			break;
		default:
			log(fedInfoStr + " ERROR: got faulty boolean");
		}
		
		
		//log(fedInfoStr + "No corresponding busStop to handle found");
	}
	
	
	public void handleAttributeUpdate(ObjectInstanceHandle oih, AttributeHandleValueMap attributes) throws Exception{

		
		//log("Got Update Handle:" + oih.toString());
		if(simulation.getHuman() != null && simulation.getHuman().getOih().equals(oih)) {
				handleHumanAttributesUpdate(simulation.getHuman(), attributes);
				return;
			}

		
		for (BusStop busStop : simulation.getStops()) {
			if(busStop.getOih().equals(oih)){
				//log("Handle busStop attribute" + oih.toString());
				handleBusStopAttributeUpdates(busStop, attributes, oih);
				return;
			}
		}
		
		for (ObjectInstanceHandle busStopHandles : busStopHandles){
			if(busStopHandles.equals(oih)){
				addBusStop(oih, attributes);
			}
		}
		
	}
	
	public void addBusStop(ObjectInstanceHandle oih, AttributeHandleValueMap attributes){
		
		String busStopName = "";
		
		//log("Received BusStop attribute updates");
		
		
		for(AttributeHandle handle : attributes.keySet()){
			if(handle.equals(busStopNameAttributeHandle)){
				busStopName = fedamb.decodeStringValues(attributes.get(busStopNameAttributeHandle));
			} else {
				log("Got more than expected");
			}
		}
		
		BusStop bs = new BusStop(simulation, busStopName);
		bs.setOih(oih);
		bs.setOch(busStopObjectClassHandle);
		simulation.addBusStop(bs);
		//log("Created BusStop: " + bs.getName());
		busStopInitialised++;
	}
	
	public void addBusStopHandle(ObjectInstanceHandle oih){
		busStopHandles.add(oih);
	}
	
	public RTIambassador getRTIAmb(){
		return rtiamb;
	}
}
 