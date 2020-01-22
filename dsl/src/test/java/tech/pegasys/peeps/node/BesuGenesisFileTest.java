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
import tech.pegasys.peeps.node.genesis.ethhash.EthHashConfig;
import tech.pegasys.peeps.node.genesis.ethhash.GenesisConfigEthHash;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import io.vertx.core.json.DecodeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BesuGenesisFileTest {

  @Test
  public void matchingGenesisFileMustNotBeRecreated(@TempDir Path directory) throws IOException {
    final Path location = directory.resolve("matchingGenesisFileMustNotBeRecreated.json");
    final BesuGenesisFile genesisFile = new BesuGenesisFile(location);
    final Genesis genesisAlpha = createGenesis(genesisFile, Account.ALPHA, Account.BETA);
    final Genesis genesisBeta = createGenesis(genesisFile, Account.ALPHA, Account.BETA);

    genesisFile.ensureExists(genesisAlpha);

    final FileTime genesisAlpaModified = Files.getLastModifiedTime(location);

    genesisFile.ensureExists(genesisBeta);

    assertThat(genesisAlpaModified).isEqualTo(Files.getLastModifiedTime(location));
  }

  @Test
  public void nonMatchingGenesisFileMsutException(@TempDir Path directory) {

    // TODO code
  }

  @Test
  public void createdGenesisMustDeserialize(@TempDir Path directory)
      throws DecodeException, IOException {
    final Path location = directory.resolve("createdGenesisMustDeserialize.json");
    final BesuGenesisFile genesisFile = new BesuGenesisFile(location);
    final Genesis genesis = createGenesis(genesisFile, Account.ALPHA, Account.BETA);

    genesisFile.ensureExists(genesis);

    final byte[] expected = Json.encode(genesis).getBytes(StandardCharsets.UTF_8);
    final byte[] created = Files.readAllBytes(location);
    assertThat(expected).isEqualTo(created);
  }

  private Genesis createGenesis(final BesuGenesisFile genesisFile, final Account... accounts) {
    return new Genesis(
        new GenesisConfigEthHash(123, new EthHashConfig()),
        Account.of(Account.ALPHA, Account.BETA));
  }
}
