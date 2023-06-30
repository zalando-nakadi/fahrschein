package org.zalando.spring.boot.fahrschein.nakadi.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JitterConfig {

    private Boolean enabled = Boolean.FALSE;

    private JitterType type = JitterType.EQUAL;
}
