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


        ServiceManager svcManager = ServiceManager.build(orderGateway, limitReader, marketDataGateway, pnlCheck);
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
