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
package tech.pegasys.peeps.node.genesis.ibft;

import tech.pegasys.peeps.node.genesis.GenesisConfig;

import com.fasterxml.jackson.annotation.JsonGetter;

public class BesuConfigIbft extends GenesisConfig {

  private final BesuLegacyIbftOptions consensusConfig;

  public BesuConfigIbft(final long chainId, final BesuLegacyIbftOptions consensusConfig) {
    super(chainId);
    this.consensusConfig = consensusConfig;
  }

  @JsonGetter("ibft")
  public BesuLegacyIbftOptions getConsensusConfig() {
    return consensusConfig;
  }

  @Override
  public void setValidatorContractValidatorTransaction(final int blockNumber, final String address) {
    throw new RuntimeException("Can not use transitions with IBFT");
  }
}
