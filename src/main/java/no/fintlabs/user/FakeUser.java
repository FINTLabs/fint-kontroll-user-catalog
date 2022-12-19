package no.fintlabs.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FakeUser {


    private String address;
    @JsonProperty("birth_data")
    private String birthData;
    private String blood;
    private Long bonus;
    private String cardexpir;
    private String color;
    private String company;
    private String domain;
    @JsonProperty("domain_url")
    private String domainUrl;
    @JsonProperty("email_d")
    private String emailD;
    @JsonProperty("email_u")
    private String emailU;
    @JsonProperty("email_url")
    private String emailUrl;
    private String eye;
    private String hair;
    private Long height;
    private String ipv4;
    @JsonProperty("ipv4_url")
    private String ipv4Url;
    private Double latitude;
    private Double longitude;
    private String macaddress;
    @JsonProperty("maiden_name")
    private String maidenName;
    private String name;
    private String password;
    @JsonProperty("phone_h")
    private String phoneH;
    @JsonProperty("phone_w")
    private String phoneW;
    private String pict;
    private String plasticcard;
    private String sport;
    private String url;
    private String useragent;
    private String username;
    private String uuid;
    private Long weight;

}
