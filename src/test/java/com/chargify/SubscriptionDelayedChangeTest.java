package com.chargify;

import com.chargify.model.CreateSubscription;
import com.chargify.model.Customer;
import com.chargify.model.PricePointIntervalUnit;
import com.chargify.model.product.Product;
import com.chargify.model.product.ProductFamily;
import com.chargify.model.Subscription;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SubscriptionDelayedChangeTest extends ChargifyTest
{
  private static ProductFamily productFamily;
  private static Product initialProduct;
  private static Product targetProduct;
  private static Subscription subscription;
  private static Customer customer;

  @BeforeClass
  public static void setup()
  {
    productFamily = chargify.createProductFamily( new ProductFamily( randomName() ) );

    final Product initialProduct = new Product( randomName(), 0, 1, PricePointIntervalUnit.month );
    initialProduct.setRequestCreditCard( false );
    initialProduct.setRequireCreditCard( false );
    SubscriptionDelayedChangeTest.initialProduct = chargify.createProduct( productFamily.getId(), initialProduct );

    final Product targetProduct = new Product( randomName(), 0, 1, PricePointIntervalUnit.month );
    targetProduct.setRequestCreditCard( false );
    targetProduct.setRequireCreditCard( false );
    targetProduct.setHandle( randomName() );
    SubscriptionDelayedChangeTest.targetProduct = chargify.createProduct( productFamily.getId(), targetProduct );

    customer = chargify.createCustomer( new Customer( randomName(), randomName(), randomEmail() ) );

    final CreateSubscription subscription = new CreateSubscription();
    subscription.setProductId( SubscriptionDelayedChangeTest.initialProduct.getId() );
    subscription.setCustomerId( customer.getId() );
    SubscriptionDelayedChangeTest.subscription = chargify.createSubscription( subscription );
  }

  @Test
  public void delayedProductChangeShouldModifyNextProductIdAndNotChangeCurrentProduct()
  {
    final Subscription resultSubscription = chargify.changeSubscriptionProduct( subscription.getId(),
                                                                                targetProduct.getHandle(),
                                                                                true );
    assertNotNull( "Product change not scheduled", resultSubscription.getNextProductId() );
    assertEquals( "Scheduled change to wrong product", targetProduct.getId(), resultSubscription.getNextProductId() );
    assertEquals( "Current product changed", initialProduct.getId(), resultSubscription.getProduct().getId() );
  }

  @AfterClass
  public static void cleanup()
  {
    chargify.cancelSubscriptionProductChange( subscription.getId() );
    chargify.cancelSubscriptionById( subscription.getId() );
    chargify.archiveProductById( targetProduct.getId() );
    chargify.archiveProductById( initialProduct.getId() );
    chargify.archiveProductFamilyById( productFamily.getId() );
    // The customer could not be deleted because there are subscriptions associated with this account. You may want to delete the individual subscriptions first.
//    chargify.deleteCustomerById( customer.getId() );
  }
}
