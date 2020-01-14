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
 */
package tech.pegasys.peeps.consensus;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.BesuConfigurationBuilder;
import tech.pegasys.peeps.node.GenesisAccounts;
import tech.pegasys.peeps.node.NodeKeys;
import tech.pegasys.peeps.node.model.Address;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.privacy.OrionKeyPair;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.SignerWallet;

import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.Test;

public class EthHashConsensusTest extends NetworkTest {

  private Besu nodeAlpha;
  private EthSigner signerAlpha;
  private Besu nodeBeta;

  @Override
  protected void setUpNetwork(final Network network) {

    this.nodeAlpha =
        network.addNode(
            new BesuConfigurationBuilder()
                .withNodePrivateKeyFile(NodeKeys.BOOTNODE.getPrivateKeyFile())
                .withPrivacyManagerPublicKey(OrionKeyPair.ALPHA.getPublicKey()));

    this.signerAlpha = network.addSigner(SignerWallet.ALPHA, nodeAlpha);

    // TODO move this into Network, same approach as Orions, add enodeAddress to Besu
    // TODO fits as a function of Besu
    // TODO better typing then String - create ENODE Address
    final String bootnodeEnodeAddress =
        NodeKeys.BOOTNODE.getEnodeAddress(nodeAlpha.ipAddress(), nodeAlpha.p2pPort());

    this.nodeBeta =
        network.addNode(
            new BesuConfigurationBuilder()
                .withBootnodeEnodeAddress(bootnodeEnodeAddress)
                .withPrivacyManagerPublicKey(OrionKeyPair.BETA.getPublicKey()));
  }

  @Test
  public void consensusAfterMiningMustHappen() {

    final Address sender = GenesisAccounts.GAMMA.address();
    final Address receiver = GenesisAccounts.BETA.address();
    final long amount = 5000L;

    final Wei senderStartingBalance = nodeAlpha.rpc().getBalance(sender);
    final Wei receiverStartingBalance = nodeAlpha.rpc().getBalance(receiver);

    final Hash transfer = signerAlpha.rpc().transfer(sender, receiver, amount);

    network().awaitConsensusOnTransactionReciept(transfer);

    final Wei senderEndBalance = nodeAlpha.rpc().getBalance(sender);
    final Wei receiverEndBalance = nodeAlpha.rpc().getBalance(receiver);

    // TODO assert change in balances is correct

    assertThat(senderEndBalance).isEqualTo(nodeBeta.rpc().getBalance(sender));
    assertThat(receiverEndBalance).isEqualTo(nodeBeta.rpc().getBalance(receiver));
  }
}
