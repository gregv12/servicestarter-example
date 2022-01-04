package com.fluxtion.example.servicestarter.example1;

import com.fluxtion.example.servicestarter.example1.aot.Processor;
import com.fluxtion.example.servicestarter.example1.gui.ServiceManagaerFrame;
import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.helpers.AsynchronousTaskExecutor;
import lombok.extern.slf4j.Slf4j;

import static com.fluxtion.example.servicestarter.example1.Main.*;

/**
 * Runs the same example as {@link Main} but uses a pre-compiled {@link com.fluxtion.runtim.EventProcessor} to
 * drive the service manager
 */
@Slf4j
public class CompiledMain {
    public static void main(String[] args) {
        log.info("running compiled main");
        //uncomment this line to generate the ServiceManager aot
//        compileAot();
        ServiceManager svcManager = ServiceManager.fromProcessor(new Processor());
        svcManager.registerTaskExecutor(new AsynchronousTaskExecutor());
        svcManager.triggerDependentsOnNotification(true);
        svcManager.triggerNotificationOnSuccessfulTaskExecution(true);
        svcManager.registerStatusListener(new ServiceManagaerFrame(svcManager)::logStatus);
    }


    public static void compileAot() {
        Service orderGateway = Service.builder(ORDER_GATEWAY)
                .startTask(Main::emptyTask)
                .stopTask(Main::emptyTask)
                .build();
        //push order limits to pnlCheck
        Service limitReader = Service.builder(LIMIT_READER)
                .startTask(Main::emptyTask)
                .stopTask(Main::emptyTask)
                .build();
        //pushes market data to pnlCheck
        Service marketDataGateway = Service.builder(MARKET_DATA_GATEWAY)
                .startTask(Main::emptyTask)
                .stopTask(Main::emptyTask)
                .build();
        //carries out size and off market check on incoming orders - has a complex dependency relationship
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
        //riak manages new orders
        Service riskManager = Service.builder(RISK_MANAGER)
                .servicesThatRequireMe(orderProcessor)
                .stopTask(Main::emptyTask)
                .startTask(Main::emptyTask)
                .build();

        ServiceManager.compiledServiceManager(
                orderGateway,
                limitReader,
                marketDataGateway,
                pnlCheck,
                orderProcessor,
                internalOrderSource,
                orderAudit,
                riskManager
        );
    }
}
