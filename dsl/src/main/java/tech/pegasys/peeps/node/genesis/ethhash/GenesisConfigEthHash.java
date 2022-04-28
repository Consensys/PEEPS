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
package tech.pegasys.peeps.node.genesis.ethhash;

import tech.pegasys.peeps.node.genesis.GenesisConfig;

import com.fasterxml.jackson.annotation.JsonGetter;

public class GenesisConfigEthHash extends GenesisConfig {

  private final EthHashConfig ethHash;

  public GenesisConfigEthHash(final long chainId, final EthHashConfig ethHash) {
    super(chainId);
    this.ethHash = ethHash;
  }

  @JsonGetter("ethash")
  public EthHashConfig getConsensusConfig() {
    return ethHash;
  }

  @Override
  public void setValidatorContractValidatorTransaction(final int blockNumber, final String address) {
    throw new RuntimeException("Can not use transitions with EthHash");
  }
}
