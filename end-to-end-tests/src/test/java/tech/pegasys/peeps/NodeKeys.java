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

import tech.pegasys.peeps.node.model.NodeKey;

public enum NodeKeys {
  ALPHA("node/keys/alpha"),
  BETA("node/keys/beta");

  private static final String PRIVATE_KEY_FILENAME = "/key.priv";
  private static final String PUBLIC_KEY_FILENAME = "/key.pub";

  private final String pubKeyResource;
  private final String privKeyResource;

  NodeKeys(final String keysDirectoryResource) {

    this.pubKeyResource = keysDirectoryResource + PUBLIC_KEY_FILENAME;
    this.privKeyResource = keysDirectoryResource + PRIVATE_KEY_FILENAME;
  }

  public NodeKey keys() {
    return new NodeKey() {

      @Override
      public String nodePublicKeyResource() {
        return pubKeyResource;
      }

      @Override
      public String nodePrivateKeyResource() {
        return privKeyResource;
      }
    };
  }
}
