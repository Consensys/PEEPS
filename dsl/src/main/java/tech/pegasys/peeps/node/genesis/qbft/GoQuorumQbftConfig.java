package tech.pegasys.peeps.node.genesis.qbft;

import com.fasterxml.jackson.annotation.JsonGetter;
import tech.pegasys.peeps.node.genesis.GenesisConfig;
import tech.pegasys.peeps.node.genesis.bft.BftConfig;

import java.util.ArrayList;
import java.util.List;

public class GoQuorumQbftConfig extends GenesisConfig {
    private final BftConfig consensusConfig;
    private final List<Transition> transitions = new ArrayList<>();

    public GoQuorumQbftConfig(final long chainId, final BftConfig consensusConfig) {
        super(chainId);
        this.consensusConfig = consensusConfig;
    }

    @JsonGetter("qbft")
    public BftConfig getConsensusConfig() {
        return consensusConfig;
    }

    @JsonGetter("transitions")
    public List<Transition> getTransitions() {
        return transitions;
    }

    @Override
    public void setSmartContractTransition(final int blockNumber, final String address) {
        transitions.add(new Transition(blockNumber, address));
    }
}
