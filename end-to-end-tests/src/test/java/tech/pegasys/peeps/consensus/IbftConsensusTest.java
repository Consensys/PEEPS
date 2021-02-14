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
package tech.pegasys.peeps.consensus;

import tech.pegasys.peeps.NetworkTest;
import tech.pegasys.peeps.NodeConfiguration;
import tech.pegasys.peeps.SignerConfiguration;
import tech.pegasys.peeps.network.ConsensusMechanism;
import tech.pegasys.peeps.network.Network;

public class IbftConsensusTest extends NetworkTest {

  private final NodeConfiguration node = NodeConfiguration.ALPHA;
  private final SignerConfiguration signer = SignerConfiguration.ALPHA;

  @Override
  protected void setUpNetwork(final Network network) {
    network.addNode(node.id(), node.keys());
    network.addNode(NodeConfiguration.BETA.id(), NodeConfiguration.BETA.keys());
    network.set(ConsensusMechanism.IBFT, node.id());
    network.addSigner(signer.id(), signer.resources(), node.id());
  }

}
