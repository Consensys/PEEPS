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
package tech.pegasys.peeps.node.genesis.ibft2;

import tech.pegasys.peeps.node.genesis.GenesisConfig;
import tech.pegasys.peeps.node.genesis.bft.BftConfig;

import com.fasterxml.jackson.annotation.JsonGetter;

public class GenesisConfigIbft2 extends GenesisConfig {

  private final BftConfig consensusConfig;

  public GenesisConfigIbft2(final long chainId, final BftConfig consensusConfig) {
    super(chainId);
    this.consensusConfig = consensusConfig;
  }

  @JsonGetter("ibft2")
  public BftConfig getConsensusConfig() {
    return consensusConfig;
  }

  @Override
  public void setValidatorContractValidatorTransaction(
      final int blockNumber, final String address) {
    throw new RuntimeException("Can not use transitions with IBFT2");
  }
}
