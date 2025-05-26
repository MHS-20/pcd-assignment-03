package pcd.ass01;

import akka.actor.*;
import pcd.ass01.BoidsModel;
import pcd.ass01.Boid;
import pcd.ass01.MessageProtocol.*;

import java.util.ArrayList;
import java.util.List;

public class SimulationManagerActor extends AbstractActor {

    private final BoidsModel model;
    private final int nBoids;
    private final List<ActorRef> boidActors = new ArrayList<>();

    public SimulationManagerActor(BoidsModel model, int nBoids) {
        this.model = model;
        this.nBoids = nBoids;
    }

    public static Props props(BoidsModel model, int nBoids) {
        return Props.create(SimulationManagerActor.class, () -> new SimulationManagerActor(model, nBoids));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartSimulation.class, this::onStartSimulation)
                .match(StopSimulation.class, this::onStopSimulation)
                .match(ResetSimulation.class, this::onResetSimulation)
                .build();
    }

    private void onStartSimulation(StartSimulation msg) {
        List<Boid> boids = model.getBoids();
        for (int i = 0; i < nBoids; i++) {
            Boid boid = boids.get(i);
            ActorRef boidActor = getContext().actorOf(BoidActor.props(boid, model), "boid-" + i);
            boidActors.add(boidActor);
        }
        // Avvia il primo update
        self().tell(new BoidsProtocol.UpdateAllBoids(), self());
    }

    private void onUpdateAllBoids(BoidsProtocol.UpdateAllBoids msg) {
        // Invia il messaggio di update a tutti i boid
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(new BoidsProtocol.UpdateBoid(), self());
        }
        // Puoi pianificare il prossimo update qui (es. con context().system().scheduler())
    }
}