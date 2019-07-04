package com.ibm.isl.ta;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "taclient")
public class AppProperties {
    private String icp_host;
    private String api_host;
    private String api_host_context;

	public String getIcp_host() {
		return icp_host;
	}

	public void setIcp_host(String icp_host) {
		this.icp_host = icp_host;
	}

	public String getApi_host() {
		return api_host;
	}

	public void setApi_host(String api_host) {
		this.api_host = api_host;
	}

	public String getApi_host_context() {
		return api_host_context;
	}

	public void setApi_host_context(String api_host_context) {
		this.api_host_context = api_host_context;
	}
 
    
    /*
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration readTimeout = Duration.ofSeconds(30);
    private final Security security = new Security();

    // Getters and Setters (Omitted for brevity)

    public static class Security {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();
        private boolean enabled;
        private Map<String, String> permissions = new HashMap<>();

        // Getters and Setters (Omitted for brevity)
    }*/
}