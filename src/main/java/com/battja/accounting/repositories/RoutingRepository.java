package com.battja.accounting.repositories;

import com.battja.accounting.entities.Account;
import com.battja.accounting.entities.Amount;
import com.battja.accounting.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutingRepository extends JpaRepository<Route, Integer> {

    List<Route> findByMerchantAndRoutingTypeAndCurrency(Account merchant, Route.RoutingType routingType, Amount.Currency currency);

}
