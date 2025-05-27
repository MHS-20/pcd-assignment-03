package pcd.ass01;

import java.util.List;

public interface BoidProtocol {
    public static record StartUpdate (List<Boid> boids) {}
    //public static record CalculateVelocity (List<Boid> boids) {}
    //public static record StartUpdateVelocity () {}
    //public static record StartUpdatePosition () {}

    public static record UpdatedBoid (Boid boid) {}
    public static record UpdateView (BoidsModel model, int framerate) {}
//    public static record UpdatedPosition (Boid boid) {}
//    public static record UpdatedVelocity (Boid boid) {}

    public static record BootSimulation(BoidsModel model) {}
    public static record StopSimulation () {}
    public static record StartSimulation () {}
    public static record ResetSimulation (List<Boid> boids) {}

    // model weight messages
    public static record SetSeparationWeight(double weight) {}
    public static record SetAlignmentWeight(double weight) {}
    public static record SetCohesionWeight(double weight) {}
}