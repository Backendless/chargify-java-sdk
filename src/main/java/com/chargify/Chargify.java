package com.chargify;

import com.chargify.model.*;
import com.chargify.model.product.Product;
import com.chargify.model.product.ProductFamily;
import com.chargify.model.product.ProductPricePoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface Chargify
{
  ProductFamily createProductFamily( ProductFamily productFamily );

  ProductFamily findProductFamilyById( String id );

  List<ProductFamily> findAllProductFamilies();

  ProductFamily archiveProductFamilyById( String id );

  Product createProduct( String productFamilyId, Product product );

  Product findProductById( String id );

  Product findProductByApiHandle( String apiHandle );

  Set<ProductPricePoint> findProductPricePointsByProductId( String productId );

  Set<PricePoint> findComponentPricePoints( int componentId );

  List<Product> findAllProducts();

  List<Product> findProductsByProductFamilyId( String productFamilyId );

  Product archiveProductById( String id );

  Subscription createSubscription( CreateSubscription subscription );

  void updateSubscriptionNextBillingDate( String subscriptionId, LocalDateTime nextBillingDate );

  SubscriptionChargeResult createSubscriptionCharge( String subscriptionId, SubscriptionCharge subscriptionCharge );

  Subscription findSubscriptionById( String id );

  List<Subscription> findSubscriptionsByCustomerId( String customerId );

  List<Subscription> findAllSubscriptions();

  List<Subscription> findSubscriptionsByState( String state, int pageNumber, int pageSize );

  Subscription cancelSubscriptionById( String id );

  Subscription cancelSubscriptionProductChange( String subscriptionId );

  Subscription migrateSubscription( String subscriptionId, String productHandle );

  Subscription migrateSubscription( String subscriptionId, String productHandle, String pricePointHandle );

  Subscription reactivateSubscription( String subscriptionId );

  Subscription reactivateSubscription( String subscriptionId, SubscriptionReactivationData reactivationData );

  ComponentPricePointUpdate migrateSubscriptionComponentToPricePoint( String subscriptionId, int componentId,
                                                                      String pricePointHandle );

  List<ComponentPricePointUpdate> bulkUpdateSubscriptionComponentPricePoint( String subscriptionId, List<ComponentPricePointUpdate> items );

  Subscription cancelScheduledSubscriptionProductChange( String subscriptionId );

  Subscription changeSubscriptionProduct( String subscriptionId, String productHandle, boolean delayed );

  Subscription changeSubscriptionProduct( String subscriptionId, String productHandle, String pricePointHandle, boolean delayed );

  RenewalPreview previewSubscriptionRenewal( String subscriptionId );

  List<Metadata> createSubscriptionMetadata( String subscriptionId, Metadata... metadata );

  SubscriptionMetadata readSubscriptionMetadata( String subscriptionId );

  List<Metadata> updateSubscriptionMetadata( String subscriptionId, Metadata... metadata );

  Component createComponent( String productFamilyId, Component component );

  Allocation createComponentAllocation( String subscriptionId, int componentId, Allocation allocation );

  AllocationPreview previewComponentAllocation( String subscriptionId, int componentId, int quantity );

  List<Component> findComponentsByProductFamily( String productFamilyId );

  Component findComponentByIdAndProductFamily( int componentId, String productFamilyId );

  ComponentWithPricePoints findComponentWithPricePointsByIdAndProductFamily( int componentId, String productFamilyId );

  List<SubscriptionComponent> findSubscriptionComponents( String subscriptionId );

  SubscriptionComponent findSubscriptionComponentById( String subscriptionId, int componentId );

  Usage reportSubscriptionComponentUsage( String subscriptionId, int componentId, Usage usage );

  Customer createCustomer( Customer customer );

  Customer updateCustomer( Customer customer );

  Customer findCustomerById( String id );

  Customer findCustomerByReference( String reference );

  List<Customer> findAllCustomers();

  void deleteCustomerById( String id );

  ReferralCode validateReferralCode( String code );

  Adjustment adjust( String subscriptionId, Adjustment adjustment );
}
