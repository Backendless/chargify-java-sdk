package com.chargify;

import com.chargify.exceptions.ChargifyResponseErrorHandler;
import com.chargify.exceptions.ResourceNotFoundException;
import com.chargify.model.Adjustment;
import com.chargify.model.Allocation;
import com.chargify.model.AllocationPreview;
import com.chargify.model.Component;
import com.chargify.model.ComponentPricePointUpdate;
import com.chargify.model.ComponentWithPricePoints;
import com.chargify.model.CreatePaymentProfile;
import com.chargify.model.CreateSubscription;
import com.chargify.model.Customer;
import com.chargify.model.Metadata;
import com.chargify.model.Migration;
import com.chargify.model.PaymentProfile;
import com.chargify.model.PricePoint;
import com.chargify.model.PricePointUpdate;
import com.chargify.model.ReferralCode;
import com.chargify.model.RenewalPreview;
import com.chargify.model.Subscription;
import com.chargify.model.SubscriptionCharge;
import com.chargify.model.SubscriptionChargePayload;
import com.chargify.model.SubscriptionChargeResult;
import com.chargify.model.SubscriptionComponent;
import com.chargify.model.SubscriptionMetadata;
import com.chargify.model.SubscriptionProductUpdate;
import com.chargify.model.SubscriptionReactivationData;
import com.chargify.model.SubscriptionStatement;
import com.chargify.model.Transaction;
import com.chargify.model.UpdatePaymentProfile;
import com.chargify.model.UpdateSubscription;
import com.chargify.model.Usage;
import com.chargify.model.product.Product;
import com.chargify.model.product.ProductFamily;
import com.chargify.model.product.ProductPricePoint;
import com.chargify.model.wrappers.AdjustmentWrapper;
import com.chargify.model.wrappers.AllocationPreviewWrapper;
import com.chargify.model.wrappers.AllocationWrapper;
import com.chargify.model.wrappers.AnyComponentWrapper;
import com.chargify.model.wrappers.ComponentPricePointUpdatesWrapper;
import com.chargify.model.wrappers.ComponentPricePointsWrapper;
import com.chargify.model.wrappers.ComponentWrapper;
import com.chargify.model.wrappers.CreateSubscriptionWrapper;
import com.chargify.model.wrappers.CustomerWrapper;
import com.chargify.model.wrappers.MetadataWrapper;
import com.chargify.model.wrappers.MeteredComponentWrapper;
import com.chargify.model.wrappers.MigrationWrapper;
import com.chargify.model.wrappers.OnOffComponentWrapper;
import com.chargify.model.wrappers.PaymentProfileWrapper;
import com.chargify.model.wrappers.PricePointUpdateResultWrapper;
import com.chargify.model.wrappers.PricePointUpdateWrapper;
import com.chargify.model.wrappers.ProductFamilyWrapper;
import com.chargify.model.wrappers.ProductPricePointsWrapper;
import com.chargify.model.wrappers.ProductWrapper;
import com.chargify.model.wrappers.QuantityBasedComponentWrapper;
import com.chargify.model.wrappers.ReferralCodeWrapper;
import com.chargify.model.wrappers.RenewalPreviewWrapper;
import com.chargify.model.wrappers.SubscriptionChargeWrapper;
import com.chargify.model.wrappers.SubscriptionComponentWrapper;
import com.chargify.model.wrappers.SubscriptionProductUpdateWrapper;
import com.chargify.model.wrappers.SubscriptionStatementWrapper;
import com.chargify.model.wrappers.SubscriptionWrapper;
import com.chargify.model.wrappers.TransactionWrapper;
import com.chargify.model.wrappers.UpdateSubscriptionWrapper;
import com.chargify.model.wrappers.UsageWrapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ChargifyService implements Chargify
{
  private final RestTemplate httpClient;

  public ChargifyService( final String domain, final String apiKey, int connectTimeoutInMillis,
                          int readTimeoutInMillis )
  {
    this( createRestTemplate( "https://" + domain + ".chargify.com", apiKey,
                              connectTimeoutInMillis, readTimeoutInMillis ) );
  }

  private ChargifyService( RestTemplate httpClient )
  {
    this.httpClient = httpClient;

    this.httpClient.getMessageConverters().stream()
        .filter( AbstractJackson2HttpMessageConverter.class::isInstance )
        .map( AbstractJackson2HttpMessageConverter.class::cast )
        .map( AbstractJackson2HttpMessageConverter::getObjectMapper )
        .forEach( mapper -> mapper.disable( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS ) );
  }

  private static RestTemplate createRestTemplate( String baseUrl, String apiKey,
                                                  int connectTimeoutInMillis, int readTimeoutInMillis )
  {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout( Duration.ofMillis( connectTimeoutInMillis ) );
    requestFactory.setReadTimeout( Duration.ofMillis( readTimeoutInMillis ) );

    RestTemplate restTemplate = new RestTemplate( requestFactory );
    restTemplate.setUriTemplateHandler( new DefaultUriBuilderFactory( baseUrl ) );
    restTemplate.setErrorHandler( new ChargifyResponseErrorHandler() );

    String base64Creds = Base64.getEncoder()
        .encodeToString( ( apiKey + ":x" ).getBytes( StandardCharsets.UTF_8 ) );
    restTemplate.getInterceptors().add( ( request, body, execution ) -> {
      request.getHeaders().add( "Authorization", "Basic " + base64Creds );
      return execution.execute( request, body );
    } );

    return restTemplate;
  }

  @Override
  public ProductFamily createProductFamily( ProductFamily productFamily )
  {
    return httpClient.postForObject( "/product_families.json",
                                     new ProductFamilyWrapper( productFamily ), ProductFamilyWrapper.class )
        .getProductFamily();
  }

  @Override
  public ProductFamily findProductFamilyById( String id )
  {
    try
    {
      return httpClient.getForObject( "/product_families/" + id + ".json", ProductFamilyWrapper.class ).getProductFamily();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public List<ProductFamily> findAllProductFamilies()
  {
    return Arrays.stream( httpClient.getForObject( "/product_families.json", ProductFamilyWrapper[].class ) )
        .map( ProductFamilyWrapper::getProductFamily )
        .collect( Collectors.toList() );
  }

  @Override
  public ProductFamily archiveProductFamilyById( String id )
  {
    try
    {
      return httpClient.exchange( "/product_families/" + id + ".json", HttpMethod.DELETE, HttpEntity.EMPTY, ProductFamilyWrapper.class )
          .getBody()
          .getProductFamily();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Product createProduct( String productFamilyId, Product product )
  {
    return httpClient.postForObject( "/product_families/" + productFamilyId + "/products.json",
                                     new ProductWrapper( product ), ProductWrapper.class )
        .getProduct();
  }

  @Override
  public Product findProductById( String id )
  {
    try
    {
      return httpClient.getForObject( "/products/" + id + ".json", ProductWrapper.class )
          .getProduct();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Product findProductByApiHandle( String apiHandle )
  {
    try
    {
      return httpClient.getForObject( "/products/handle/" + apiHandle + ".json", ProductWrapper.class )
          .getProduct();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Set<ProductPricePoint> findProductPricePointsByProductId( String productId )
  {
    try
    {
      return httpClient.getForObject(
              "/products/" + productId + "/price_points.json", ProductPricePointsWrapper.class )
          .getPricePoints();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Set<PricePoint> findComponentPricePoints( int componentId )
  {
    try
    {
      return httpClient.getForObject(
              "/components/" + componentId + "/price_points.json", ComponentPricePointsWrapper.class )
          .getPricePoints();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public PricePoint updatePricePoint( int componentId, int pricePointId, PricePointUpdate pricePointUpdate )
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType( MediaType.APPLICATION_JSON );
    HttpEntity<PricePointUpdateWrapper> entity = new HttpEntity<>(
        new PricePointUpdateWrapper( pricePointUpdate ), headers );

    return httpClient.exchange(
        "/components/" + componentId + "/price_points/" + pricePointId + ".json",
        HttpMethod.PUT,
        entity,
        PricePointUpdateResultWrapper.class ).getBody().getPricePoint();
  }

  @Override
  public List<Product> findAllProducts()
  {
    return Arrays.stream( httpClient.getForObject( "/products.json", ProductWrapper[].class ) )
        .map( ProductWrapper::getProduct )
        .collect( Collectors.toList() );
  }

  @Override
  public List<Product> findProductsByProductFamilyId( String productFamilyId )
  {
    return Arrays.stream( httpClient.getForObject( "/product_families/" + productFamilyId + "/products.json",
                                                   ProductWrapper[].class ) )
        .map( ProductWrapper::getProduct )
        .collect( Collectors.toList() );
  }

  @Override
  public Product archiveProductById( String id )
  {
    try
    {
      return httpClient.exchange( "/products/" + id + ".json", HttpMethod.DELETE,
                                  HttpEntity.EMPTY, ProductWrapper.class )
          .getBody()
          .getProduct();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Subscription createSubscription( CreateSubscription subscription )
  {
    return httpClient.postForObject( "/subscriptions.json", new CreateSubscriptionWrapper( subscription ), SubscriptionWrapper.class )
        .getSubscription();
  }

  @Override
  public void updateSubscription( String subscriptionId, UpdateSubscription subscription )
  {
    httpClient.put( "/subscriptions/" + subscriptionId + ".json", new UpdateSubscriptionWrapper( subscription ) );
  }

  @Override
  public void updateSubscriptionNextBillingDate( String subscriptionId, LocalDateTime nextBillingDate )
  {
    updateSubscription(
        subscriptionId,
        UpdateSubscription.builder().nextBillingAt( ChargifyUtil.toChargifyDateString( nextBillingDate ) ).build()
    );
  }

  @Override
  public SubscriptionChargeResult createSubscriptionCharge( String subscriptionId, SubscriptionCharge subscriptionCharge )
  {
    return httpClient.postForObject( "/subscriptions/" + subscriptionId + "/charges.json",
                                     Map.of( "charge", SubscriptionChargePayload.from( subscriptionCharge ) ), SubscriptionChargeWrapper.class )
        .getSubscriptionChargeResult();
  }

  @Override
  public Subscription findSubscriptionById( String id )
  {
    try
    {
      return httpClient.getForObject(
              "/subscriptions/" + id + ".json", SubscriptionWrapper.class )
          .getSubscription();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public List<PaymentProfile> findPaymentProfilesForCustomer( String customerId )
  {
    try
    {
      return Arrays.stream( httpClient.getForObject( "/payment_profiles.json?customer_id=" + customerId, PaymentProfileWrapper[].class ) )
          .map( PaymentProfileWrapper::getPaymentProfile )
          .collect( Collectors.toList() );
    }
    catch( ResourceNotFoundException e )
    {
      return List.of();
    }
  }

  @Override
  public PaymentProfile createPaymentProfile( CreatePaymentProfile paymentProfile )
  {
    Map<String, Object> body = new HashMap<>();
    body.put( "payment_profile", paymentProfile );

    return httpClient.postForObject(
        "/payment_profiles.json", body, PaymentProfileWrapper.class ).getPaymentProfile();
  }

  @Override
  public void updatePaymentProfile( String paymentProfileId, UpdatePaymentProfile paymentProfile )
  {
    Map<String, Object> body = new HashMap<>();
    body.put( "payment_profile", paymentProfile );

    httpClient.put( "/payment_profiles/" + paymentProfileId + ".json", body );
  }

  @Override
  public PaymentProfile updateSubscriptionPaymentProfile( String subscriptionId, String paymentProfileId )
  {
    return httpClient.postForObject(
        "/subscriptions/" + subscriptionId + "/payment_profiles/" + paymentProfileId + "/change_payment_profile.json",
        Map.of(), PaymentProfileWrapper.class ).getPaymentProfile();
  }

  @Override
  public PaymentProfile findPaymentProfileById( String paymentProfileId )
  {
    try
    {
      return httpClient.getForObject( "/payment_profiles/" + paymentProfileId + ".json", PaymentProfileWrapper.class )
          .getPaymentProfile();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public void deleteUnusedPaymentProfile( String paymentProfileId )
  {
    httpClient.delete( "/payment_profiles/" + paymentProfileId + ".json" );
  }

  @Override
  public void deletePaymentProfile( String subscriptionId, String paymentProfileId )
  {
    httpClient.delete( "/subscriptions/" + subscriptionId + "/payment_profiles/" + paymentProfileId + ".json" );
  }

  @Override
  public List<Subscription> findSubscriptionsByCustomerId( String customerId )
  {
    return findSubscriptionsByCustomerId( customerId, 0, 200 );
  }

  @Override
  public List<Subscription> findSubscriptionsByCustomerId( String customerId, int pageNumber, int pageSize )
  {
    return Arrays.stream( httpClient.getForObject(
            "/customers/" + customerId + "/subscriptions.json?page=" + pageNumber + "&" + "per_page=" + pageSize,
            SubscriptionWrapper[].class ) )
        .map( SubscriptionWrapper::getSubscription )
        .collect( Collectors.toList() );
  }

  @Override
  public List<Subscription> findAllSubscriptions()
  {
    return Arrays.stream( httpClient.getForObject( "/subscriptions.json", SubscriptionWrapper[].class ) )
        .map( SubscriptionWrapper::getSubscription )
        .collect( Collectors.toList() );
  }

  @Override
  public Subscription purgeSubscription( Subscription subscription )
  {
    return httpClient.postForObject( "/subscriptions/" + subscription.getId() + "/purge.json?ack=" + subscription.getCustomer().getId() +
                                         "&cascade[]=customer&cascade[]=payment_profile",
                                     HttpEntity.EMPTY, SubscriptionWrapper.class )
        .getSubscription();
  }

  @Override
  public List<Subscription> findSubscriptionsByStateAndMetadata( String state, Map<String, String> metadata, int pageNumber, int pageSize )
  {
    StringBuilder fields = new StringBuilder();
    metadata.forEach( ( key, value ) -> fields.append( "&metadata[" ).append( key ).append( "]=" ).append( value ) );

    return Arrays.stream( httpClient.getForObject( "/subscriptions.json?page=" + pageNumber + "&" +
                                                       "per_page=" + pageSize + "&state=" + state + fields,
                                                   SubscriptionWrapper[].class ) )
        .map( SubscriptionWrapper::getSubscription )
        .collect( Collectors.toList() );
  }

  @Override
  public List<Subscription> findSubscriptionsByState( String state, int pageNumber, int pageSize )
  {
    return Arrays.stream( httpClient.getForObject( "/subscriptions.json?page=" + pageNumber + "&" +
                                                       "per_page=" + pageSize + "&state=" + state,
                                                   SubscriptionWrapper[].class ) )
        .map( SubscriptionWrapper::getSubscription )
        .collect( Collectors.toList() );
  }

  @Override
  public Subscription cancelSubscriptionById( String id )
  {
    try
    {
      return httpClient.exchange( "/subscriptions/" + id + ".json", HttpMethod.DELETE,
                                  HttpEntity.EMPTY, SubscriptionWrapper.class )
          .getBody()
          .getSubscription();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Subscription cancelSubscriptionProductChange( String subscriptionId )
  {
    final Subscription subscription = new Subscription();
    subscription.setNextProductId( "" );

    return httpClient.exchange( "/subscriptions/" + subscriptionId + ".json", HttpMethod.PUT,
                                new HttpEntity<>( new SubscriptionWrapper( subscription ) ), SubscriptionWrapper.class )
        .getBody()
        .getSubscription();
  }

  @Override
  public Subscription migrateSubscription( String subscriptionId, Migration migration )
  {
    return httpClient.postForObject( "/subscriptions/" + subscriptionId + "/migrations.json",
                                     new MigrationWrapper( migration ), SubscriptionWrapper.class )
        .getSubscription();
  }

  @Override
  public Subscription reactivateSubscription( String subscriptionId, boolean preserveBalance )
  {
    return httpClient.exchange( "/subscriptions/" + subscriptionId + "/reactivate.json", HttpMethod.PUT,
                                new HttpEntity<>( Map.of( "preserve_balance", preserveBalance ) ),
                                SubscriptionWrapper.class )
        .getBody()
        .getSubscription();
  }

  @Override
  public Subscription reactivateSubscription( String subscriptionId,
                                              SubscriptionReactivationData reactivationData )
  {
    return httpClient.exchange(
            prepareSubscriptionReactivationURI( subscriptionId, reactivationData ),
            HttpMethod.PUT,
            HttpEntity.EMPTY,
            SubscriptionWrapper.class
        )
        .getBody()
        .getSubscription();
  }

  @Override
  public ComponentPricePointUpdate migrateSubscriptionComponentToPricePoint( String subscriptionId,
                                                                             int componentId,
                                                                             String pricePointHandle )
  {
    return httpClient.postForObject( "/subscriptions/" + subscriptionId + "/price_points.json",
                                     new ComponentPricePointUpdatesWrapper(
                                         List.of( new ComponentPricePointUpdate( componentId, pricePointHandle ) ) ),
                                     ComponentPricePointUpdatesWrapper.class )
        .getPricePointUpdates().get( 0 );
  }

  @Override
  public List<ComponentPricePointUpdate> bulkUpdateSubscriptionComponentPricePoint( String subscriptionId,
                                                                                    List<ComponentPricePointUpdate> items )
  {
    return httpClient.postForObject( "/subscriptions/" + subscriptionId + "/price_points.json",
                                     new ComponentPricePointUpdatesWrapper( items ),
                                     ComponentPricePointUpdatesWrapper.class )
        .getPricePointUpdates();
  }

  @Override
  public Subscription cancelScheduledSubscriptionProductChange( String subscriptionId )
  {
    return httpClient.exchange( "/subscriptions/" + subscriptionId + ".json", HttpMethod.PUT,
                                new HttpEntity<>(
                                    Map.of(
                                        "subscription",
                                        Map.of(
                                            "next_product_id", "",
                                            "next_product_price_point_id", ""
                                        )
                                    )
                                ), SubscriptionWrapper.class )
        .getBody()
        .getSubscription();
  }

  @Override
  public Subscription changeSubscriptionProduct( String subscriptionId, SubscriptionProductUpdate payload )
  {
    return httpClient.exchange( "/subscriptions/" + subscriptionId + ".json", HttpMethod.PUT,
                                new HttpEntity<>( new SubscriptionProductUpdateWrapper( payload ) ), SubscriptionWrapper.class )
        .getBody()
        .getSubscription();
  }

  @Override
  public RenewalPreview previewSubscriptionRenewal( String subscriptionId )
  {
    return httpClient.postForObject( "/subscriptions/" + subscriptionId + "/renewals/preview.json",
                                     HttpEntity.EMPTY, RenewalPreviewWrapper.class )
        .getRenewalPreview();
  }

  @Override
  public List<Metadata> createSubscriptionMetadata( String subscriptionId, Metadata... metadata )
  {
    return Arrays.asList( httpClient.postForObject( "/subscriptions/" + subscriptionId + "/metadata.json",
                                                    new MetadataWrapper( metadata ), Metadata[].class ) );
  }

  @Override
  public SubscriptionMetadata readSubscriptionMetadata( String subscriptionId )
  {
    try
    {
      return httpClient.getForObject( "/subscriptions/" + subscriptionId + "/metadata.json",
                                      SubscriptionMetadata.class );
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public List<Metadata> updateSubscriptionMetadata( String subscriptionId, Metadata... metadata )
  {
    return Arrays.asList( httpClient.exchange( "/subscriptions/" + subscriptionId + "/metadata.json",
                                               HttpMethod.PUT,
                                               new HttpEntity<>( new MetadataWrapper( metadata ) ), Metadata[].class )
                              .getBody() );
  }

  @Override
  public Component createComponent( String productFamilyId, Component component )
  {
    if( component.getKind() == null )
      throw new IllegalArgumentException( "Component Kind must not be null" );

    final String pluralKindPathParam;
    final ComponentWrapper componentWrapper = switch( component.getKind() )
    {
      case quantity_based_component ->
      {
        pluralKindPathParam = "quantity_based_components";
        yield new QuantityBasedComponentWrapper( component );
      }
      case metered_component ->
      {
        pluralKindPathParam = "metered_components";
        yield new MeteredComponentWrapper( component );
      }
      case on_off_component ->
      {
        pluralKindPathParam = "on_off_components";
        yield new OnOffComponentWrapper( component );
      }
      default -> throw new IllegalArgumentException( "Invalid component kind - " + component.getKind() );
    };

    return httpClient.postForObject( "/product_families/" + productFamilyId + "/" + pluralKindPathParam + ".json",
                                     componentWrapper, AnyComponentWrapper.class )
        .getComponent();
  }

  @Override
  public Allocation createComponentAllocation( String subscriptionId, int componentId, Allocation allocation )
  {
    return httpClient.postForObject( "/subscriptions/" + subscriptionId + "/components/" + componentId +
                                         "/allocations.json",
                                     new AllocationWrapper( allocation ), AllocationWrapper.class )
        .getAllocation();
  }

  @Override
  public AllocationPreview previewComponentAllocation( String subscriptionId, int componentId, int quantity )
  {
    return httpClient.postForObject( "/subscriptions/" + subscriptionId + "/allocations/preview.json",
                                     Map.of( "allocations", List.of( new AllocationPreview.ComponentAllocationDTO( componentId, quantity ) ) ),
                                     AllocationPreviewWrapper.class )
        .getAllocationPreview();
  }

  @Override
  public List<Component> findComponentsByProductFamily( String productFamilyId )
  {
    return Arrays.stream( httpClient.getForObject( "/product_families/" + productFamilyId + "/components.json",
                                                   AnyComponentWrapper[].class ) )
        .map( AnyComponentWrapper::getComponent )
        .collect( Collectors.toList() );
  }

  @Override
  public Component findComponentByIdAndProductFamily( int componentId, String productFamilyId )
  {
    try
    {
      return httpClient.getForObject( "/product_families/" + productFamilyId +
                                          "/components/" + componentId + ".json",
                                      AnyComponentWrapper.class )
          .getComponent();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public ComponentWithPricePoints findComponentWithPricePointsByIdAndProductFamily( int componentId,
                                                                                    String productFamilyId )
  {
    return new ComponentWithPricePoints( findComponentByIdAndProductFamily( componentId, productFamilyId ),
                                         findComponentPricePoints( componentId ) );
  }

  @Override
  public List<SubscriptionComponent> findSubscriptionComponents( String subscriptionId )
  {
    return Arrays.stream( httpClient.getForObject( "/subscriptions/" + subscriptionId + "/components.json",
                                                   SubscriptionComponentWrapper[].class ) )
        .map( SubscriptionComponentWrapper::getComponent )
        .collect( Collectors.toList() );
  }

  @Override
  public List<SubscriptionStatement> findSubscriptionStatements(
      String subscriptionId, int page, int pageSize, String sort, String direction )
  {
    if( pageSize > 200 )
      throw new IllegalArgumentException( "Page size can't be bigger than 200" );

    StringBuilder uriBuilder = new StringBuilder();
    uriBuilder.append( "page=" ).append( page );
    uriBuilder.append( "&per_page=" ).append( pageSize );
    if( sort != null )
      uriBuilder.append( "&sort=" ).append( sort );
    if( direction != null )
      uriBuilder.append( "&direction=" ).append( direction );

    return Arrays.stream( httpClient.getForObject(
            "/subscriptions/" + subscriptionId + "/statements.json?" + uriBuilder, SubscriptionStatementWrapper[].class ) )
        .map( SubscriptionStatementWrapper::getStatement )
        .collect( Collectors.toList() );
  }

  @Override
  public List<Transaction> findSubscriptionTransactions( String subscriptionId, SubscriptionTransactionsSearchOptions options )
  {
    if( options.getPageSize() > 200 )
      throw new IllegalArgumentException( "Page size can't be bigger than 200" );

    StringBuilder uriBuilder = new StringBuilder();
    uriBuilder.append( "page=" ).append( options.getPage() );
    uriBuilder.append( "&per_page=" ).append( options.getPageSize() );
    uriBuilder.append( "&direction=" ).append( options.getDirection().getValue() );
    if( options.getMaxId() != null )
      uriBuilder.append( "&max_id=" ).append( options.getMaxId() );
    if( options.getSinceId() != null )
      uriBuilder.append( "&since_id=" ).append( options.getSinceId() );
    if( options.getKinds() != null )
      options.getKinds().forEach( kind -> uriBuilder.append( "&kinds[]=" ).append( kind ) );

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );
    if( options.getSinceDate() != null )
      uriBuilder.append( "&since_date=" ).append( options.getSinceDate().format( dateFormatter ) );
    if( options.getUntilDate() != null )
      uriBuilder.append( "&until_date=" ).append( options.getUntilDate().format( dateFormatter ) );

    return Arrays.stream( httpClient.getForObject(
            "/subscriptions/" + subscriptionId + "/transactions.json?" + uriBuilder, TransactionWrapper[].class ) )
        .map( TransactionWrapper::getTransaction )
        .collect( Collectors.toList() );
  }

  @Override
  public SubscriptionComponent findSubscriptionComponentById( String subscriptionId, int componentId )
  {
    try
    {
      return httpClient.getForObject( "/subscriptions/" + subscriptionId +
                                          "/components/" + componentId + ".json",
                                      SubscriptionComponentWrapper.class )
          .getComponent();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Usage reportSubscriptionComponentUsage( String subscriptionId, int componentId, Usage usage )
  {
    return httpClient.postForObject( "/subscriptions/" + subscriptionId + "/components/" + componentId +
                                         "/usages.json",
                                     new UsageWrapper( usage ), UsageWrapper.class )
        .getUsage();
  }

  @Override
  public Customer createCustomer( Customer customer )
  {
    return httpClient.postForObject( "/customers.json", new CustomerWrapper( customer ), CustomerWrapper.class )
        .getCustomer();
  }

  @Override
  public Customer updateCustomer( Customer customer )
  {
    return httpClient.exchange( "/customers/" + customer.getId() + ".json", HttpMethod.PUT,
                                new HttpEntity<>( new CustomerWrapper( customer ) ), CustomerWrapper.class )
        .getBody()
        .getCustomer();
  }

  @Override
  public Customer findCustomerById( String id )
  {
    try
    {
      return httpClient.getForObject( "/customers/" + id + ".json", CustomerWrapper.class )
          .getCustomer();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Customer findCustomerByReference( String reference )
  {
    try
    {
      return httpClient.getForObject( "/customers/lookup.json?reference={reference}",
                                      CustomerWrapper.class, reference )
          .getCustomer();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Subscription findSubscriptionByReference( String reference )
  {
    try
    {
      return httpClient.getForObject( "/subscriptions/lookup.json?reference={reference}",
                                      SubscriptionWrapper.class, reference )
          .getSubscription();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public List<Customer> findCustomersBy( Object criterion, int pageNumber )
  {
    return Arrays.stream( httpClient.getForObject( "/customers.json?q={criterion}&page={pageNumber}",
                                                   CustomerWrapper[].class, criterion, pageNumber ) )
        .map( CustomerWrapper::getCustomer )
        .collect( Collectors.toList() );
  }

  @Override
  public List<Customer> findAllCustomers()
  {
    return Arrays.stream( httpClient.getForObject( "/customers.json", CustomerWrapper[].class ) )
        .map( CustomerWrapper::getCustomer )
        .collect( Collectors.toList() );
  }

  @Override
  public List<Customer> findCustomers( int pageNumber, int perPage )
  {
    return Arrays.stream( httpClient.getForObject( String.format( "/customers.json?page=%s&per_page=%s", pageNumber, perPage ), CustomerWrapper[].class ) )
        .map( CustomerWrapper::getCustomer )
        .collect( Collectors.toList() );
  }

  @Override
  public void deleteCustomerById( String id )
  {
    try
    {
      httpClient.delete( "/customers/" + id + ".json" );
    }
    catch( ResourceNotFoundException ignored )
    {
    }
  }

  @Override
  public ReferralCode validateReferralCode( String code )
  {
    try
    {
      return httpClient.getForObject( "/referral_codes/validate.json?code=" + code,
                                      ReferralCodeWrapper.class )
          .getReferralCode();
    }
    catch( ResourceNotFoundException e )
    {
      return null;
    }
  }

  @Override
  public Adjustment adjust( String subscriptionId, Adjustment adjustment )
  {
    return httpClient.exchange( "/subscriptions/" + subscriptionId + "/adjustments.json", HttpMethod.POST,
                                new HttpEntity<>( new AdjustmentWrapper( adjustment ) ), AdjustmentWrapper.class )
        .getBody()
        .getAdjustment();
  }

  private String prepareSubscriptionReactivationURI( String subscriptionId,
                                                     SubscriptionReactivationData reactivationData )
  {
    StringBuilder urlBuilder = new StringBuilder( "/subscriptions/" ).append( subscriptionId ).append( "/reactivate.json" );

    urlBuilder.append( "?include_trial=" ).append( reactivationData.isIncludeTrial() ? "1" : "0" );
    urlBuilder.append( "&preserve_balance=" ).append( reactivationData.isPreserveBalance() ? "1" : "0" );
    if( reactivationData.getCouponCode() != null )
    {
      urlBuilder.append( "&coupon_code=" ).append( UriUtils.encode( reactivationData.getCouponCode(), StandardCharsets.UTF_8 ) );
    }
    if( reactivationData.getResume() != null )
    {
      urlBuilder.append( "&resume=" ).append( reactivationData.getResume() ? "true" : "false" );
    }
    if( reactivationData.isForgiveBalance() )
    {
      urlBuilder.append( "&resume%5Bforgive_balance%5D=" ).append( reactivationData.isForgiveBalance() ? "true" : "false" );
    }

    return urlBuilder.toString();
  }
}
