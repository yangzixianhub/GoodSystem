package dev.wanheng.springjwtlogin.messaging;

public final class ConsistencyEventType {
    private ConsistencyEventType() {
    }

    public static final String ORDER_CREATED = "ORDER_CREATED";
    public static final String STOCK_RESERVED = "STOCK_RESERVED";
    public static final String STOCK_REJECTED = "STOCK_REJECTED";
    public static final String PAY_SUCCESS = "PAY_SUCCESS";
    public static final String ORDER_CANCEL_TIMEOUT = "ORDER_CANCEL_TIMEOUT";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
}
