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
package tech.pegasys.peeps.network;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.peeps.util.Await.await;

import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.BesuConfigurationBuilder;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.model.TransactionReceipt;
import tech.pegasys.peeps.privacy.Orion;
import tech.pegasys.peeps.privacy.OrionConfigurationBuilder;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.EthSignerConfigurationBuilder;
import tech.pegasys.peeps.util.PathGenerator;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;

public class Network implements Closeable {

  private final List<NetworkMember> members;
  private final List<Besu> nodes;
  private final List<EthSigner> signers;
  private final List<Orion> privacyManagers;

  private final Subnet subnet;
  private final org.testcontainers.containers.Network network;
  private final PathGenerator pathGenerator;
  private final Vertx vertx;

  public Network(final Path configurationDirectory) {
    checkNotNull(configurationDirectory, "Path to configuration directory is mandatory");

    this.privacyManagers = new ArrayList<>();
    this.members = new ArrayList<>();
    this.signers = new ArrayList<>();
    this.nodes = new ArrayList<>();
    this.pathGenerator = new PathGenerator(configurationDirectory);
    this.vertx = Vertx.vertx();
    this.subnet = new Subnet();
    this.network = subnet.createContainerNetwork();
  }

  public void start() {
    members.parallelStream().forEach(member -> member.start());

    awaitConnectivity();
  }

  public void stop() {
    members.parallelStream().forEach(member -> member.stop());
  }

  @Override
  public void close() {
    stop();
    vertx.close();
    network.close();
  }

  public Besu addNode(final BesuConfigurationBuilder config) {
    final Besu besu =
        new Besu(
            config
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(subnet.getAddressAndIncrement())
                .build());

    nodes.add(besu);
    members.add(besu);

    return besu;
  }

  public Orion addPrivacyManager(final OrionConfigurationBuilder config) {

    final Orion manager =
        new Orion(
            config
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(subnet.getAddressAndIncrement())
                .withFileSystemConfigurationFile(pathGenerator.uniqueFile())
                .withBootnodeUrls(privacyManagerBootnodeUrls())
                .build());

    privacyManagers.add(manager);
    members.add(manager);

    return manager;
  }

  public EthSigner addSigner(final EthSignerConfigurationBuilder config, final Besu downstream) {
    final EthSigner signer =
        new EthSigner(
            config
                .withVertx(vertx)
                .withContainerNetwork(network)
                .withIpAddress(subnet.getAddressAndIncrement())
                .withDownstream(downstream)
                .build());

    signers.add(signer);
    members.add(signer);

    return signer;
  }

  /**
   * Waits until either all nodes in the network reach consensus on the Transaction Receipt (that
   * includes a block hash), or exceptions when wait time has been exceeded.
   */
  public void awaitConsensusOnTransactionReciept(final Hash transaction) {
    checkState(nodes.size() > 1, "There must be two or more nodes to be able to wait on consensus");

    await(
        () -> {
          final List<TransactionReceipt> receipts =
              nodes
                  .parallelStream()
                  .map(node -> node.rpc().getTransactionReceipt(transaction))
                  .collect(Collectors.toList());

          assertThat(receipts.size()).isEqualTo(nodes.size());
          final TransactionReceipt firstReceipt = receipts.get(0);

          for (final TransactionReceipt receipt : receipts) {
            assertThat(receipt).isNotNull();
            assertThat(receipt.isSuccess()).isTrue();
            assertThat(receipt).usingRecursiveComparison().isEqualTo(firstReceipt);
          }
        },
        "Consensus was not reached in time for Transaction Receipt with hash: " + transaction);
  }

  private void awaitConnectivity() {

    nodes.parallelStream().forEach(node -> node.awaitConnectivity(nodes));
    privacyManagers
        .parallelStream()
        .forEach(privacyManger -> privacyManger.awaitConnectivity(privacyManagers));
    signers.parallelStream().forEach(signer -> signer.awaitConnectivityToDownstream());
  }

  private List<String> privacyManagerBootnodeUrls() {
    return privacyManagers
        .parallelStream()
        .map(manager -> manager.getPeerNetworkAddress())
        .collect(Collectors.toList());
  }
}
