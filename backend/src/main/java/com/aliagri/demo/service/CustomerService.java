package com.aliagri.demo.service;

import com.aliagri.demo.dto.customerdto.CustomerRequest;
import com.aliagri.demo.dto.customerdto.CustomerResponse; // Updated import
import com.aliagri.demo.model.Customer;
import com.aliagri.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public CustomerResponse getById(Long id) {
        return mapToResponse(requireCustomer(id));
    }

    public CustomerResponse create(CustomerRequest request) {
        Customer customer = new Customer(request.name(), request.idCardNumber(), request.address());
        return mapToResponse(customerRepository.save(customer));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = requireCustomer(id);
        customer.setName(request.name());
        customer.setIdCardNumber(request.idCardNumber());
        customer.setAddress(request.address());
        return mapToResponse(customerRepository.save(customer));
    }

    public void delete(Long id) {
        customerRepository.delete(requireCustomer(id));
    }

    private Customer requireCustomer(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getIdCardNumber(), customer.getAddress());
    }
}