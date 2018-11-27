package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;


public class HumanSimValues {
	public final static int NUM_BUSSTOPS = 6;
	public final static int NUM_HUMANS = 10000;
	public final static Duration MAX_SIM_TIME = Duration.hours(48);
	public static final String READY_TO_RUN = "ReadyToRun";
	public static final boolean STOCHASTIC = false;
	public final static boolean WORKLOAD_OPEN = true;
	public final static Duration interarrivalTime = Duration.seconds(40);
}
