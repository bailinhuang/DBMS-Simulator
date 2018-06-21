package main.java.Modules;

import java.util.PriorityQueue;

public class QueryProcessorModule extends Module {

    //Capacity for n processes
    QueryProcessorModule(Simulator simulator, RandomValueGenerator randSimulator, int numProcesses) {
        super(simulator, randSimulator);
        this.numberServers = numProcesses;
        this.queue = new PriorityQueue<>(new ComparatorNormalEvent());
    }


    //TODO ver la posibilidad de tener este metodo en la clase padre
    @Override
    public void processArrival(Event event) {
        //main.java.Statistics
        event.getQuery().getQueryStatistics().setArrivalTimeModule(this.simulator.getClockTime());
        this.statisticsOfModule.increaseTotalQueueSize(this.queue.size());

        //System.out.println("Llega cliente al modulo 3 -> "+event.getTimeClock());
        if(busyServers < numberServers){
            processClient(event);
            //System.out.println("Tiempo servicio -> "+event.getTimeClock()+"\n");
        }else{
            queue.offer(event);
        }
    }

    @Override
    public void processClient(Event event) {
        ++busyServers;
        event.setTimeClock(event.getTimeClock()+getServiceTime(event));
        //Output is generated
        event.setEventType(EventType.DEPARTURE);
        this.simulator.addEvent(event);
    }


    @Override
    public double getServiceTime(Event event) {
        double processingTime = 0.0;
        //lexical validation
        processingTime += 0.1;

        //syntactic validation --> uniform random value 0<=x<=1 seconds
        processingTime += this.randomValueGenerator.generateUniformDistributionValue(0,1);

        //semantic validation --> uniform random value 0<=x<=2 seconds
        processingTime += this.randomValueGenerator.generateUniformDistributionValue(0,2);

        //permit verification --> exponential random value with mean 0.7 seconds -> 0.7 = 1/lambda -> lambda = 1/0.7
        processingTime+= this.randomValueGenerator.generateExponentialDistributionValue(1.0/0.7);

        //TODO definir si realmente la consulta debe saber cuanto dura para que la optimizen.
        //query optimization
        processingTime += event.getQuery().getTimeOptimization();

        return  processingTime;
    }


    //TODO definir si seguimos el algoritmo de clase o se deja este.
    @Override
    public void processDeparture(Event event) {
        //System.out.println("Sale cliente al modulo 3 -> "+event.getTimeClock()+"\n\n");
        //Exit to the next event
        --busyServers;

        //main.java.Statistics
        event.getQuery().getQueryStatistics().setDepartureTime(this.simulator.getClockTime());


        if (!this.simulator.isTimeOut(event)) {
            //Exit to the next event
            event.setCurrentModule(simulator.getTransactionalStorageModule());
            event.setEventType(EventType.ARRIVAL);
            this.simulator.addEvent(event);
        }

        boolean noTimeOut = false;
        while (this.queue.size()>0 && !noTimeOut){
            Event temporal = this.queue.poll();
            if(!this.simulator.isTimeOut(event)){
                processClient(temporal);
                noTimeOut = true;
            }
        }

        //main.java.Statistics
        event.getQuery().getQueryStatistics().setDepartureTime(this.simulator.getClockTime());
        this.statisticsOfModule.increaseNumberOfQuery(event.getQuery().getType());
        this.statisticsOfModule.increaseTimeOfQuery(event.getQuery().getType(),event.getQuery().getQueryStatistics().getArrivalTimeModule(),this.simulator.getClockTime());


    }

}