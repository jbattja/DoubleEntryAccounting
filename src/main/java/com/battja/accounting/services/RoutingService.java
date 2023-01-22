package com.battja.accounting.services;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Amount;
import com.battja.accounting.entities.Route;
import com.battja.accounting.entities.Transaction;
import com.battja.accounting.repositories.RoutingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    public Account getSettlementAccount(@NonNull Account merchant, @NonNull Amount.Currency settlementCurrency) {
        // get entries for the merchant.
        List<Route> routingList = routingRepository.findByMerchantAndRoutingTypeAndCurrency(merchant, Route.RoutingType.SETTLEMENT,settlementCurrency);
        if (routingList.isEmpty()) {
            routingList = routingRepository.findByMerchantAndRoutingTypeAndCurrency(null, Route.RoutingType.SETTLEMENT,settlementCurrency);
        }
        if (routingList.isEmpty()) {
            log.warn("No accounts found to settle to for currency " + settlementCurrency + " and merchant " + merchant);
            return null;
        }
        // Multiple entries can be used... we'll return a random one (this can be setup for A/B testing)
        return routingList.get((int) (Math.random() * routingList.size())).getTargetAccount();
    }

    public Account getPartnerAccount(@NonNull Account merchant, @NonNull Transaction payment) {
        // OPTION 1: get entries for the merchant with all criteria
        List<Route> routingList = routingRepository.findByMerchantAndRoutingTypeAndCurrencyAndPaymentMethod(
                merchant, Route.RoutingType.PAYMENT,payment.getCurrency(),payment.getPaymentMethod());
        if (routingList.isEmpty()) {
            // OPTION 2: same merchant, but any currencies
            routingList = routingRepository.findByMerchantAndRoutingTypeAndCurrencyAndPaymentMethod(
                    merchant, Route.RoutingType.PAYMENT,null,payment.getPaymentMethod());
        }
        if (routingList.isEmpty()) {
            // OPTION 3: all merchants, but with payment currency
            routingList = routingRepository.findByMerchantAndRoutingTypeAndCurrencyAndPaymentMethod(
                    null, Route.RoutingType.PAYMENT,payment.getCurrency(),payment.getPaymentMethod());
        }
        if (routingList.isEmpty()) {
            // LAST OPTION: all merchants, any currency
            routingList = routingRepository.findByMerchantAndRoutingTypeAndCurrencyAndPaymentMethod(
                    null, Route.RoutingType.PAYMENT,null,payment.getPaymentMethod());
        }
        if (routingList.isEmpty()) {
            log.warn("No accounts found to route this payment to: " + payment);
            return null;
        }
        // Multiple entries can be used... we'll return a random one (this can be setup for A/B testing)
        return routingList.get((int) (Math.random() * routingList.size())).getTargetAccount();
    }

    public Route addRoute(Route route) {
        route = routingRepository.save(route);
        log.info("Created route:" + route);
        return route;
    }

    @Autowired
    RoutingRepository routingRepository;

}
