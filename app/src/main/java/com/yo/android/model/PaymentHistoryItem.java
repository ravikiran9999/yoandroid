package com.yo.android.model;

public class PaymentHistoryItem {
    private String current_credit;

    private String updated_at;

    private String added_credit;

    private String status;

    private String existing_credit;

    private boolean arrowDown;

    public String getCurrent_credit() {
        return current_credit;
    }

    public void setCurrent_credit(String current_credit) {
        this.current_credit = current_credit;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getAddedCredit() {
        return added_credit;
    }

    public void setAdded_credit(String added_credit) {
        this.added_credit = added_credit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExisting_credit() {
        return existing_credit;
    }

    public void setExisting_credit(String existing_credit) {
        this.existing_credit = existing_credit;
    }

    @Override
    public String toString() {
        return "ClassPojo [current_credit = " + current_credit + ", updated_at = " + updated_at + ", added_credit = " + added_credit + ", status = " + status + ", existing_credit = " + existing_credit + "]";
    }

    public boolean isArrowDown() {
        return arrowDown;
    }

    public void setArrowDown(boolean arrowDown) {
        this.arrowDown = arrowDown;
    }
}