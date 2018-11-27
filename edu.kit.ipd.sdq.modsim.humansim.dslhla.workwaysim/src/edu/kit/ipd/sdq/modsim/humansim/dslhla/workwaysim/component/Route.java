package edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.component;

import java.util.HashMap;
import java.util.Map;

import edu.kit.ipd.sdq.modsim.humansim.dslhla.workwaysim.entities.Queue;


public class Route {

    private Map<Queue, RouteSegment> segmentMap;

    public Route() {
        this.segmentMap = new HashMap<Queue, RouteSegment>();
    }

    public void addSegment(Queue from, Queue to, int distance, int averageSpeed) {
        if (this.segmentMap.containsKey(from)) {
            throw new IllegalStateException("There is already a segement originating from bus stop " + from);
        }

        RouteSegment s = new RouteSegment(from, to, distance, averageSpeed);
        this.segmentMap.put(from, s);
    }

    // public BusStop nextStop(BusStop from) {
    // return getRouteSegment(from).getTo();
    // }
    //
    // public int distanceToNextStop(BusStop from) {
    // return getRouteSegment(from).getDistance();
    // }
    //
    // public int averageSpeedToNextStop(BusStop from) {
    // return getRouteSegment(from).getAverageSpeed();
    // }

    public RouteSegment getRouteSegment(Queue from) {
        if (!this.segmentMap.containsKey(from)) {
            throw new IllegalStateException("There is no segment originating from bus stop " + from);
        }

        return this.segmentMap.get(from);
    }

    public class RouteSegment {

        private Queue from, to;

        // distance in kilometers
        private int distance;

        // average speed in kilometers per hour
        private int averageSpeed;

        public RouteSegment(Queue from, Queue to, int distance, int averageSpeed) {
            this.from = from;
            this.to = to;
            this.distance = distance;
            this.averageSpeed = averageSpeed;
        }

        public Queue getFrom() {
            return from;
        }

        public Queue getTo() {
            return to;
        }

        public int getDistance() {
            return distance;
        }

        public int getAverageSpeed() {
            return averageSpeed;
        }

    }

}
