package tech.pegasys.peeps.privacy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;
import java.util.List;
import org.testcontainers.containers.Network;

public class OrionConfigurationBuilder {

  private List<String> privKeys;
  private List<String> pubKeys;
  private List<String> bootnodeUrls;
  private String ipAddress;
  private Path fileSystemConfigFile;
  private Network containerNetwork;

  public OrionConfigurationBuilder withPrivateKeys(final List<String> privKeys) {
    this.privKeys = privKeys;
    return this;
  }

  public OrionConfigurationBuilder withPublicKeys(final List<String> pubKeys) {
    this.pubKeys = pubKeys;
    return this;
  }

  public OrionConfigurationBuilder withBootnodeUrls(final List<String> bootnodeUrls) {
    this.bootnodeUrls = bootnodeUrls;
    return this;
  }

  public OrionConfigurationBuilder withContainerNetwork(final Network containerNetwork) {
    this.containerNetwork = containerNetwork;
    return this;
  }


  public OrionConfigurationBuilder withIpAddress(final String networkIpAddress) {
    this.ipAddress = networkIpAddress;
    return this;
  }

  public OrionConfigurationBuilder withFileSystemConfigurationFile(
      final Path fileSystemConfigFile) {
    this.fileSystemConfigFile = fileSystemConfigFile;
    return this;
  }

  public OrionConfiguration build() {
    checkNotNull(privKeys, "Private keys are mandatory");
    checkArgument(privKeys.size() > 0, "At least one private key is required");
    checkNotNull(pubKeys, "Public keys are mandatory");
    checkArgument(pubKeys.size() > 0, "At least one public key is required");
    checkNotNull(fileSystemConfigFile, "A file system configuration file path is mandatory");
    checkNotNull(containerNetwork, "Container network Address is mandatory");
    checkNotNull(ipAddress, "Container IP Address is mandatory");

    return new OrionConfiguration(privKeys, pubKeys, bootnodeUrls, ipAddress, containerNetwork,
        fileSystemConfigFile);
  }
}
