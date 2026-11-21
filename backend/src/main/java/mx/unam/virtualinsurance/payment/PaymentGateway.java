package mx.unam.virtualinsurance.payment;

public interface PaymentGateway {

	GatewayPaymentResult authorize(GatewayPaymentCommand command);
}

