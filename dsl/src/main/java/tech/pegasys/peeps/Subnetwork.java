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
package tech.pegasys.peeps;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;

public class Subnetwork {

  // private static final int OCTET_MAXIMUM = 255;
  private static final String SUBNET_FORMAT = "172.20.%d.0/24";
  private static final AtomicInteger THIRD_OCTET = new AtomicInteger(0);

  public org.testcontainers.containers.Network build() {

    // TODO loop through until a free subnet is found

    final String subnet = String.format(SUBNET_FORMAT, THIRD_OCTET.getAndIncrement());

    // TODO subnet with substitution for static IPs
    final org.testcontainers.containers.Network network =
        org.testcontainers.containers.Network.builder()
            .createNetworkCmdModifier(
                modifier ->
                    modifier.withIpam(new Ipam().withConfig(new Config().withSubnet(subnet))))
            .build();

    return network;
  }

  /** Retrieves the next available IP address and now considers it as unavailable. */
  public String getAndIncrement() {
    // TODO code me!
    return null;
  }
}
