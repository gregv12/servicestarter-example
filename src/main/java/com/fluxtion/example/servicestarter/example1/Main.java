package com.fluxtion.example.servicestarter.example1;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    public static void main(String[] args) {
        Service orderGateway = Service.builder("orderGateway")
                .startTask(Main::emptyTask)
                .build();
        Service limitReader = Service.builder("limitReader")
                .startTask(Main::emptyTask)
                .build();
        Service marketDataGateway = Service.builder("marketDataGateway")
                .startTask(Main::emptyTask)
                .build();
        Service pnlCheck = Service.builder("pnlCheck")
                .requiredServices(limitReader, marketDataGateway)
                .servicesThatRequireMe(orderGateway)
                .startTask(Main::emptyTask)
                .build();


        ServiceManager svcManager = ServiceManager.build(orderGateway, limitReader, marketDataGateway, pnlCheck);
        svcManager.triggerDependentsOnNotification(true);
        svcManager.triggerNotificationOnSuccessfulTaskExecution(true);
        svcManager.registerStatusListener(Main::logStatus);
        svcManager.startAllServices();
    }

    public static void logStatus(List<ServiceStatusRecord> statusUpdate){
        log.info("Current status:\n" +
                statusUpdate.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining("\n"))
        );
    }

    public static void emptyTask(){
    }
}
