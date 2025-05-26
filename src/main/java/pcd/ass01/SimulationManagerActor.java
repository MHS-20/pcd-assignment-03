package pcd.ass01;

import akka.actor.*;
import pcd.ass01.BoidProtocol.*;

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
                .match(BootSimulation.class, this::onBootSimulation)
                .match(StartSimulation.class, this::onStartSimulation)
                .match(UpdatedBoid.class, this::onUpdatedBoid)
                .match(StopSimulation.class, this::onStopSimulation)
                .match(ResetSimulation.class, this::onResetSimulation)
                .build();
    }

    private void onBootSimulation(BootSimulation msg) {
        List<Boid> boids = model.getBoids();
        for (int i = 0; i < nBoids; i++) {
            Boid boid = boids.get(i);
            ActorRef boidActor = getContext().actorOf(BoidActor.props(boid, model), "boid-" + i);
            boidActors.add(boidActor);
        }
        // self().tell(new BoidsProtocol.UpdateAllBoids(), self());
    }

    private void onStartSimulation(StartSimulation msg) {
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(new StartUpdate(model.getBoids()), self());
        }
        // Puoi pianificare il prossimo update qui (es. con context().system().scheduler())
    }

    private void onUpdatedBoid(UpdatedBoid msg) {
        // Gestisci l'aggiornamento del boid
        Boid updatedBoid = msg.boid();
        model.getBoids().set(model.getBoids().indexOf(updatedBoid), updatedBoid);
    }

    private void onStopSimulation(StopSimulation msg) {
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(PoisonPill.getInstance(), self());
        }
        boidActors.clear();
    }

    private void onResetSimulation(ResetSimulation msg) {
        model.setBoids(msg.boids());
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(new ResetSimulation(msg.boids()), self());
        }
    }
}