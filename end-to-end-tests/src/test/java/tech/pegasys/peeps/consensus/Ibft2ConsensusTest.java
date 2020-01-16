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

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Besu;
import tech.pegasys.peeps.node.BesuConfigurationBuilder;
import tech.pegasys.peeps.node.GenesisAccounts;
import tech.pegasys.peeps.node.NodeKeys;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.verification.ValueReceived;
import tech.pegasys.peeps.node.verification.ValueSent;
import tech.pegasys.peeps.privacy.OrionKeyPair;
import tech.pegasys.peeps.signer.EthSigner;
import tech.pegasys.peeps.signer.SignerWallet;

import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class Ibft2ConsensusTest extends NetworkTest {

  private Besu nodeAlpha;
  private EthSigner signerAlpha;

  @Override
  protected void setUpNetwork(final Network network) {
    // TODO need to convert the public key to an address
    //	  network.set(ConsensusMechanism.IBFT2, NodeKeys.BOOTNODE.getPubKey());

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

    network.addNode(
        new BesuConfigurationBuilder()
            .withBootnodeEnodeAddress(bootnodeEnodeAddress)
            .withPrivacyManagerPublicKey(OrionKeyPair.BETA.getPublicKey()));

    // Choose IBFT 2 as consensus mechanism
  }

  @Test
  public void consensusAfterMiningMustHappen() {

    // TODO The sender account should be retrieved from the Signer (as it know which accounts it has
    // unlocked)
    final Address sender = GenesisAccounts.GAMMA.address();
    final Address receiver = GenesisAccounts.BETA.address();
    final Wei transderAmount = Wei.valueOf(5000L);

    network().verifyConsensusOnValue(sender, receiver);

    final Wei senderStartBalance = nodeAlpha.rpc().getBalance(sender);
    final Wei receiverStartBalance = nodeAlpha.rpc().getBalance(receiver);

    final Hash receipt = signerAlpha.rpc().transfer(sender, receiver, transderAmount);

    network().awaitConsensusOnTransactionReciept(receipt);

    // verify state transform
    final ValueSent sent = new ValueSent(sender, senderStartBalance, receipt);
    final ValueReceived received =
        new ValueReceived(receiver, receiverStartBalance, transderAmount);

    nodeAlpha.verifyTransition(sent, received);

    network().verifyConsensusOnValue(sender, receiver);
  }
}
