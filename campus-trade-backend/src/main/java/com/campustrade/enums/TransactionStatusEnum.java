package com.campustrade.enums;
public enum TransactionStatusEnum {
    PENDING("待沟通"), PAID("已付款"), COMPLETED("已完成"), CANCELLED("已取消");
    private final String desc;
    TransactionStatusEnum(String d) { this.desc = d; }
    public String getDesc() { return desc; }
}
