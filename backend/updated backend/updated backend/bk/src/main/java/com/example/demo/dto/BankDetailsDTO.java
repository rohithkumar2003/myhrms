package com.example.demo.dto;

import com.example.demo.model.BankDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDetailsDTO {
	private Long id;  // 👈 Add this
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private String branch;
    
    public static BankDetailsDTO fromEntity(BankDetails bankDetails) {
        if (bankDetails == null) return null;
        
        return BankDetailsDTO.builder()
        		  .id(bankDetails.getId()) 
                .accountNumber(bankDetails.getAccountNumber())
                .bankName(bankDetails.getBankName())
                .ifscCode(bankDetails.getIfscCode())
                .branch(bankDetails.getBranch())
                .build();
    }
    
    public BankDetails toEntity() {
        return BankDetails.builder()
        		 .id(this.id)
                .accountNumber(this.accountNumber)
                .bankName(this.bankName)
                .ifscCode(this.ifscCode)
                .branch(this.branch)
                .build();
    }
}