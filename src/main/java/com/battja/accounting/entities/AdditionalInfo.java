package com.battja.accounting.entities;

/**
 * Entity can be used for any structured additional info to pass along
 */
public class AdditionalInfo {

    private Account fundingSource;

    public AdditionalInfo() {}

    public Account getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(Account fundingSource) {
        this.fundingSource = fundingSource;
    }
}
