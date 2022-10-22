package dk.systemedz.entsoe.marketdataservice.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfiguration {

    /*@Bean
    @Profile("!productionHttp")
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .requiresChannel(channel ->
                        channel.anyRequest().requiresSecure())
                .authorizeRequests(authorize ->
                        authorize.anyRequest().permitAll())
                .build();
    }*/

}
