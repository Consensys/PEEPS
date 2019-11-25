package tech.pegasys.peeps.node.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeInfoResponse {

  private final NodeInfo result;

  public NodeInfoResponse(@JsonProperty("result") final NodeInfo result) {
    this.result = result;
  }

  public NodeInfo getResult() {
    return result;
  }
}
