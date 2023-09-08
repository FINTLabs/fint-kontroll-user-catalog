package no.fintlabs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(no.fintlabs.securityconfig.FintKontrollSecurityConfig.class)
public class FintKontrollConfiguration {

}