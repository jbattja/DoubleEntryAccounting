package com.battja.accounting.services;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Amount;
import com.battja.accounting.entities.Route;
import com.battja.accounting.repositories.RoutingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    public Account getSettlementAccount(Account merchant, Amount.Currency settlementCurrency) {
        // get entries for the merchant.
        List<Route> routingList = routingRepository.findByMerchantAndRoutingTypeAndCurrency(merchant, Route.RoutingType.SETTLEMENT,settlementCurrency);
        if (routingList.isEmpty()) {
            routingList = routingRepository.findByMerchantAndRoutingTypeAndCurrency(null, Route.RoutingType.SETTLEMENT,settlementCurrency);
        }
        routingList = routingList.stream().filter(route -> route.getCurrency() == settlementCurrency).toList();
        if (routingList.isEmpty()) {
            log.warn("No accounts found to settle to for currency " + settlementCurrency + " and merchant " + merchant);
            return null;
        }
        // Multiple entries can be used... we'll return a random one (this can be setup for A/B testing)
        return routingList.get((int) (Math.random() * routingList.size())).getTargetAccount();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            System.out.println((int) (Math.random() * 1));
        }
    }

    public Route addRoute(Route route) {
        route = routingRepository.save(route);
        log.info("Created route:" + route);
        return route;
    }

    @Autowired
    RoutingRepository routingRepository;

}
