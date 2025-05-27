package pcd.ass01;

import akka.actor.*;
import pcd.ass01.BoidProtocol.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimulationManagerActor extends AbstractActorWithStash {

    private int framerate;
    private static final int FRAMERATE = 60;
   // private ViewActor view;
    private BoidsView view;
    private long t0;

    private BoidsModel model;
    private int nBoids;

    private List<Boid> boids;
    private int count = 0;
    private final List<ActorRef> boidActors = new ArrayList<>();

    public SimulationManagerActor(BoidsModel model, int nBoids, BoidsView view) {
        this.model = model;
        this.nBoids = nBoids;
        this.view = view;
        this.boids = new ArrayList<>();
    }

    public static Props props(BoidsModel model, int nBoids, BoidsView view) {
        return Props.create(SimulationManagerActor.class, () -> new SimulationManagerActor(model, nBoids, view));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BootSimulation.class, this::onBootSimulation)
                .match(StartSimulation.class, msg -> this.stash())
                .match(StopSimulation.class, msg -> this.stash())
                .match(ResetSimulation.class, msg -> this.stash())
                .match(SetSeparationWeight.class, msg -> this.stash())
                .match(SetAlignmentWeight.class, msg -> this.stash())
                .match(SetCohesionWeight.class, msg -> this.stash())
                .match(UpdatedBoid.class, msg -> this.stash())
                .build();
    }

    public Receive updateBehavior() {
        return receiveBuilder()
                .match(StartSimulation.class, this::onStartSimulation)
                .match(StopSimulation.class, this::onStopSimulation)
                .match(ResetSimulation.class, this::onResetSimulation)
                .match(SetSeparationWeight.class, this::onSeparationWeight)
                .match(SetAlignmentWeight.class, this::onAlignmentWeight)
                .match(SetCohesionWeight.class, this::onCohesionWeight)
                .match(UpdatedBoid.class, msg -> this.stash())
                .match(BootSimulation.class, msg -> this.stash())
                .build();
    }


    public Receive collectUpdateBehavior() {
        return receiveBuilder()
                .match(UpdatedBoid.class, this::onUpdatedBoid)
                .match(StartSimulation.class, msg -> this.stash())
                .match(BootSimulation.class, msg -> this.stash())
                .match(StopSimulation.class, msg -> this.stash())
                .match(ResetSimulation.class, msg -> this.stash())
                .match(SetSeparationWeight.class, msg -> this.stash())
                .match(SetAlignmentWeight.class, msg -> this.stash())
                .match(SetCohesionWeight.class, msg -> this.stash())
                .match(UpdatedBoid.class, msg -> this.stash())
                .build();
    }

    private void onBootSimulation(BootSimulation msg) {
        //model = msg.model();
        List<Boid> boids = model.getBoids();
        for (int i = 0; i < nBoids; i++) {
            Boid boid = boids.get(i);
            ActorRef boidActor = getContext().actorOf(BoidActor.props(boid, model), "boid-" + i);
            boidActors.add(boidActor);
        }

        this.unstashAll();
        this.getContext().become(updateBehavior());
        self().tell(new StartSimulation(), self());
    }

    private void onStartSimulation(StartSimulation msg) {
        t0 = System.currentTimeMillis();
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(new StartUpdate(model.getBoids()), self());
        }
        boids.clear();
        count = 0;

        this.unstashAll();
        this.getContext().become(collectUpdateBehavior());
    }

    private void onUpdatedBoid(UpdatedBoid msg) {
        boids.add(msg.boid());
        count++;
        if (count == nBoids) {

            // update gui
            model.setBoids(boids);
            //view.getSelf().tell(new UpdateView(model, framerate), self());
            view.setModel(model);
            view.update(framerate);

            var dtElapsed = System.currentTimeMillis() - t0;
            var framratePeriod = 1000/FRAMERATE;
            if (dtElapsed < framratePeriod) {
                try {
                    Thread.sleep(framratePeriod - dtElapsed);
                } catch (Exception ex) {}
                framerate = FRAMERATE;
            } else {
                framerate = (int) (1000/dtElapsed);
            }

            this.unstashAll();
            this.getContext().become(updateBehavior());
            self().tell(new StartSimulation(), self());
        }
    }

    private void onStopSimulation(StopSimulation msg) {
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(new StopSimulation(), self());
        }
    }

    private void onResetSimulation(ResetSimulation msg) {
        model.setBoids(msg.boids());
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(PoisonPill.getInstance(), self());
        }
        boidActors.clear();
        this.getContext().become(createReceive());
        this.getSelf().tell(new BootSimulation(model), self());
    }

    private void onSeparationWeight(SetSeparationWeight msg) {
        model.setSeparationWeight(msg.weight());
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(new SetSeparationWeight(msg.weight()), self());
        }
    }

    private void onAlignmentWeight(SetAlignmentWeight msg) {
        model.setAlignmentWeight(msg.weight());
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(new SetAlignmentWeight(msg.weight()), self());
        }
    }

    private void onCohesionWeight(SetCohesionWeight msg) {
        model.setCohesionWeight(msg.weight());
        for (ActorRef boidActor : boidActors) {
            boidActor.tell(new SetCohesionWeight(msg.weight()), self());
        }
    }
}