/*
 * Copyright 2021 ConsenSys AG.
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
package tech.pegasys.peeps.privacy;

import tech.pegasys.peeps.network.NetworkMember;
import tech.pegasys.peeps.privacy.model.OrionKey;
import tech.pegasys.peeps.privacy.rpc.TransactionManagerRpcExpectingData;

import java.util.Collection;

public interface TransactionManager extends NetworkMember {

  String getPayload(OrionKey key);

  String getNetworkRpcAddress();

  void awaitConnectivity(Collection<TransactionManager> values);

  String getPeerNetworkAddress();

  // TODO is this actually needed?
  String getId();

  // TODO this isn't right
  TransactionManagerRpcExpectingData getRpc();
}
