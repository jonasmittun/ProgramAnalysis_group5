package Week05;

import java.util.Set;

public interface AbstractStepper {
    Set<State> step(State state);
}
