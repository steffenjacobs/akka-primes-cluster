package me.steffenjacobs.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static me.steffenjacobs.akka.PrimeClusterMasterActor.*;

public class PrimeClusterWorkerActor extends AbstractActor {
    private final ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    @Override
    public void preStart() {
        mediator.tell(new DistributedPubSubMediator.Subscribe(TOPIC_WORKERS, self()), self());
    }

    @Override
    public void postStop() {
        mediator.tell(new DistributedPubSubMediator.Unsubscribe(TOPIC_WORKERS, self()), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(MSG_WORK_AVAILABLE, msg -> sender().tell(MSG_GIVE_WORK, self()))
                .match(SegmentMessage.class, msg -> {
                    // receive partition from PrimeMaster
                    List<Long> primes = new ArrayList<>();
                    for (long i = msg.getStart(); i < msg.getEnd(); i++) {
                        // add to result, if element is a prime number
                        if (isPrime(i)) {
                            primes.add(i);
                        }
                    }

                    System.out.printf("Calculated %s primes between %s and %s.%n", primes.size(), msg.getStart(),
                            msg.getEnd());
                    // send resulting subset of primes to PrimeMaster
                    this.getSender().tell(new PrimeResult(primes), this.getSelf());
                }).match(DistributedPubSubMediator.SubscribeAck.class, msg -> {
                            System.out.println("Subscribed to " +
                                    "'workers'! Requesting work...");
                            this.getSender().tell(MSG_GIVE_WORK, this.getSelf());
                        }
                ).build();
    }

    private boolean isPrime(long n) {
        return BigInteger.valueOf(n).isProbablePrime(100);
    }
}
