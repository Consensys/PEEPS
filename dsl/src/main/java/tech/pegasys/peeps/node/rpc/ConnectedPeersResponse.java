package tech.pegasys.peeps.node.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectedPeersResponse {

    private final ConnectedPeer[] result;

    public ConnectedPeersResponse(@JsonProperty("result") final ConnectedPeer[] result) {
      this.result = result;
    }

    public ConnectedPeer[] getResult() {
      return result;
    }
  }