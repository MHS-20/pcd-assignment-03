package pcd.ass01;

import akka.actor.AbstractActor;

import java.util.List;

public class BoidActor extends AbstractActor {

    Boid boid;
    BoidsModel model;

    public BoidActor(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MessageProtocol.StartUpdateVelocity.class, this::onStartUpdateVelocity)
                .match(MessageProtocol.StartUpdatePoisition.class, this::onStartUpdatePosition)
                .build();
    }

    public void onStartUpdateVelocity(MessageProtocol.StartUpdateVelocity msg) {
        List<Boid> boids = msg.boids();
        model.setBoids(boids);
        boid.updateVelocity(model);
        getSender().tell(new MessageProtocol.UpdatedVelocity(boid), getSelf());
    }

    public void onStartUpdatePosition(MessageProtocol.StartUpdatePoisition msg) {
        List<Boid> boids = msg.boids();
        model.setBoids(boids);
        boid.updatePos(model);
        getSender().tell(new MessageProtocol.UpdatedPosition(boid), getSelf());
    }
}

