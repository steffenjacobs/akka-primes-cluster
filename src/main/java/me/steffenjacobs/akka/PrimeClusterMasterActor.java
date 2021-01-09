package me.steffenjacobs.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PrimeClusterMasterActor extends AbstractActor {
    public static final String TOPIC_WORKERS = "topic-workers";

    private static final StringMessage MSG_PRIMES_GENERATED = new StringMessage("primes-generated");
    public static final StringMessage MSG_WORK_AVAILABLE = new StringMessage("work-available");
    public static final StringMessage MSG_GIVE_WORK = new StringMessage("give-work");

    private static final int INTERVAL_SIZE = 50000;
    private static final int SEGMENT_COUNT = 500;

    private final ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    private List<SegmentMessage> availableSegments = new ArrayList<>();

    private final List<Long> primeResults = new ArrayList<>();
    private int resultCount = 0;
    private long lastIntervalStart = 0;

    public PrimeClusterMasterActor() {
        schedulePrimeCalculation();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals(MSG_PRIMES_GENERATED, msg -> {
            System.out.println("[Master] Scheduled wake-up!");
            mediator.tell(new DistributedPubSubMediator.Publish(TOPIC_WORKERS, MSG_WORK_AVAILABLE), self());
            schedulePrimeCalculation();
        }).matchEquals(MSG_GIVE_WORK, msg -> {
            // give worker a segment if available
            if (!availableSegments.isEmpty()) {
                sender().tell(availableSegments.remove(0), self());
            }
        }).match(PrimeResult.class, msg -> {
            handlePrimeResultReceived(msg);
            // give worker another segment if available
            if (!availableSegments.isEmpty()) {
                sender().tell(availableSegments.remove(0), self());
            }
            getContext().unwatch(sender());
        }).match(Terminated.class, msg -> System.out.printf("Active worker crashed: %s%n", msg.getActor())
        ).build();
    }

    private void handlePrimeResultReceived(PrimeResult msg) {
        primeResults.addAll(msg.getResults());
        System.out.printf("Received %s primes. Progress: %s%n", msg.getResults().size(), (double)resultCount/SEGMENT_COUNT);

        // check, if all subsets were received
        if (++resultCount >= SEGMENT_COUNT) {
            Collections.sort(primeResults);

            //Write to output.txt
            System.out.printf("Size: %s%n", primeResults.size());
            primeResults.forEach(prime -> System.out.print(prime + ", "));

            System.out.println("Done.");
            this.getContext().system().terminate();
        }
    }

    private List<SegmentMessage> createSegments() {
        List<SegmentMessage> segments = new ArrayList<>();
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            segments.add(new SegmentMessage(lastIntervalStart, lastIntervalStart + INTERVAL_SIZE));
            lastIntervalStart += INTERVAL_SIZE;
        }
        return segments;
    }

    private void schedulePrimeCalculation() {
        if (!availableSegments.isEmpty()) {
            System.err.printf("Remaining segments: %s - No new segments were generated!%n", availableSegments.size());
        } else {
            availableSegments.addAll(createSegments());
        }
        context().system().scheduler().scheduleOnce(Duration.create(2000,
                TimeUnit.MILLISECONDS), self(), MSG_PRIMES_GENERATED,
                context().dispatcher(), null);
    }
}
