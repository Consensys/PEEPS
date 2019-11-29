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
package tech.pegasys.peeps;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class OrionConfig {

  private static final int CLIENT_PORT = 8888;
  private static final int NODE_PORT = 8080;

  private final List<String> privKeys;
  private final List<String> pubKeys;
  private final List<String> bootnodeUrls;
  private final String networkIpAddress;

  public OrionConfig(
      final List<String> privKeys,
      final List<String> pubKeys,
      final List<String> bootnodeUrls,
      final String networkIpAddress) {
    this.privKeys = privKeys;
    this.pubKeys = pubKeys;
    this.bootnodeUrls = bootnodeUrls;
    this.networkIpAddress = networkIpAddress;
  }

  public void write(final String outputFile) throws IOException {

    final StringBuilder content = new StringBuilder();
    content.append(String.format("nodeUrl = \"http://%s:%d\"\n", networkIpAddress, NODE_PORT));
    content.append(String.format("clientUrl = \"http://%s:%d\"\n", networkIpAddress, CLIENT_PORT));
    content.append(String.format("nodeport = %d\n", NODE_PORT));
    content.append(String.format("clientport = %d\n", CLIENT_PORT));
    content.append(String.format("publickeys = [%s]\n", flatten(pubKeys)));
    content.append(String.format("privatekeys = [%s]\n", flatten(privKeys)));
    content.append(String.format("othernodes  = [%s]\n", flatten(bootnodeUrls)));

    Files.write(
        new File(outputFile).toPath(),
        content.toString().getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE);
  }

  private String flatten(final List<String> values) {
    return values.stream().map(k -> "\"" + k + "\"").collect(Collectors.joining(","));
  }
}
