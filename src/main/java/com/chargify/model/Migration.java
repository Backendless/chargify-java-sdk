package com.chargify.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude( JsonInclude.Include.NON_NULL )
public class Migration
{
  @JsonProperty( "product_handle" )
  private String productHandle;

  @JsonProperty( "include_trial")
  private Integer includeTrial;

  @JsonProperty( "product_price_point_handle")
  private String pricePointHandle;
}
