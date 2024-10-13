package com.kassandra.service;

import com.kassandra.domain.PaymentMethod;
import com.kassandra.domain.PaymentOrderStatus;
import com.kassandra.modal.PaymentOrder;
import com.kassandra.modal.User;
import com.kassandra.repository.PaymentOrderRepository;
import com.kassandra.response.PaymentResponse;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecretKey;

    @Override
    public PaymentOrder createOrder(User user, Long amount, PaymentMethod paymentMethod) {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setUser(user);
        paymentOrder.setAmount(amount);
        paymentOrder.setPaymentMethod(paymentMethod);
        return paymentOrderRepository.save(paymentOrder);
        ;
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long id) throws Exception {
        return paymentOrderRepository.findById(id).orElseThrow(
                () ->new Exception("payment order not found"));
    }

    @Override
    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws RazorpayException {
        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
            if(paymentOrder.getPaymentMethod().equals(PaymentMethod.RAZORPAY)){
                RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecretKey);
                Payment payment = razorpay.payments.fetch(paymentId);
                Integer amount = payment.get("amount");
                String status = payment.get("status");

                if (status.equals("captured")){
                    paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                    return true;
                }
                paymentOrder.setStatus(PaymentOrderStatus.FAILED);
                paymentOrderRepository.save(paymentOrder);
                return false;



            }
        }
        return null;
    }

    @Override
    public PaymentResponse createRazorpayPaymentLing(User user, Long amount) {
        return null;
    }

    @Override
    public PaymentResponse createStripePaymentLing(User user, Long orderID) {
        return null;
    }
}
