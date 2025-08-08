package com.chargify;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

  PricePoint updatePricePoint( int componentId, int pricePointId, PricePointUpdate pricePointUpdate );

  List<Product> findAllProducts();

  List<Product> findProductsByProductFamilyId( String productFamilyId );

  Product archiveProductById( String id );

  Subscription createSubscription( CreateSubscription subscription );

  void updateSubscription( String subscriptionId, UpdateSubscription subscription );

  void updateSubscriptionNextBillingDate( String subscriptionId, LocalDateTime nextBillingDate );

  SubscriptionChargeResult createSubscriptionCharge( String subscriptionId, SubscriptionCharge subscriptionCharge );

  Subscription findSubscriptionById( String id );

  List<PaymentProfile> findPaymentProfilesForCustomer( String customerId );

  PaymentProfile createPaymentProfile( CreatePaymentProfile paymentProfile );

  void updatePaymentProfile( String paymentProfileId, UpdatePaymentProfile paymentProfile );

  PaymentProfile updateSubscriptionPaymentProfile( String subscriptionId, String paymentProfileId );

  PaymentProfile findPaymentProfileById( String paymentProfileId );

  void deleteUnusedPaymentProfile( String paymentProfileId );

  void deletePaymentProfile( String subscriptionId, String paymentProfileId );

  List<Subscription> findSubscriptionsByCustomerId( String customerId );

  List<Subscription> findSubscriptionsByCustomerId( String customerId, int pageNumber, int pageSize );

  List<Subscription> findAllSubscriptions();

  Subscription purgeSubscription( Subscription subscription );

  List<Subscription> findSubscriptionsByState( String state, int pageNumber, int pageSize );

  List<Subscription> findSubscriptionsByStateAndMetadata( String state, Map<String, String> metadata, int pageNumber, int pageSize );

  Subscription cancelSubscriptionById( String id );

  Subscription cancelSubscriptionProductChange( String subscriptionId );

  Subscription migrateSubscription( String subscriptionId, Migration migration );

  Subscription reactivateSubscription( String subscriptionId, boolean preserveBalance );

  Subscription reactivateSubscription( String subscriptionId, SubscriptionReactivationData reactivationData );

  ComponentPricePointUpdate migrateSubscriptionComponentToPricePoint( String subscriptionId, int componentId,
                                                                      String pricePointHandle );

  List<ComponentPricePointUpdate> bulkUpdateSubscriptionComponentPricePoint( String subscriptionId, List<ComponentPricePointUpdate> items );

  Subscription cancelScheduledSubscriptionProductChange( String subscriptionId );

  Subscription changeSubscriptionProduct( String subscriptionId, SubscriptionProductUpdate payload );

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

  List<SubscriptionStatement> findSubscriptionStatements(
      String subscriptionId, int page, int pageSize, String sort, String direction );

  List<Transaction> findSubscriptionTransactions( String subscriptionId, SubscriptionTransactionsSearchOptions options );

  SubscriptionComponent findSubscriptionComponentById( String subscriptionId, int componentId );

  Usage reportSubscriptionComponentUsage( String subscriptionId, int componentId, Usage usage );

  Customer createCustomer( Customer customer );

  Customer updateCustomer( Customer customer );

  Customer findCustomerById( String id );

  Customer findCustomerByReference( String reference );

  Subscription findSubscriptionByReference( String reference );

  /**
   * Search to retrieve a single or group of customers.
   *
   * @param criterion  (string or integer) - can be email, Chargify ID, Reference (Your App), Organization
   * @param pageNumber (start from 1) the page parameter via the query string to access subsequent pages of 50 transactions
   * @return List of customers
   */
  List<Customer> findCustomersBy( Object criterion, int pageNumber );

  /**
   * The first page of results is displayed
   * Default value for per_page is 50
   * For page settings and how many records to fetch in each request (perPage), use
   * {@link #findCustomers(int pageNumber, int perPage)}
   */
  List<Customer> findAllCustomers();

  List<Customer> findCustomers( int pageNumber, int perPage );

  void deleteCustomerById( String id );

  ReferralCode validateReferralCode( String code );

  Adjustment adjust( String subscriptionId, Adjustment adjustment );
}
