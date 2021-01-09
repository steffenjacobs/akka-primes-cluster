package me.steffenjacobs.akka;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.routing.RandomPool;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.UUID;

public class PrimeClusterApp {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar prime-cluster-app.jar <port> <master (true|false)>");
        }
        int port = Integer.parseInt(args[0]);
        boolean createMaster = Boolean.parseBoolean(args[1]);
        System.out.printf("Starting up %s on port %s.%n", createMaster ? "master and two workers" : "two workers", port);

        // Create an Akka system
        Config config =
                ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load());
        ActorSystem actorSystem = ActorSystem.create("AkkaExampleSystem", config);

        if (createMaster) {
            //Create Master
            actorSystem.actorOf(ClusterSingletonManager.props(Props.create(PrimeClusterMasterActor.class),
                    PoisonPill.getInstance(), ClusterSingletonManagerSettings.create(actorSystem)), "master");
        }

        //Create RandomPool of 2 Workers
        actorSystem.actorOf(new RandomPool(2).props(Props.create(PrimeClusterWorkerActor.class)),
                "W_" + UUID.randomUUID());
    }
}
