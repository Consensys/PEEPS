package tech.pegasys.peeps.node.genesis.qbft;

import com.fasterxml.jackson.annotation.JsonGetter;

public class Transition {
    private final int blockNumber;
    private final String address;

    public Transition(final int blockNumber, final String address) {
        this.blockNumber = blockNumber;
        this.address = address;
    }

    @JsonGetter("block")
    public int getBlockNumber() {
        return blockNumber;
    }

    @JsonGetter("validatorcontractaddress")
    public String getAddress() {
        return address;
    }
}
