package com.hhl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataItem {
	private long id;
    private String reward_date; // Thêm thuộc tính rewardDate
    private String partner_account;
    private String client_account_type;
    private String country;
    private String client_uid;
    private String currency;
    private double volume_lots;
    private double volume_mln_usd;
    private String reward;
    private String reward_usd;
    private int orders_count;
    private String reward_order;
    private String partner_account_name;
    private long client_account;
    private String link_code;
}
