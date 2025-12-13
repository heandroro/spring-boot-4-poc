package com.example.poc.infrastructure.mapping;

import com.example.poc.domain.Customer;
import com.example.poc.domain.vo.Address;
import com.example.poc.domain.vo.Email;
import com.example.poc.domain.vo.Money;
import com.example.poc.web.CustomerDto;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-13T17:58:39-0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public CustomerDto toDto(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        String email = null;
        String street = null;
        String city = null;
        String state = null;
        String postalCode = null;
        String country = null;
        BigDecimal creditLimit = null;
        BigDecimal availableCredit = null;
        String status = null;
        String id = null;
        String name = null;

        email = customerEmailValue( customer );
        street = customerAddressStreet( customer );
        city = customerAddressCity( customer );
        state = customerAddressState( customer );
        postalCode = customerAddressPostalCode( customer );
        country = customerAddressCountry( customer );
        creditLimit = customerCreditLimitAmount( customer );
        availableCredit = customerAvailableCreditAmount( customer );
        if ( customer.getStatus() != null ) {
            status = customer.getStatus().name();
        }
        id = customer.getId();
        name = customer.getName();

        CustomerDto customerDto = new CustomerDto( id, name, email, street, city, state, postalCode, country, creditLimit, availableCredit, status );

        return customerDto;
    }

    private String customerEmailValue(Customer customer) {
        Email email = customer.getEmail();
        if ( email == null ) {
            return null;
        }
        return email.value();
    }

    private String customerAddressStreet(Customer customer) {
        Address address = customer.getAddress();
        if ( address == null ) {
            return null;
        }
        return address.street();
    }

    private String customerAddressCity(Customer customer) {
        Address address = customer.getAddress();
        if ( address == null ) {
            return null;
        }
        return address.city();
    }

    private String customerAddressState(Customer customer) {
        Address address = customer.getAddress();
        if ( address == null ) {
            return null;
        }
        return address.state();
    }

    private String customerAddressPostalCode(Customer customer) {
        Address address = customer.getAddress();
        if ( address == null ) {
            return null;
        }
        return address.postalCode();
    }

    private String customerAddressCountry(Customer customer) {
        Address address = customer.getAddress();
        if ( address == null ) {
            return null;
        }
        return address.country();
    }

    private BigDecimal customerCreditLimitAmount(Customer customer) {
        Money creditLimit = customer.getCreditLimit();
        if ( creditLimit == null ) {
            return null;
        }
        return creditLimit.amount();
    }

    private BigDecimal customerAvailableCreditAmount(Customer customer) {
        Money availableCredit = customer.getAvailableCredit();
        if ( availableCredit == null ) {
            return null;
        }
        return availableCredit.amount();
    }
}
