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

import tech.pegasys.peeps.util.Resources;

import java.util.Optional;

import org.testcontainers.containers.Network;

public class NodeConfiguration {

  private final String genesisFilePath;
  private final String enclavePublicKeyPath;
  private final String cors;
  private final Network containerNetwork;

  public NodeConfiguration(
      final String genesisFilePath,
      final String enclavePublicKeyPath,
      final String cors,
      final Network containerNetwork) {
    this.genesisFilePath = Resources.getCanonicalPath(genesisFilePath);
    this.enclavePublicKeyPath = Resources.getCanonicalPath(enclavePublicKeyPath);
    this.cors = cors;
    this.containerNetwork = containerNetwork;
  }

  public String getGenesisFilePath() {
    return genesisFilePath;
  }

  public String getEnclavePublicKeyPath() {
    return enclavePublicKeyPath;
  }

  public Optional<String> getCors() {
    return Optional.ofNullable(cors);
  }

  public Optional<Network> getContainerNetwork() {
    return Optional.ofNullable(containerNetwork);
  }
}
