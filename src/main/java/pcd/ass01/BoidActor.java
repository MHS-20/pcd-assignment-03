package pcd.ass01;

import akka.actor.AbstractActor;
import akka.actor.Props;
import pcd.ass01.BoidProtocol.*;

import java.util.List;

public class BoidActor extends AbstractActor {

    private Boid boid;
    private BoidsModel model;

    public BoidActor(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartUpdate.class, this::onStartUpdate)
                .match(StartUpdateVelocity.class, this::onStartUpdateVelocity)
                .match(StartUpdatePoisition.class, this::onStartUpdatePosition)
                .build();
    }

    public static Props props(Boid boid, BoidsModel model) {
        return Props.create(BoidActor.class, () -> new BoidActor(boid, model));
    }

    public void onStartUpdate(StartUpdate msg) {
        List<Boid> boids = msg.boids();
        model.setBoids(boids);
        boid.update(model);
        getSender().tell(new BoidProtocol.UpdatedBoid(boid), getSelf());
    }

    public void onStartUpdateVelocity(StartUpdateVelocity msg) {
        List<Boid> boids = msg.boids();
        model.setBoids(boids);
        boid.updateVelocity(model);
        getSender().tell(new BoidProtocol.UpdatedVelocity(boid), getSelf());
    }

    public void onStartUpdatePosition(StartUpdatePoisition msg) {
        List<Boid> boids = msg.boids();
        model.setBoids(boids);
        boid.updatePos(model);
        getSender().tell(new BoidProtocol.UpdatedPosition(boid), getSelf());
    }
}

