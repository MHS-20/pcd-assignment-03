package pcd.ass01;

import java.util.List;

public interface MessageProtocol {
    public static record StartUpdatePoisition (List<Boid> boids) {}
    public static record StartUpdateVelocity (List<Boid> boids) {}

    public static record UpdatedPosition (Boid boid) {}
    public static record UpdatedVelocity (Boid boid) {}

    public static record StopSimulation () {}
    public static record StartSimulation (List<Boid> boids) {}
    public static record ResetSimulation (List<Boid> boids) {}

    // model weight messages
    public static record SetSeparationWeight(double weight) {}
    public static record SetAlignmentWeight(double weight) {}
    public static record SetCohesionWeight(double weight) {}
}