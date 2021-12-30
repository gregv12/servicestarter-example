package com.fluxtion.example.servicestarter.example1;

import com.fluxtion.example.servicestarter.example1.gui.ServiceManagaerFrame;
import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.helpers.AsynchronousTaskExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static final String ORDER_GATEWAY = "orderGateway";
    public static final String LIMIT_READER = "limitReader";
    public static final String MARKET_DATA_GATEWAY = "marketDataGateway";
    public static final String PNL_CHECK = "pnlCheck";
    public static final String ORDER_PROCESSOR = "orderProcessor";
    public static final String INTERNAL_ORDER_SOURCE = "internalOrderSource";
    public static final String ORDER_AUDIT = "orderAudit";
    public static final String RISK_MANAGER = "riskManager";

    public static void main(String[] args) {
        Service orderGateway = Service.builder(ORDER_GATEWAY)
                .startTask(Main::emptyTask)
                .stopTask(Main::emptyTask)
                .build();
        //push profitability limits to pnlCheck
        Service limitReader = Service.builder(LIMIT_READER)
                .startTask(Main::emptyTask)
                .stopTask(Main::emptyTask)
                .build();
        //pushes market data to pnlCheck
        Service marketDataGateway = Service.builder(MARKET_DATA_GATEWAY)
                .startTask(Main::emptyTask)
                .stopTask(Main::emptyTask)
                .build();
        //carries out pnl check on incoming orders - has a complex dependency relationship
        Service pnlCheck = Service.builder(PNL_CHECK)
                .requiredServices(limitReader, marketDataGateway)
                .servicesThatRequireMe(orderGateway)
                .stopTask(Main::emptyTask)
                .startTask(Main::emptyTask)
                .build();
        //processes valid orders
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
        Service riskManager = Service.builder(RISK_MANAGER)
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
                riskManager
        );
        svcManager.registerTaskExecutor(new AsynchronousTaskExecutor());
        svcManager.triggerDependentsOnNotification(true);
        svcManager.triggerNotificationOnSuccessfulTaskExecution(true);
        svcManager.registerStatusListener(new ServiceManagaerFrame(svcManager)::logStatus);
    }


    @SneakyThrows
    public static void emptyTask() {
        Thread.sleep(1_500);
    }
}
