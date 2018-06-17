package Modules;

import java.util.PriorityQueue;

public class ClientCommunicationsManagerModule extends Module {

    //Receive k connections
    ClientCommunicationsManagerModule(Simulator simulator, RandomValueGenerator randSimulator, int numConnections) {
        super(simulator, randSimulator);
        this.numberServers = numConnections;
        this.queue = new PriorityQueue<>(new ComparatorNormalEvent());
    }


    @Override
    public void processArrival(Event event) {
        ++this.simulator.llegan;
        //System.out.println("Llega cliente al modulo 1 -> "+event.getTimeClock());

        //Statistics
        event.getQuery().getQueryStatistics().setArrivalTime(this.simulator.getClockTime());
        this.statisticsOfModule.increaseTotalQueueSize(this.queue.size());

        if(this.busyServers < numberServers){
            processClient(event);
            //System.out.println("Tiempo servicio -> "+event.getTimeClock()+"\n");
        }else
        {
            this.simulator.increaseRejectQueries();
        }
        //A new arrival is generated
        this.simulator.generateNewEvent();
    }

    @Override
    public void processClient(Event event) {
        ++busyServers;
        event.setCurrentModule(simulator.getProcessManagerModule());
        event.setEventType(EventType.ARRIVAL);
        this.simulator.addEvent(event);
    }

    @Override
    public double getServiceTime(Event event) { return 0.0; }

    public void processReturn(Event event){
        //System.out.println("Regresa cliente"+ event.getTimeClock());

        //Statistics
        event.getQuery().getQueryStatistics().setArrivalTimeModule(this.simulator.getClockTime());

        //transmission time R = numbers of blocks
        double timeTemp = event.getQuery().getNumberOfBlocks();

        event.setTimeClock(event.getTimeClock()+timeTemp);
        event.setEventType(EventType.DEPARTURE);
        this.simulator.addEvent(event);
    }


    @Override
    public void processDeparture(Event event) {
       // System.out.println("\033[36m\n####################\nCliente atendido"+ event.getTimeClock()+"\n####################");

        ++this.simulator.numClientes;
        --busyServers;

        //Statistics
        event.getQuery().getQueryStatistics().setDepartureTime(this.simulator.getClockTime());
        this.statisticsOfModule.increaseNumberOfQuery(event.getQuery().getType());
        this.statisticsOfModule.increaseTimeOfQuery(event.getQuery().getType(),event.getQuery().getQueryStatistics().getArrivalTimeModule(),this.simulator.getClockTime());
    }

    @Override
    public void processEvent(Event event) {
        switch (event.getEventType()){
            case ARRIVAL: processArrival(event);
                break;
            case DEPARTURE: processDeparture(event);
                break;
            case RETURN: processReturn(event);
                break;
                default:
                    System.out.println("Error, processEvent");
                    break;
        }
    }
}
