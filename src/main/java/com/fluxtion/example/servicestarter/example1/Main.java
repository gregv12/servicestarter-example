package com.fluxtion.example.servicestarter.example1;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import com.fluxtion.example.servicestater.helpers.AsynchronousTaskExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    public static final String ORDER_GATEWAY = "orderGateway";
    public static final String LIMIT_READER = "limitReader";
    public static final String MARKET_DATA_GATEWAY = "marketDataGateway";
    public static final String PNL_CHECK = "pnlCheck";
    public static final String ORDER_PROCESSOR = "orderProcessor";
    public static final String INTERNAL_ORDER_SOURCE = "internalOrderSource";
    public static final String ORDER_AUDIT = "orderAudit";
    public static final String VALID_ORDER_PUBLISHER = "validOrderPublisher";

    public static void main(String[] args) {
        Service orderGateway = Service.builder(ORDER_GATEWAY)
                .startTask(Main::emptyTask)
                .stopTask(Main::emptyTask)
                .build();
        Service limitReader = Service.builder(LIMIT_READER)
                .startTask(Main::emptyTask)
                .stopTask(Main::emptyTask)
                .build();
        Service marketDataGateway = Service.builder(MARKET_DATA_GATEWAY)
                .startTask(Main::emptyTask)
                .stopTask(Main::emptyTask)
                .build();
        Service pnlCheck = Service.builder(PNL_CHECK)
                .requiredServices(limitReader, marketDataGateway)
                .servicesThatRequireMe(orderGateway)
                .stopTask(Main::emptyTask)
                .startTask(Main::emptyTask)
                .build();
        Service orderProcessor = Service.builder(ORDER_PROCESSOR)
                .servicesThatRequireMe(pnlCheck)
                .stopTask(Main::emptyTask)
                .startTask(Main::emptyTask)
                .build();
        //internal orders, do not require pnl check, feed straight into order processor
        Service internalOrderSource = Service.builder(INTERNAL_ORDER_SOURCE)
                .requiredServices(orderProcessor)
                .stopTask(Main::emptyTask)
                .startTask(Main::emptyTask)
                .build();
        //writes valid orders to an audit file for regulators
        Service orderAudit = Service.builder(ORDER_AUDIT)
                .servicesThatRequireMe(orderProcessor)
                .stopTask(Main::emptyTask)
                .startTask(Main::emptyTask)
                .build();
        //publishes valid orders
        Service validOrderPublisher = Service.builder(VALID_ORDER_PUBLISHER)
                .servicesThatRequireMe(orderProcessor)
                .stopTask(Main::emptyTask)
                .startTask(Main::emptyTask)
                .build();


        ServiceManager svcManager = ServiceManager.build(
                orderGateway,
                limitReader,
                marketDataGateway,
                pnlCheck,
                orderProcessor,
                internalOrderSource,
                orderAudit,
                validOrderPublisher
        );
        svcManager.registerTaskExecutor(new AsynchronousTaskExecutor());
        svcManager.triggerDependentsOnNotification(true);
        svcManager.triggerNotificationOnSuccessfulTaskExecution(true);
        svcManager.registerStatusListener(new GuiMain(svcManager)::logStatus);
    }


    @SneakyThrows
    public static void emptyTask() {
        Thread.sleep(1_500);
    }
}
