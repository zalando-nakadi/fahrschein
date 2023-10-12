package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.Data;
@Data
public class SubscriptionConfig {

  private Boolean enabled;

  private String subscriptionId;

  public static SubscriptionConfig defaultSubscriptionConfig() {
    SubscriptionConfig s = new SubscriptionConfig();
    s.setEnabled(Boolean.FALSE);

    return s;
  }
}
