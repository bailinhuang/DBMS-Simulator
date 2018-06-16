package Modules;

import java.util.PriorityQueue;

public class ExecutorModule extends Module {


    ExecutorModule(Simulator simulator, RandomValueGenerator randSimulator, int numProcesses) {
        super(simulator, randSimulator);
        this.numberServers = numProcesses;
    }

    @Override
    public void processArrival(Event event) {
        System.out.println("Llega cliente al modulo 5 -> "+event.getTimeClock());
        if(busyServers < numberServers){
            processClient(event);
        }else{
            queue.offer(event);
        }
    }

    @Override
    public void processClient(Event event) {
        ++busyServers;
        event.setTimeClock(event.getTimeClock()+getServiceTime(event));
        //System.out.println("Tiempo servicio -> "+event.getTimeClock()+"\n");
        //Output is generated
        event.setEventType(EventType.DEPARTURE);
        this.simulator.addEvent(event);
    }

    @Override
    public double getServiceTime(Event event) {

        double numBlocks = event.getQuery().getNumberOfBlocks();

        //B^2 milliseconds = (B^2)/1000  seconds
        double timeTemp = (numBlocks * numBlocks)/1000.0;

        switch (event.getQuery().getType()){
            case DDL:
                //Update database schema 1/2 second
                timeTemp += 0.5;
                break;
            case UPDATE:
                //Update database schema 1 second
                timeTemp += 1.0;
                break;
            case SELECT:
            case JOIN:
                break;
        }
        //transmission time R = numbers of blocks
        timeTemp += event.getQuery().getNumberOfBlocks();

        return timeTemp;
    }


    @Override
    public void processDeparture(Event event) {
        System.out.println("Sale cliente al modulo 4 -> "+event.getTimeClock()+"\n\n");
        --busyServers;

        //Exit to the next event
        //event.setCurrentModule(simulator.getTransactionalStorageModule());
        event.setCurrentModule(simulator.getClientCommunicationsManagerModule());
        event.setEventType(EventType.DEPARTURE);
        this.simulator.addEvent(event);

        if(queue.size()>0)
        {
            Event temporal = queue.poll();
            processClient(temporal);
        }


    }




}
