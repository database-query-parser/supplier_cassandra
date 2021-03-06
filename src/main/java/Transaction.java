import java.math.BigDecimal;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;

import transaction.*;

/**
 * Implementation of the eight transaction types.
 * 1) New Order Transaction
 * 2) Payment Transaction
 * 3) Delivery Transaction
 * 4) Order-Status Transaction
 * 5) Stock-level Transaction
 * 6) Popular-Item Transaction
 * 7) Top-Balance Transaction
 * 8) Related-Customer Transaction
 */
public class Transaction {
    public static String[] CONTACT_POINTS = null;
    public static String KEY_SPACE = null;

    private Session session;
    private NewOrderTransaction newOrderTransaction;
    private PaymentTransaction paymentTransaction;
    private DeliveryTransaction deliveryTransaction;
    private OrderStatusTransaction orderStatusTransaction;
    private StockLevelTransaction stockLevelTransaction;
    private PopularItemTransaction popularItemTransaction;
    private TopBalanceTransaction topBalanceTransaction;
    private RelatedCustomerTransaction relatedCustomerTransaction;

    public Transaction(int index, String consistencyLevel, String[] contactPoints, String keySpace) {
        this.CONTACT_POINTS = contactPoints;
        this.KEY_SPACE = keySpace;
        int numContactPoints = CONTACT_POINTS.length;

        QueryOptions queryOptions;
        if (consistencyLevel.equalsIgnoreCase("ONE")) {
            queryOptions = new QueryOptions().setConsistencyLevel(ConsistencyLevel.ONE);
        } else {
            queryOptions = new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM);
        }

        Cluster cluster = Cluster.builder()
                .addContactPoint(CONTACT_POINTS[index % numContactPoints])
                .withQueryOptions(queryOptions)
                .build();
        session = cluster.connect();

        newOrderTransaction = new NewOrderTransaction(session, this.KEY_SPACE);
        paymentTransaction = new PaymentTransaction(session, this.KEY_SPACE);
        deliveryTransaction = new DeliveryTransaction(session, this.KEY_SPACE);
        orderStatusTransaction = new OrderStatusTransaction(session, this.KEY_SPACE);
        stockLevelTransaction = new StockLevelTransaction(session, this.KEY_SPACE);
        popularItemTransaction = new PopularItemTransaction(session, this.KEY_SPACE);
        topBalanceTransaction = new TopBalanceTransaction(session, this.KEY_SPACE);
        relatedCustomerTransaction = new RelatedCustomerTransaction(session, this.KEY_SPACE);
    }

    public void processNewOrder(int wId, int dId, int cId, int numItems,
            int[] itemNum, int[] supplierWarehouse, int[] qty) {
        newOrderTransaction.processOrder(wId, dId, cId, numItems, itemNum, supplierWarehouse, qty);
    }

    public void processPayment(int wId, int dId, int cId, float payment) {
        paymentTransaction.processPaymentTransaction(wId, dId, cId, payment);
    }

    public void processDelivery(int wId, int carrierId) {
        deliveryTransaction.processDeliveryTransaction(wId, carrierId);
    }

    public void processOrderStatus(int wId, int dId, int cId) {
        orderStatusTransaction.processOrderStatus(wId, dId, cId);
    }

    public void processStockLevel(int wId, int dId, BigDecimal T, int L) {
        stockLevelTransaction.processStockLevelTransaction(wId, dId, T, L);
    }

    public void processPopularItem(int wId, int dId, int L) {
        popularItemTransaction.popularItem(wId, dId, L);
    }

    public void processTopBalance() {
        topBalanceTransaction.calTopBalance();
    }

    public void processRelatedCustomer(int wId, int dId, int cId) {
        relatedCustomerTransaction.relatedCustomer(wId, dId, cId);
    }
}
