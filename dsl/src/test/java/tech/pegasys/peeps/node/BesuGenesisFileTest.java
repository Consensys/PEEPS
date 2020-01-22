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
package tech.pegasys.peeps.node;

import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.peeps.json.Json;
import tech.pegasys.peeps.node.genesis.BesuGenesisFile;
import tech.pegasys.peeps.node.genesis.Genesis;
import tech.pegasys.peeps.node.genesis.GenesisAccount;
import tech.pegasys.peeps.node.genesis.ethhash.EthHashConfig;
import tech.pegasys.peeps.node.genesis.ethhash.GenesisConfigEthHash;
import tech.pegasys.peeps.node.model.GenesisAddress;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import io.vertx.core.json.DecodeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BesuGenesisFileTest {

  @Test
  public void matchingGenesisFileMsutReturn() {

    // TODO code
  }

  @Test
  public void nonMatchingGenesisFileMsutException() {

    // TODO code
  }

  @Test
  public void createdGenesisMustDeserialize(@TempDir Path directory)
      throws DecodeException, IOException {

    final String filename = "genesis-file.json";
    final Path location = directory.resolve(filename);
    final BesuGenesisFile genesisFile = new BesuGenesisFile(location);

    final GenesisConfigEthHash config = new GenesisConfigEthHash(123, new EthHashConfig());
    final Map<GenesisAddress, GenesisAccount> accounts = Account.of(Account.ALPHA, Account.BETA);

    final Genesis genesis = new Genesis(config, accounts);

    genesisFile.ensureExists(genesis);

    final byte[] expected = Json.encode(genesis).getBytes(StandardCharsets.UTF_8);
    final byte[] created = Files.readAllBytes(location);

    assertThat(expected).isEqualTo(created);
  }
}
