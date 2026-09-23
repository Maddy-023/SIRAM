// MapeoProperties.java
package com.siram.notificacion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "mapeo")
@Data
public class MapeoProperties {
    private Map<String, String> entidades;
    private Map<String, String> telefonos;
}