/*
 * Copyright 2020 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package tech.pegasys.peeps.node;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.testcontainers.containers.GenericContainer;
import tech.pegasys.peeps.network.NetworkMember;
import tech.pegasys.peeps.network.subnet.SubnetAddress;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.NodeIdentifier;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.node.rpc.NodeRpc;
import tech.pegasys.peeps.node.rpc.NodeRpcClient;
import tech.pegasys.peeps.node.rpc.NodeRpcMandatoryResponse;
import tech.pegasys.peeps.node.verification.AccountValue;
import tech.pegasys.peeps.node.verification.NodeValueTransition;
import tech.pegasys.peeps.util.DockerLogs;

public abstract class Web3Provider implements NetworkMember {

  protected final NodeRpcClient nodeRpc;
  protected final NodeRpc rpc;
  protected final GenericContainer<?> dockerContainer;
  private final Web3ProviderConfiguration config;

  public Web3Provider(final Web3ProviderConfiguration config,
      final GenericContainer<?> dockerContainer) {
    this.config = config;
    this.dockerContainer = dockerContainer;
    this.nodeRpc = new NodeRpcClient(config.getVertx(), dockerLogs());
    this.rpc = new NodeRpcMandatoryResponse(nodeRpc);
  }

  public abstract SubnetAddress ipAddress();

  // TODO these may not have a value, i.e. node not started :. optional
  public abstract String enodeId();

  // TODO stricter typing then String
  public abstract String enodeAddress();

  public abstract String nodePublicKey();

  public abstract NodeIdentifier identity();

  public abstract int httpRpcPort();

  public abstract void awaitConnectivity(final Collection<Web3Provider> peers);

  public abstract String getNodeId();

  public void verifyValue(final Set<AccountValue> values) {
    values.parallelStream().forEach(value -> value.verify(rpc));
  }

  private Set<Supplier<String>> dockerLogs() {
    return Set.of(() -> getLogs());
  }

  public String getLogs() {
    return DockerLogs.format("Web3Provider", dockerContainer);
  }

  public NodeRpc rpc() {
    return rpc;
  }

  public void verifyTransition(final NodeValueTransition... changes) {
    Stream.of(changes).parallel().forEach(change -> change.verify(rpc));
  }

  public void verifySuccessfulTransactionReceipt(final Hash transaction) {
    final TransactionReceipt receipt = rpc.getTransactionReceipt(transaction);

    assertThat(receipt.getTransactionHash()).isEqualTo(transaction);
    assertThat(receipt.isSuccess()).isTrue();
  }
}
