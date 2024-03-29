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
package tech.pegasys.peeps.signer;

import tech.pegasys.peeps.signer.model.SignerKeyFileResource;
import tech.pegasys.peeps.signer.model.SignerPasswordFileResource;
import tech.pegasys.peeps.signer.model.WalletFileResources;

import org.apache.tuweni.eth.Address;
import org.web3j.crypto.Credentials;

public class SignerConfiguration {
  private final String name;
  private final WalletFileResources resources;
  private final Credentials credentials;

  public SignerConfiguration(
      final String name,
      final String keyResource,
      final String passwordResource,
      final Credentials credentials) {
    this.credentials = credentials;

    this.resources =
        new WalletFileResources() {

          @Override
          public SignerPasswordFileResource getPassword() {
            return new SignerPasswordFileResource(passwordResource);
          }

          @Override
          public SignerKeyFileResource getKey() {
            return new SignerKeyFileResource(keyResource);
          }
        };

    this.name = name;
  }

  public String name() {
    return name;
  }

  public WalletFileResources resources() {
    return resources;
  }

  public Credentials getCredentials() {
    return credentials;
  }

  public Address address() {
    return Address.fromHexString(credentials.getAddress());
  }
}
