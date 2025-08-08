package com.chargify.exceptions;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public final class ChargifyResponseErrorHandler extends DefaultResponseErrorHandler
{
  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.disable( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS );
    objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerModules( new JavaTimeModule() );
  }

  @Override
  protected void handleError( ClientHttpResponse response, HttpStatusCode statusCode, @Nullable URI url, @Nullable HttpMethod method ) throws IOException
  {
    if( statusCode.is4xxClientError() )
    {
      if( statusCode == HttpStatus.NOT_FOUND )
        throw new ResourceNotFoundException();
      else if( statusCode == HttpStatus.FORBIDDEN ) // TODO: see issue https://chargify.zendesk.com/hc/en-us/requests/69553
        throw new ChargifyException( readInputStream( response.getBody() ) );
      else
        throw objectMapper.readValue( response.getBody(), ChargifyError.class ).exception();
    }
    else if( statusCode.is5xxServerError() )
      throw new HttpServerErrorException( statusCode.value(), readInputStream( response.getBody() ) );
    else
      throw new UnknownHttpStatusCodeException( statusCode.value(), readInputStream( response.getBody() ) );
  }

  private String readInputStream( final InputStream stream )
  {
    return new java.util.Scanner( stream ).useDelimiter( "\\A" ).next();
  }
}
