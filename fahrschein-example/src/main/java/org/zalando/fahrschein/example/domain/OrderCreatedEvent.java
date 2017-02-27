package org.zalando.fahrschein.example.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by akukuljac on 27/02/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCreatedEvent {

    private String orderNumber;

    private Integer appDomainId;

    private String customerNumber;

    private Integer grandTotal;

    private String shippingCountry;

    private String paymentMethod;

    private String couponCode;

    private Integer itemCount;

    private String partnerIds;

    private MetaData metaData;

    public OrderCreatedEvent() {
    }

    @JsonCreator
    public OrderCreatedEvent(@JsonProperty("orderNumber") String orderNumber,
                             @JsonProperty("appDomainId") Integer appDomainId, @JsonProperty("grandTotal") Integer grandTotal,
                             @JsonProperty("shippingCountry") String shippingCountry,
                             @JsonProperty("paymentMethod") String paymentMethod, @JsonProperty("customerNumber") String customerNumber,
                             @JsonProperty("couponCode") String couponCode, @JsonProperty("itemCount") Integer itemCount,
                             @JsonProperty("partnerIds") String partnerIds, @JsonProperty("metadata") MetaData metaData) {
        this.orderNumber = orderNumber;
        this.appDomainId = appDomainId;
        this.grandTotal = grandTotal;
        this.shippingCountry = shippingCountry;
        this.paymentMethod = paymentMethod;
        this.customerNumber = customerNumber;
        this.couponCode = couponCode;
        this.itemCount = itemCount;
        this.partnerIds = partnerIds;
        this.metaData = metaData;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Integer getAppDomainId() {
        return appDomainId;
    }

    public void setAppDomainId(Integer appDomainId) {
        this.appDomainId = appDomainId;
    }

    public Integer getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Integer grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public String getPartnerIds() {
        return partnerIds;
    }

    public void setPartnerIds(String partnerIds) {
        this.partnerIds = partnerIds;
    }

}
