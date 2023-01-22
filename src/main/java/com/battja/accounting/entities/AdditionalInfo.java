package com.battja.accounting.entities;

/**
 * Entity can be used for any structured additional info to pass along
 */
public class AdditionalInfo {

    private Account fundingSource;
    private Batch fromBatch;

    public AdditionalInfo() {}

    public Account getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(Account fundingSource) {
        this.fundingSource = fundingSource;
    }

    public Batch getFromBatch() {
        return fromBatch;
    }

    public void setFromBatch(Batch fromBatch) {
        this.fromBatch = fromBatch;
    }
}
