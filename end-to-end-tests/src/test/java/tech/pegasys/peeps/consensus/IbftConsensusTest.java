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


import org.apache.tuweni.crypto.SECP256K1.KeyPair;
import org.apache.tuweni.eth.Address;
import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.Test;
import tech.pegasys.peeps.FixedSignerConfigs;
import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.network.ConsensusMechanism;
import tech.pegasys.peeps.network.Network;
import tech.pegasys.peeps.node.Account;
import tech.pegasys.peeps.node.Web3Provider;
import tech.pegasys.peeps.node.Web3ProviderType;
import tech.pegasys.peeps.node.model.Hash;
import tech.pegasys.peeps.node.verification.ValueReceived;
import tech.pegasys.peeps.node.verification.ValueSent;
import tech.pegasys.peeps.signer.SignerConfiguration;

public class

IbftConsensusTest extends NetworkTest {

  private Web3Provider alphaNode;
  private final SignerConfiguration signer = FixedSignerConfigs.ALPHA;

  final KeyPair fromPrivKeyHexString(final String input) {
//    return KeyPair.fromSecretKey(SecretKey.fromBytes(Bytes32.fromHexString(input)));
    return KeyPair.random();
  }

  @Override
  protected void setUpNetwork(final Network network) {
    alphaNode = network.addNode("alpha", fromPrivKeyHexString("131f7f9bfb56c19a3026fd5d9b0f5c0173d53ba7f4ec51e51f4466f5abfa4b06"), Web3ProviderType.GOQUORUM);
    final Web3Provider betaNode = network.addNode("beta", fromPrivKeyHexString("55ec42a1da04bd05d0ee2c17fe6fb782531478ba8815f9c5f5ef7f90af5edc59"), Web3ProviderType.GOQUORUM);
    final Web3Provider gammaNode = network.addNode("gamma", fromPrivKeyHexString("9a4b0678a40507b381e1db3d52847217092d08f839b183932cf4feefb06edceb"), Web3ProviderType.GOQUORUM);
 //   final Web3Provider epsilonNode = network.addNode("epsilon", fromPrivKeyHexString("97bcfadc9f952bc5b60001608e342b702651096ce590bcf2856374925a808d9a"), Web3ProviderType.GOQUORUM);
    //network.addNode("besu-1", KeyPair.random(), Web3ProviderType.BESU);
    network.set(ConsensusMechanism.IBFT, alphaNode, betaNode, gammaNode);
    network.addSigner(signer.id(), signer.resources(), alphaNode);
  }

  @Test
  public void consensusAfterMiningMustHappen() {
    final Address sender = signer.address();
    final Address receiver = Account.BETA.address();
    final Wei transferAmount = Wei.valueOf(5000L);

    verify().consensusOnValueAt(sender, receiver);

    final Wei senderStartBalance = execute(alphaNode).getBalance(sender);
    final Wei receiverStartBalance = execute(alphaNode).getBalance(receiver);

    final Hash receipt = execute(signer).transferTo(receiver, transferAmount);

    await().consensusOnTransactionReceipt(receipt);

    verifyOn(alphaNode)
        .transistion(
            new ValueSent(sender, senderStartBalance, receipt),
            new ValueReceived(receiver, receiverStartBalance, transferAmount));

    verify().consensusOnValueAt(sender, receiver);
  }
}
