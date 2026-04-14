package com.bank.cuentamovimiento.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClienteEventConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void recibirEventoCliente(String mensaje) {
        log.info("Evento recibido desde ms-cliente-persona: {}", mensaje);
        // Aquí se pueden procesar eventos del microservicio de clientes
        // Por ejemplo, actualizar datos locales del cliente en las cuentas
    }
}
