/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.peeps.signer.rpc;

import tech.pegasys.peeps.json.rpc.JsonRpcClient;
import tech.pegasys.peeps.node.rpc.admin.ConnectedPeer;
import tech.pegasys.peeps.node.rpc.admin.ConnectedPeersResponse;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignerRpcClient extends JsonRpcClient {

  private static final Logger LOG = LogManager.getLogger();

  public SignerRpcClient(final Vertx vertx) {
    super(vertx, LOG);
  }

  private ConnectedPeer[] connectedPeers() {
    return post("admin_peers", ConnectedPeersResponse.class).getResult();
  }
}
