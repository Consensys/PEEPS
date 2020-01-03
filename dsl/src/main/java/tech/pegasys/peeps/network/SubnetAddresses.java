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
package tech.pegasys.peeps.network;

import static com.google.common.base.Preconditions.checkNotNull;

public class SubnetAddresses {

  private static final byte FIRST_AVAILABLE_HOST_ADDRESS = 2;

  private final String addressFormat;
  private byte hostAddress;

  public SubnetAddresses(final String addressFormat) {
    checkNotNull(addressFormat);

    this.addressFormat = addressFormat;
    this.hostAddress = FIRST_AVAILABLE_HOST_ADDRESS;
  }

  /** Retrieves the next available IP address and now considers it as unavailable. */
  public synchronized String getAddressAndIncrement() {
    final String address = String.format(addressFormat, hostAddress);
    hostAddress++;
    return address;
  }
}
