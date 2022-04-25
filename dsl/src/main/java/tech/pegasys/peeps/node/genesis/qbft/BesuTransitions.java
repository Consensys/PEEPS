package tech.pegasys.peeps.node.genesis.qbft;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.ArrayList;
import java.util.List;

public class BesuTransitions {
    private final List<Transition> transitions = new ArrayList<>();


    @JsonGetter("qbft")
    public List<Transition> getTransitions() {
        return transitions;
    }

    public void add(final Transition transition) {
        this.transitions.add(transition);
    }
}
