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
package tech.pegasys.peeps.node;

public class NodeConfigurationBuilder {

  private static final String DEFAULT_GENESIS_FILE = "genesis/eth_hash_4004.json";

  private String genesis;
  private String cors;

  public NodeConfigurationBuilder() {
    this.genesis = DEFAULT_GENESIS_FILE;
  }

  public NodeConfigurationBuilder withGenesis(final String genesisFile) {
    this.genesis = genesisFile;
    return this;
  }

  public NodeConfigurationBuilder cors(final String cors) {
    this.cors = cors;
    return this;
  }

  public NodeConfiguration build() {

    return new NodeConfiguration(genesis, cors);
  }
}
