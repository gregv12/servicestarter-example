/*
 * Copyright (C) 2018 V12 Technology Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program.  If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package com.fluxtion.example.servicestarter.example1.aot;

import com.fluxtion.runtime.StaticEventProcessor;
import com.fluxtion.runtime.lifecycle.BatchHandler;
import com.fluxtion.runtime.lifecycle.Lifecycle;
import com.fluxtion.runtime.EventProcessor;

import com.fluxtion.example.servicestarter.example1.Main;
import com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterCommandProcessor;
import com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterStatusListener;
import com.fluxtion.example.servicestater.graph.ForwardPassServiceController;
import com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted;
import com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStartTask;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStatus;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStopTask;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestStartAll;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestStopAll;
import com.fluxtion.example.servicestater.graph.ReversePassServiceController;
import com.fluxtion.example.servicestater.graph.ServiceStatusRecordCache;
import com.fluxtion.example.servicestater.graph.TaskWrapperPublisher;
import com.fluxtion.runtime.audit.Auditor;
import com.fluxtion.runtime.audit.EventLogControlEvent;
import com.fluxtion.runtime.audit.EventLogManager;
import com.fluxtion.runtime.event.Event;
import com.fluxtion.runtime.time.Clock;
import com.fluxtion.runtime.time.ClockStrategy.ClockStrategyEvent;
import java.util.Arrays;
import java.util.HashMap;

/*
 * <pre>
 * generation time   : 2022-01-04T07:18:11.795694800
 * generator version : 3.1.4
 * api version       : 3.1.4
 * </pre>
 * @author Greg Higgins
 */
@SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
public class Processor implements EventProcessor, StaticEventProcessor, BatchHandler, Lifecycle {

  //Node declarations
  public final Clock clock = new Clock();
  private final TaskWrapperPublisher commandPublisher = new TaskWrapperPublisher();
  public final EventLogManager eventLogger = new EventLogManager();
  private final ServiceStatusRecordCache serviceStatusCache = new ServiceStatusRecordCache();
  private final ForwardPassServiceController internalOrderSource_start =
      new ForwardPassServiceController("internalOrderSource", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController limitReader_stop =
      new ReversePassServiceController("limitReader", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController marketDataGateway_stop =
      new ReversePassServiceController("marketDataGateway", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController orderAudit_stop =
      new ReversePassServiceController("orderAudit", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController orderGateway_start =
      new ForwardPassServiceController("orderGateway", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController pnlCheck_start =
      new ForwardPassServiceController("pnlCheck", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController limitReader_start =
      new ForwardPassServiceController("limitReader", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController marketDataGateway_start =
      new ForwardPassServiceController("marketDataGateway", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController orderProcessor_start =
      new ForwardPassServiceController("orderProcessor", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController orderAudit_start =
      new ForwardPassServiceController("orderAudit", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController riskManager_start =
      new ForwardPassServiceController("riskManager", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController riskManager_stop =
      new ReversePassServiceController("riskManager", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController orderProcessor_stop =
      new ReversePassServiceController("orderProcessor", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController internalOrderSource_stop =
      new ReversePassServiceController("internalOrderSource", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController pnlCheck_stop =
      new ReversePassServiceController("pnlCheck", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController orderGateway_stop =
      new ReversePassServiceController("orderGateway", commandPublisher, serviceStatusCache);
  //Dirty flags
  private boolean isDirty_clock = false;
  private boolean isDirty_internalOrderSource_start = false;
  private boolean isDirty_internalOrderSource_stop = false;
  private boolean isDirty_limitReader_start = false;
  private boolean isDirty_limitReader_stop = false;
  private boolean isDirty_marketDataGateway_start = false;
  private boolean isDirty_marketDataGateway_stop = false;
  private boolean isDirty_orderAudit_start = false;
  private boolean isDirty_orderAudit_stop = false;
  private boolean isDirty_orderGateway_start = false;
  private boolean isDirty_orderGateway_stop = false;
  private boolean isDirty_orderProcessor_start = false;
  private boolean isDirty_orderProcessor_stop = false;
  private boolean isDirty_pnlCheck_start = false;
  private boolean isDirty_pnlCheck_stop = false;
  private boolean isDirty_riskManager_start = false;
  private boolean isDirty_riskManager_stop = false;
  //Filter constants

  public Processor() {
    internalOrderSource_start.setDependents(Arrays.asList());
    internalOrderSource_start.setStartTask(Main::emptyTask);
    internalOrderSource_start.setStopTask(Main::emptyTask);
    limitReader_start.setDependents(Arrays.asList(pnlCheck_start));
    limitReader_start.setStartTask(Main::emptyTask);
    limitReader_start.setStopTask(Main::emptyTask);
    marketDataGateway_start.setDependents(Arrays.asList(pnlCheck_start));
    marketDataGateway_start.setStartTask(Main::emptyTask);
    marketDataGateway_start.setStopTask(Main::emptyTask);
    orderAudit_start.setDependents(Arrays.asList(orderProcessor_start));
    orderAudit_start.setStartTask(Main::emptyTask);
    orderAudit_start.setStopTask(Main::emptyTask);
    orderGateway_start.setDependents(Arrays.asList());
    orderGateway_start.setStartTask(Main::emptyTask);
    orderGateway_start.setStopTask(Main::emptyTask);
    orderProcessor_start.setDependents(Arrays.asList(pnlCheck_start, internalOrderSource_start));
    orderProcessor_start.setStartTask(Main::emptyTask);
    orderProcessor_start.setStopTask(Main::emptyTask);
    pnlCheck_start.setDependents(Arrays.asList(orderGateway_start));
    pnlCheck_start.setStartTask(Main::emptyTask);
    pnlCheck_start.setStopTask(Main::emptyTask);
    riskManager_start.setDependents(Arrays.asList(orderProcessor_start));
    riskManager_start.setStartTask(Main::emptyTask);
    riskManager_start.setStopTask(Main::emptyTask);
    internalOrderSource_stop.setDependents(Arrays.asList(orderProcessor_stop));
    internalOrderSource_stop.setStartTask(Main::emptyTask);
    internalOrderSource_stop.setStopTask(Main::emptyTask);
    limitReader_stop.setDependents(Arrays.asList());
    limitReader_stop.setStartTask(Main::emptyTask);
    limitReader_stop.setStopTask(Main::emptyTask);
    marketDataGateway_stop.setDependents(Arrays.asList());
    marketDataGateway_stop.setStartTask(Main::emptyTask);
    marketDataGateway_stop.setStopTask(Main::emptyTask);
    orderAudit_stop.setDependents(Arrays.asList());
    orderAudit_stop.setStartTask(Main::emptyTask);
    orderAudit_stop.setStopTask(Main::emptyTask);
    orderGateway_stop.setDependents(Arrays.asList(pnlCheck_stop));
    orderGateway_stop.setStartTask(Main::emptyTask);
    orderGateway_stop.setStopTask(Main::emptyTask);
    orderProcessor_stop.setDependents(Arrays.asList(orderAudit_stop, riskManager_stop));
    orderProcessor_stop.setStartTask(Main::emptyTask);
    orderProcessor_stop.setStopTask(Main::emptyTask);
    pnlCheck_stop.setDependents(
        Arrays.asList(limitReader_stop, marketDataGateway_stop, orderProcessor_stop));
    pnlCheck_stop.setStartTask(Main::emptyTask);
    pnlCheck_stop.setStopTask(Main::emptyTask);
    riskManager_stop.setDependents(Arrays.asList());
    riskManager_stop.setStartTask(Main::emptyTask);
    riskManager_stop.setStopTask(Main::emptyTask);
    eventLogger.trace = (boolean) true;
    eventLogger.printEventToString = (boolean) true;
    eventLogger.traceLevel = com.fluxtion.runtime.audit.EventLogControlEvent.LogLevel.INFO;
    eventLogger.clock = clock;
    //node auditors
    initialiseAuditor(eventLogger);
    initialiseAuditor(clock);
  }

  @Override
  public void onEvent(Object event) {
    switch (event.getClass().getName()) {
      case ("com.fluxtion.example.servicestater.graph.FluxtionServiceManager$RegisterCommandProcessor"):
        {
          RegisterCommandProcessor typedEvent = (RegisterCommandProcessor) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.FluxtionServiceManager$RegisterStatusListener"):
        {
          RegisterStatusListener typedEvent = (RegisterStatusListener) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$NotifyServiceStarted"):
        {
          NotifyServiceStarted typedEvent = (NotifyServiceStarted) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$NotifyServiceStopped"):
        {
          NotifyServiceStopped typedEvent = (NotifyServiceStopped) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$PublishStartTask"):
        {
          PublishStartTask typedEvent = (PublishStartTask) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$PublishStatus"):
        {
          PublishStatus typedEvent = (PublishStatus) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$PublishStopTask"):
        {
          PublishStopTask typedEvent = (PublishStopTask) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$RequestServiceStart"):
        {
          RequestServiceStart typedEvent = (RequestServiceStart) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$RequestServiceStop"):
        {
          RequestServiceStop typedEvent = (RequestServiceStop) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$RequestStartAll"):
        {
          RequestStartAll typedEvent = (RequestStartAll) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$RequestStopAll"):
        {
          RequestStopAll typedEvent = (RequestStopAll) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.runtim.audit.EventLogControlEvent"):
        {
          EventLogControlEvent typedEvent = (EventLogControlEvent) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.runtim.time.ClockStrategy$ClockStrategyEvent"):
        {
          ClockStrategyEvent typedEvent = (ClockStrategyEvent) event;
          handleEvent(typedEvent);
          break;
        }
    }
  }

  public void handleEvent(RegisterCommandProcessor typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(commandPublisher, "commandPublisher", "registerCommandProcessor", typedEvent);
    commandPublisher.registerCommandProcessor(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(RegisterStatusListener typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(serviceStatusCache, "serviceStatusCache", "registerStatusListener", typedEvent);
    serviceStatusCache.registerStatusListener(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(NotifyServiceStarted typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[internalOrderSource]
      case ("internalOrderSource"):
        auditInvocation(
            internalOrderSource_stop,
            "internalOrderSource_stop",
            "notifyServiceStarted",
            typedEvent);
        isDirty_internalOrderSource_stop =
            internalOrderSource_stop.notifyServiceStarted(typedEvent);
        if (isDirty_orderProcessor_stop) {
          auditInvocation(
              internalOrderSource_stop,
              "internalOrderSource_stop",
              "recalculateStatusForStop",
              typedEvent);
          isDirty_internalOrderSource_stop = internalOrderSource_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[limitReader]
      case ("limitReader"):
        auditInvocation(limitReader_stop, "limitReader_stop", "notifyServiceStarted", typedEvent);
        isDirty_limitReader_stop = limitReader_stop.notifyServiceStarted(typedEvent);
        auditInvocation(
            limitReader_stop, "limitReader_stop", "recalculateStatusForStop", typedEvent);
        isDirty_limitReader_stop = limitReader_stop.recalculateStatusForStop();
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[marketDataGateway]
      case ("marketDataGateway"):
        auditInvocation(
            marketDataGateway_stop, "marketDataGateway_stop", "notifyServiceStarted", typedEvent);
        isDirty_marketDataGateway_stop = marketDataGateway_stop.notifyServiceStarted(typedEvent);
        auditInvocation(
            marketDataGateway_stop,
            "marketDataGateway_stop",
            "recalculateStatusForStop",
            typedEvent);
        isDirty_marketDataGateway_stop = marketDataGateway_stop.recalculateStatusForStop();
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[orderAudit]
      case ("orderAudit"):
        auditInvocation(orderAudit_stop, "orderAudit_stop", "notifyServiceStarted", typedEvent);
        isDirty_orderAudit_stop = orderAudit_stop.notifyServiceStarted(typedEvent);
        auditInvocation(orderAudit_stop, "orderAudit_stop", "recalculateStatusForStop", typedEvent);
        isDirty_orderAudit_stop = orderAudit_stop.recalculateStatusForStop();
        if (isDirty_orderAudit_stop | isDirty_riskManager_stop) {
          auditInvocation(
              orderProcessor_stop, "orderProcessor_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderProcessor_stop = orderProcessor_stop.recalculateStatusForStop();
        }
        if (isDirty_orderProcessor_stop) {
          auditInvocation(
              internalOrderSource_stop,
              "internalOrderSource_stop",
              "recalculateStatusForStop",
              typedEvent);
          isDirty_internalOrderSource_stop = internalOrderSource_stop.recalculateStatusForStop();
        }
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[orderGateway]
      case ("orderGateway"):
        auditInvocation(orderGateway_stop, "orderGateway_stop", "notifyServiceStarted", typedEvent);
        isDirty_orderGateway_stop = orderGateway_stop.notifyServiceStarted(typedEvent);
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[orderProcessor]
      case ("orderProcessor"):
        auditInvocation(
            orderProcessor_stop, "orderProcessor_stop", "notifyServiceStarted", typedEvent);
        isDirty_orderProcessor_stop = orderProcessor_stop.notifyServiceStarted(typedEvent);
        if (isDirty_orderAudit_stop | isDirty_riskManager_stop) {
          auditInvocation(
              orderProcessor_stop, "orderProcessor_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderProcessor_stop = orderProcessor_stop.recalculateStatusForStop();
        }
        if (isDirty_orderProcessor_stop) {
          auditInvocation(
              internalOrderSource_stop,
              "internalOrderSource_stop",
              "recalculateStatusForStop",
              typedEvent);
          isDirty_internalOrderSource_stop = internalOrderSource_stop.recalculateStatusForStop();
        }
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[pnlCheck]
      case ("pnlCheck"):
        auditInvocation(pnlCheck_stop, "pnlCheck_stop", "notifyServiceStarted", typedEvent);
        isDirty_pnlCheck_stop = pnlCheck_stop.notifyServiceStarted(typedEvent);
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[riskManager]
      case ("riskManager"):
        auditInvocation(riskManager_stop, "riskManager_stop", "notifyServiceStarted", typedEvent);
        isDirty_riskManager_stop = riskManager_stop.notifyServiceStarted(typedEvent);
        auditInvocation(
            riskManager_stop, "riskManager_stop", "recalculateStatusForStop", typedEvent);
        isDirty_riskManager_stop = riskManager_stop.recalculateStatusForStop();
        if (isDirty_orderAudit_stop | isDirty_riskManager_stop) {
          auditInvocation(
              orderProcessor_stop, "orderProcessor_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderProcessor_stop = orderProcessor_stop.recalculateStatusForStop();
        }
        if (isDirty_orderProcessor_stop) {
          auditInvocation(
              internalOrderSource_stop,
              "internalOrderSource_stop",
              "recalculateStatusForStop",
              typedEvent);
          isDirty_internalOrderSource_stop = internalOrderSource_stop.recalculateStatusForStop();
        }
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(NotifyServiceStopped typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[internalOrderSource]
      case ("internalOrderSource"):
        auditInvocation(
            internalOrderSource_start,
            "internalOrderSource_start",
            "notifyServiceStopped",
            typedEvent);
        isDirty_internalOrderSource_start =
            internalOrderSource_start.notifyServiceStopped(typedEvent);
        auditInvocation(
            internalOrderSource_start,
            "internalOrderSource_start",
            "recalculateStatusForStart",
            typedEvent);
        isDirty_internalOrderSource_start = internalOrderSource_start.recalculateStatusForStart();
        if (isDirty_internalOrderSource_start | isDirty_pnlCheck_start) {
          auditInvocation(
              orderProcessor_start,
              "orderProcessor_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_orderProcessor_start = orderProcessor_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[limitReader]
      case ("limitReader"):
        auditInvocation(limitReader_start, "limitReader_start", "notifyServiceStopped", typedEvent);
        isDirty_limitReader_start = limitReader_start.notifyServiceStopped(typedEvent);
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              limitReader_start, "limitReader_start", "recalculateStatusForStart", typedEvent);
          isDirty_limitReader_start = limitReader_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[marketDataGateway]
      case ("marketDataGateway"):
        auditInvocation(
            marketDataGateway_start, "marketDataGateway_start", "notifyServiceStopped", typedEvent);
        isDirty_marketDataGateway_start = marketDataGateway_start.notifyServiceStopped(typedEvent);
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              marketDataGateway_start,
              "marketDataGateway_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_marketDataGateway_start = marketDataGateway_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[orderAudit]
      case ("orderAudit"):
        auditInvocation(orderAudit_start, "orderAudit_start", "notifyServiceStopped", typedEvent);
        isDirty_orderAudit_start = orderAudit_start.notifyServiceStopped(typedEvent);
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[orderGateway]
      case ("orderGateway"):
        auditInvocation(
            orderGateway_start, "orderGateway_start", "notifyServiceStopped", typedEvent);
        isDirty_orderGateway_start = orderGateway_start.notifyServiceStopped(typedEvent);
        auditInvocation(
            orderGateway_start, "orderGateway_start", "recalculateStatusForStart", typedEvent);
        isDirty_orderGateway_start = orderGateway_start.recalculateStatusForStart();
        if (isDirty_orderGateway_start) {
          auditInvocation(
              pnlCheck_start, "pnlCheck_start", "recalculateStatusForStart", typedEvent);
          isDirty_pnlCheck_start = pnlCheck_start.recalculateStatusForStart();
        }
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              limitReader_start, "limitReader_start", "recalculateStatusForStart", typedEvent);
          isDirty_limitReader_start = limitReader_start.recalculateStatusForStart();
        }
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              marketDataGateway_start,
              "marketDataGateway_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_marketDataGateway_start = marketDataGateway_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start | isDirty_pnlCheck_start) {
          auditInvocation(
              orderProcessor_start,
              "orderProcessor_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_orderProcessor_start = orderProcessor_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[orderProcessor]
      case ("orderProcessor"):
        auditInvocation(
            orderProcessor_start, "orderProcessor_start", "notifyServiceStopped", typedEvent);
        isDirty_orderProcessor_start = orderProcessor_start.notifyServiceStopped(typedEvent);
        if (isDirty_internalOrderSource_start | isDirty_pnlCheck_start) {
          auditInvocation(
              orderProcessor_start,
              "orderProcessor_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_orderProcessor_start = orderProcessor_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[pnlCheck]
      case ("pnlCheck"):
        auditInvocation(pnlCheck_start, "pnlCheck_start", "notifyServiceStopped", typedEvent);
        isDirty_pnlCheck_start = pnlCheck_start.notifyServiceStopped(typedEvent);
        if (isDirty_orderGateway_start) {
          auditInvocation(
              pnlCheck_start, "pnlCheck_start", "recalculateStatusForStart", typedEvent);
          isDirty_pnlCheck_start = pnlCheck_start.recalculateStatusForStart();
        }
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              limitReader_start, "limitReader_start", "recalculateStatusForStart", typedEvent);
          isDirty_limitReader_start = limitReader_start.recalculateStatusForStart();
        }
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              marketDataGateway_start,
              "marketDataGateway_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_marketDataGateway_start = marketDataGateway_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start | isDirty_pnlCheck_start) {
          auditInvocation(
              orderProcessor_start,
              "orderProcessor_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_orderProcessor_start = orderProcessor_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[riskManager]
      case ("riskManager"):
        auditInvocation(riskManager_start, "riskManager_start", "notifyServiceStopped", typedEvent);
        isDirty_riskManager_start = riskManager_start.notifyServiceStopped(typedEvent);
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(PublishStartTask typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(limitReader_stop, "limitReader_stop", "publishStartTasks", typedEvent);
    isDirty_limitReader_stop = true;
    limitReader_stop.publishStartTasks(typedEvent);
    auditInvocation(
        marketDataGateway_stop, "marketDataGateway_stop", "publishStartTasks", typedEvent);
    isDirty_marketDataGateway_stop = true;
    marketDataGateway_stop.publishStartTasks(typedEvent);
    auditInvocation(orderAudit_stop, "orderAudit_stop", "publishStartTasks", typedEvent);
    isDirty_orderAudit_stop = true;
    orderAudit_stop.publishStartTasks(typedEvent);
    auditInvocation(riskManager_stop, "riskManager_stop", "publishStartTasks", typedEvent);
    isDirty_riskManager_stop = true;
    riskManager_stop.publishStartTasks(typedEvent);
    auditInvocation(orderProcessor_stop, "orderProcessor_stop", "publishStartTasks", typedEvent);
    isDirty_orderProcessor_stop = true;
    orderProcessor_stop.publishStartTasks(typedEvent);
    auditInvocation(
        internalOrderSource_stop, "internalOrderSource_stop", "publishStartTasks", typedEvent);
    isDirty_internalOrderSource_stop = true;
    internalOrderSource_stop.publishStartTasks(typedEvent);
    auditInvocation(pnlCheck_stop, "pnlCheck_stop", "publishStartTasks", typedEvent);
    isDirty_pnlCheck_stop = true;
    pnlCheck_stop.publishStartTasks(typedEvent);
    auditInvocation(orderGateway_stop, "orderGateway_stop", "publishStartTasks", typedEvent);
    isDirty_orderGateway_stop = true;
    orderGateway_stop.publishStartTasks(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(PublishStatus typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(serviceStatusCache, "serviceStatusCache", "publishCurrentStatus", typedEvent);
    serviceStatusCache.publishCurrentStatus(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(PublishStopTask typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(
        internalOrderSource_start, "internalOrderSource_start", "publishStartTasks", typedEvent);
    isDirty_internalOrderSource_start = true;
    internalOrderSource_start.publishStartTasks(typedEvent);
    auditInvocation(orderGateway_start, "orderGateway_start", "publishStartTasks", typedEvent);
    isDirty_orderGateway_start = true;
    orderGateway_start.publishStartTasks(typedEvent);
    auditInvocation(pnlCheck_start, "pnlCheck_start", "publishStartTasks", typedEvent);
    isDirty_pnlCheck_start = true;
    pnlCheck_start.publishStartTasks(typedEvent);
    auditInvocation(limitReader_start, "limitReader_start", "publishStartTasks", typedEvent);
    isDirty_limitReader_start = true;
    limitReader_start.publishStartTasks(typedEvent);
    auditInvocation(
        marketDataGateway_start, "marketDataGateway_start", "publishStartTasks", typedEvent);
    isDirty_marketDataGateway_start = true;
    marketDataGateway_start.publishStartTasks(typedEvent);
    auditInvocation(orderProcessor_start, "orderProcessor_start", "publishStartTasks", typedEvent);
    isDirty_orderProcessor_start = true;
    orderProcessor_start.publishStartTasks(typedEvent);
    auditInvocation(orderAudit_start, "orderAudit_start", "publishStartTasks", typedEvent);
    isDirty_orderAudit_start = true;
    orderAudit_start.publishStartTasks(typedEvent);
    auditInvocation(riskManager_start, "riskManager_start", "publishStartTasks", typedEvent);
    isDirty_riskManager_start = true;
    riskManager_start.publishStartTasks(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(RequestServiceStart typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[internalOrderSource]
      case ("internalOrderSource"):
        auditInvocation(
            internalOrderSource_start, "internalOrderSource_start", "startThisService", typedEvent);
        isDirty_internalOrderSource_start = true;
        internalOrderSource_start.startThisService(typedEvent);
        auditInvocation(
            internalOrderSource_start,
            "internalOrderSource_start",
            "recalculateStatusForStart",
            typedEvent);
        isDirty_internalOrderSource_start = true;
        internalOrderSource_start.recalculateStatusForStart();
        if (isDirty_internalOrderSource_start | isDirty_pnlCheck_start) {
          auditInvocation(
              orderProcessor_start,
              "orderProcessor_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_orderProcessor_start = true;
          orderProcessor_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = true;
          orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = true;
          riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[limitReader]
      case ("limitReader"):
        auditInvocation(limitReader_start, "limitReader_start", "startThisService", typedEvent);
        isDirty_limitReader_start = true;
        limitReader_start.startThisService(typedEvent);
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              limitReader_start, "limitReader_start", "recalculateStatusForStart", typedEvent);
          isDirty_limitReader_start = true;
          limitReader_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[marketDataGateway]
      case ("marketDataGateway"):
        auditInvocation(
            marketDataGateway_start, "marketDataGateway_start", "startThisService", typedEvent);
        isDirty_marketDataGateway_start = true;
        marketDataGateway_start.startThisService(typedEvent);
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              marketDataGateway_start,
              "marketDataGateway_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_marketDataGateway_start = true;
          marketDataGateway_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[orderAudit]
      case ("orderAudit"):
        auditInvocation(orderAudit_start, "orderAudit_start", "startThisService", typedEvent);
        isDirty_orderAudit_start = true;
        orderAudit_start.startThisService(typedEvent);
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = true;
          orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[orderGateway]
      case ("orderGateway"):
        auditInvocation(orderGateway_start, "orderGateway_start", "startThisService", typedEvent);
        isDirty_orderGateway_start = true;
        orderGateway_start.startThisService(typedEvent);
        auditInvocation(
            orderGateway_start, "orderGateway_start", "recalculateStatusForStart", typedEvent);
        isDirty_orderGateway_start = true;
        orderGateway_start.recalculateStatusForStart();
        if (isDirty_orderGateway_start) {
          auditInvocation(
              pnlCheck_start, "pnlCheck_start", "recalculateStatusForStart", typedEvent);
          isDirty_pnlCheck_start = true;
          pnlCheck_start.recalculateStatusForStart();
        }
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              limitReader_start, "limitReader_start", "recalculateStatusForStart", typedEvent);
          isDirty_limitReader_start = true;
          limitReader_start.recalculateStatusForStart();
        }
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              marketDataGateway_start,
              "marketDataGateway_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_marketDataGateway_start = true;
          marketDataGateway_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start | isDirty_pnlCheck_start) {
          auditInvocation(
              orderProcessor_start,
              "orderProcessor_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_orderProcessor_start = true;
          orderProcessor_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = true;
          orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = true;
          riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[orderProcessor]
      case ("orderProcessor"):
        auditInvocation(
            orderProcessor_start, "orderProcessor_start", "startThisService", typedEvent);
        isDirty_orderProcessor_start = true;
        orderProcessor_start.startThisService(typedEvent);
        if (isDirty_internalOrderSource_start | isDirty_pnlCheck_start) {
          auditInvocation(
              orderProcessor_start,
              "orderProcessor_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_orderProcessor_start = true;
          orderProcessor_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = true;
          orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = true;
          riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[pnlCheck]
      case ("pnlCheck"):
        auditInvocation(pnlCheck_start, "pnlCheck_start", "startThisService", typedEvent);
        isDirty_pnlCheck_start = true;
        pnlCheck_start.startThisService(typedEvent);
        if (isDirty_orderGateway_start) {
          auditInvocation(
              pnlCheck_start, "pnlCheck_start", "recalculateStatusForStart", typedEvent);
          isDirty_pnlCheck_start = true;
          pnlCheck_start.recalculateStatusForStart();
        }
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              limitReader_start, "limitReader_start", "recalculateStatusForStart", typedEvent);
          isDirty_limitReader_start = true;
          limitReader_start.recalculateStatusForStart();
        }
        if (isDirty_pnlCheck_start) {
          auditInvocation(
              marketDataGateway_start,
              "marketDataGateway_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_marketDataGateway_start = true;
          marketDataGateway_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start | isDirty_pnlCheck_start) {
          auditInvocation(
              orderProcessor_start,
              "orderProcessor_start",
              "recalculateStatusForStart",
              typedEvent);
          isDirty_orderProcessor_start = true;
          orderProcessor_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
          isDirty_orderAudit_start = true;
          orderAudit_start.recalculateStatusForStart();
        }
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = true;
          riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[riskManager]
      case ("riskManager"):
        auditInvocation(riskManager_start, "riskManager_start", "startThisService", typedEvent);
        isDirty_riskManager_start = true;
        riskManager_start.startThisService(typedEvent);
        if (isDirty_orderProcessor_start) {
          auditInvocation(
              riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
          isDirty_riskManager_start = true;
          riskManager_start.recalculateStatusForStart();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(RequestServiceStop typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[internalOrderSource]
      case ("internalOrderSource"):
        auditInvocation(
            internalOrderSource_stop, "internalOrderSource_stop", "stopThisService", typedEvent);
        isDirty_internalOrderSource_stop = true;
        internalOrderSource_stop.stopThisService(typedEvent);
        if (isDirty_orderProcessor_stop) {
          auditInvocation(
              internalOrderSource_stop,
              "internalOrderSource_stop",
              "recalculateStatusForStop",
              typedEvent);
          isDirty_internalOrderSource_stop = true;
          internalOrderSource_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[limitReader]
      case ("limitReader"):
        auditInvocation(limitReader_stop, "limitReader_stop", "stopThisService", typedEvent);
        isDirty_limitReader_stop = true;
        limitReader_stop.stopThisService(typedEvent);
        auditInvocation(
            limitReader_stop, "limitReader_stop", "recalculateStatusForStop", typedEvent);
        isDirty_limitReader_stop = true;
        limitReader_stop.recalculateStatusForStop();
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = true;
          pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = true;
          orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[marketDataGateway]
      case ("marketDataGateway"):
        auditInvocation(
            marketDataGateway_stop, "marketDataGateway_stop", "stopThisService", typedEvent);
        isDirty_marketDataGateway_stop = true;
        marketDataGateway_stop.stopThisService(typedEvent);
        auditInvocation(
            marketDataGateway_stop,
            "marketDataGateway_stop",
            "recalculateStatusForStop",
            typedEvent);
        isDirty_marketDataGateway_stop = true;
        marketDataGateway_stop.recalculateStatusForStop();
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = true;
          pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = true;
          orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[orderAudit]
      case ("orderAudit"):
        auditInvocation(orderAudit_stop, "orderAudit_stop", "stopThisService", typedEvent);
        isDirty_orderAudit_stop = true;
        orderAudit_stop.stopThisService(typedEvent);
        auditInvocation(orderAudit_stop, "orderAudit_stop", "recalculateStatusForStop", typedEvent);
        isDirty_orderAudit_stop = true;
        orderAudit_stop.recalculateStatusForStop();
        if (isDirty_orderAudit_stop | isDirty_riskManager_stop) {
          auditInvocation(
              orderProcessor_stop, "orderProcessor_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderProcessor_stop = true;
          orderProcessor_stop.recalculateStatusForStop();
        }
        if (isDirty_orderProcessor_stop) {
          auditInvocation(
              internalOrderSource_stop,
              "internalOrderSource_stop",
              "recalculateStatusForStop",
              typedEvent);
          isDirty_internalOrderSource_stop = true;
          internalOrderSource_stop.recalculateStatusForStop();
        }
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = true;
          pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = true;
          orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[orderGateway]
      case ("orderGateway"):
        auditInvocation(orderGateway_stop, "orderGateway_stop", "stopThisService", typedEvent);
        isDirty_orderGateway_stop = true;
        orderGateway_stop.stopThisService(typedEvent);
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = true;
          orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[orderProcessor]
      case ("orderProcessor"):
        auditInvocation(orderProcessor_stop, "orderProcessor_stop", "stopThisService", typedEvent);
        isDirty_orderProcessor_stop = true;
        orderProcessor_stop.stopThisService(typedEvent);
        if (isDirty_orderAudit_stop | isDirty_riskManager_stop) {
          auditInvocation(
              orderProcessor_stop, "orderProcessor_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderProcessor_stop = true;
          orderProcessor_stop.recalculateStatusForStop();
        }
        if (isDirty_orderProcessor_stop) {
          auditInvocation(
              internalOrderSource_stop,
              "internalOrderSource_stop",
              "recalculateStatusForStop",
              typedEvent);
          isDirty_internalOrderSource_stop = true;
          internalOrderSource_stop.recalculateStatusForStop();
        }
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = true;
          pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = true;
          orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[pnlCheck]
      case ("pnlCheck"):
        auditInvocation(pnlCheck_stop, "pnlCheck_stop", "stopThisService", typedEvent);
        isDirty_pnlCheck_stop = true;
        pnlCheck_stop.stopThisService(typedEvent);
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = true;
          pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = true;
          orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[riskManager]
      case ("riskManager"):
        auditInvocation(riskManager_stop, "riskManager_stop", "stopThisService", typedEvent);
        isDirty_riskManager_stop = true;
        riskManager_stop.stopThisService(typedEvent);
        auditInvocation(
            riskManager_stop, "riskManager_stop", "recalculateStatusForStop", typedEvent);
        isDirty_riskManager_stop = true;
        riskManager_stop.recalculateStatusForStop();
        if (isDirty_orderAudit_stop | isDirty_riskManager_stop) {
          auditInvocation(
              orderProcessor_stop, "orderProcessor_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderProcessor_stop = true;
          orderProcessor_stop.recalculateStatusForStop();
        }
        if (isDirty_orderProcessor_stop) {
          auditInvocation(
              internalOrderSource_stop,
              "internalOrderSource_stop",
              "recalculateStatusForStop",
              typedEvent);
          isDirty_internalOrderSource_stop = true;
          internalOrderSource_stop.recalculateStatusForStop();
        }
        if (isDirty_limitReader_stop
            | isDirty_marketDataGateway_stop
            | isDirty_orderProcessor_stop) {
          auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
          isDirty_pnlCheck_stop = true;
          pnlCheck_stop.recalculateStatusForStop();
        }
        if (isDirty_pnlCheck_stop) {
          auditInvocation(
              orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
          isDirty_orderGateway_stop = true;
          orderGateway_stop.recalculateStatusForStop();
        }
        if (isDirty_internalOrderSource_start
            | isDirty_internalOrderSource_stop
            | isDirty_limitReader_start
            | isDirty_limitReader_stop
            | isDirty_marketDataGateway_start
            | isDirty_marketDataGateway_stop
            | isDirty_orderAudit_start
            | isDirty_orderAudit_stop
            | isDirty_orderGateway_start
            | isDirty_orderGateway_stop
            | isDirty_orderProcessor_start
            | isDirty_orderProcessor_stop
            | isDirty_pnlCheck_start
            | isDirty_pnlCheck_stop
            | isDirty_riskManager_start
            | isDirty_riskManager_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(RequestStartAll typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(
        internalOrderSource_start, "internalOrderSource_start", "startAllServices", typedEvent);
    isDirty_internalOrderSource_start = true;
    internalOrderSource_start.startAllServices(typedEvent);
    auditInvocation(
        internalOrderSource_start,
        "internalOrderSource_start",
        "recalculateStatusForStart",
        typedEvent);
    isDirty_internalOrderSource_start = true;
    internalOrderSource_start.recalculateStatusForStart();
    auditInvocation(orderGateway_start, "orderGateway_start", "startAllServices", typedEvent);
    isDirty_orderGateway_start = true;
    orderGateway_start.startAllServices(typedEvent);
    auditInvocation(
        orderGateway_start, "orderGateway_start", "recalculateStatusForStart", typedEvent);
    isDirty_orderGateway_start = true;
    orderGateway_start.recalculateStatusForStart();
    auditInvocation(pnlCheck_start, "pnlCheck_start", "startAllServices", typedEvent);
    isDirty_pnlCheck_start = true;
    pnlCheck_start.startAllServices(typedEvent);
    if (isDirty_orderGateway_start) {
      auditInvocation(pnlCheck_start, "pnlCheck_start", "recalculateStatusForStart", typedEvent);
      isDirty_pnlCheck_start = true;
      pnlCheck_start.recalculateStatusForStart();
    }
    auditInvocation(limitReader_start, "limitReader_start", "startAllServices", typedEvent);
    isDirty_limitReader_start = true;
    limitReader_start.startAllServices(typedEvent);
    if (isDirty_pnlCheck_start) {
      auditInvocation(
          limitReader_start, "limitReader_start", "recalculateStatusForStart", typedEvent);
      isDirty_limitReader_start = true;
      limitReader_start.recalculateStatusForStart();
    }
    auditInvocation(
        marketDataGateway_start, "marketDataGateway_start", "startAllServices", typedEvent);
    isDirty_marketDataGateway_start = true;
    marketDataGateway_start.startAllServices(typedEvent);
    if (isDirty_pnlCheck_start) {
      auditInvocation(
          marketDataGateway_start,
          "marketDataGateway_start",
          "recalculateStatusForStart",
          typedEvent);
      isDirty_marketDataGateway_start = true;
      marketDataGateway_start.recalculateStatusForStart();
    }
    auditInvocation(orderProcessor_start, "orderProcessor_start", "startAllServices", typedEvent);
    isDirty_orderProcessor_start = true;
    orderProcessor_start.startAllServices(typedEvent);
    if (isDirty_internalOrderSource_start | isDirty_pnlCheck_start) {
      auditInvocation(
          orderProcessor_start, "orderProcessor_start", "recalculateStatusForStart", typedEvent);
      isDirty_orderProcessor_start = true;
      orderProcessor_start.recalculateStatusForStart();
    }
    auditInvocation(orderAudit_start, "orderAudit_start", "startAllServices", typedEvent);
    isDirty_orderAudit_start = true;
    orderAudit_start.startAllServices(typedEvent);
    if (isDirty_orderProcessor_start) {
      auditInvocation(
          orderAudit_start, "orderAudit_start", "recalculateStatusForStart", typedEvent);
      isDirty_orderAudit_start = true;
      orderAudit_start.recalculateStatusForStart();
    }
    auditInvocation(riskManager_start, "riskManager_start", "startAllServices", typedEvent);
    isDirty_riskManager_start = true;
    riskManager_start.startAllServices(typedEvent);
    if (isDirty_orderProcessor_start) {
      auditInvocation(
          riskManager_start, "riskManager_start", "recalculateStatusForStart", typedEvent);
      isDirty_riskManager_start = true;
      riskManager_start.recalculateStatusForStart();
    }
    if (isDirty_internalOrderSource_start
        | isDirty_internalOrderSource_stop
        | isDirty_limitReader_start
        | isDirty_limitReader_stop
        | isDirty_marketDataGateway_start
        | isDirty_marketDataGateway_stop
        | isDirty_orderAudit_start
        | isDirty_orderAudit_stop
        | isDirty_orderGateway_start
        | isDirty_orderGateway_stop
        | isDirty_orderProcessor_start
        | isDirty_orderProcessor_stop
        | isDirty_pnlCheck_start
        | isDirty_pnlCheck_stop
        | isDirty_riskManager_start
        | isDirty_riskManager_stop) {
      auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
      serviceStatusCache.publishStatus();
    }
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(RequestStopAll typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(limitReader_stop, "limitReader_stop", "stopAllServices", typedEvent);
    isDirty_limitReader_stop = true;
    limitReader_stop.stopAllServices(typedEvent);
    auditInvocation(limitReader_stop, "limitReader_stop", "recalculateStatusForStop", typedEvent);
    isDirty_limitReader_stop = true;
    limitReader_stop.recalculateStatusForStop();
    auditInvocation(
        marketDataGateway_stop, "marketDataGateway_stop", "stopAllServices", typedEvent);
    isDirty_marketDataGateway_stop = true;
    marketDataGateway_stop.stopAllServices(typedEvent);
    auditInvocation(
        marketDataGateway_stop, "marketDataGateway_stop", "recalculateStatusForStop", typedEvent);
    isDirty_marketDataGateway_stop = true;
    marketDataGateway_stop.recalculateStatusForStop();
    auditInvocation(orderAudit_stop, "orderAudit_stop", "stopAllServices", typedEvent);
    isDirty_orderAudit_stop = true;
    orderAudit_stop.stopAllServices(typedEvent);
    auditInvocation(orderAudit_stop, "orderAudit_stop", "recalculateStatusForStop", typedEvent);
    isDirty_orderAudit_stop = true;
    orderAudit_stop.recalculateStatusForStop();
    auditInvocation(riskManager_stop, "riskManager_stop", "stopAllServices", typedEvent);
    isDirty_riskManager_stop = true;
    riskManager_stop.stopAllServices(typedEvent);
    auditInvocation(riskManager_stop, "riskManager_stop", "recalculateStatusForStop", typedEvent);
    isDirty_riskManager_stop = true;
    riskManager_stop.recalculateStatusForStop();
    auditInvocation(orderProcessor_stop, "orderProcessor_stop", "stopAllServices", typedEvent);
    isDirty_orderProcessor_stop = true;
    orderProcessor_stop.stopAllServices(typedEvent);
    if (isDirty_orderAudit_stop | isDirty_riskManager_stop) {
      auditInvocation(
          orderProcessor_stop, "orderProcessor_stop", "recalculateStatusForStop", typedEvent);
      isDirty_orderProcessor_stop = true;
      orderProcessor_stop.recalculateStatusForStop();
    }
    auditInvocation(
        internalOrderSource_stop, "internalOrderSource_stop", "stopAllServices", typedEvent);
    isDirty_internalOrderSource_stop = true;
    internalOrderSource_stop.stopAllServices(typedEvent);
    if (isDirty_orderProcessor_stop) {
      auditInvocation(
          internalOrderSource_stop,
          "internalOrderSource_stop",
          "recalculateStatusForStop",
          typedEvent);
      isDirty_internalOrderSource_stop = true;
      internalOrderSource_stop.recalculateStatusForStop();
    }
    auditInvocation(pnlCheck_stop, "pnlCheck_stop", "stopAllServices", typedEvent);
    isDirty_pnlCheck_stop = true;
    pnlCheck_stop.stopAllServices(typedEvent);
    if (isDirty_limitReader_stop | isDirty_marketDataGateway_stop | isDirty_orderProcessor_stop) {
      auditInvocation(pnlCheck_stop, "pnlCheck_stop", "recalculateStatusForStop", typedEvent);
      isDirty_pnlCheck_stop = true;
      pnlCheck_stop.recalculateStatusForStop();
    }
    auditInvocation(orderGateway_stop, "orderGateway_stop", "stopAllServices", typedEvent);
    isDirty_orderGateway_stop = true;
    orderGateway_stop.stopAllServices(typedEvent);
    if (isDirty_pnlCheck_stop) {
      auditInvocation(
          orderGateway_stop, "orderGateway_stop", "recalculateStatusForStop", typedEvent);
      isDirty_orderGateway_stop = true;
      orderGateway_stop.recalculateStatusForStop();
    }
    if (isDirty_internalOrderSource_start
        | isDirty_internalOrderSource_stop
        | isDirty_limitReader_start
        | isDirty_limitReader_stop
        | isDirty_marketDataGateway_start
        | isDirty_marketDataGateway_stop
        | isDirty_orderAudit_start
        | isDirty_orderAudit_stop
        | isDirty_orderGateway_start
        | isDirty_orderGateway_stop
        | isDirty_orderProcessor_start
        | isDirty_orderProcessor_stop
        | isDirty_pnlCheck_start
        | isDirty_pnlCheck_stop
        | isDirty_riskManager_start
        | isDirty_riskManager_stop) {
      auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
      serviceStatusCache.publishStatus();
    }
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(EventLogControlEvent typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(eventLogger, "eventLogger", "calculationLogConfig", typedEvent);
    eventLogger.calculationLogConfig(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(ClockStrategyEvent typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(clock, "clock", "setClockStrategy", typedEvent);
    isDirty_clock = true;
    clock.setClockStrategy(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  private void auditEvent(Object typedEvent) {
    eventLogger.eventReceived(typedEvent);
    clock.eventReceived(typedEvent);
  }

  private void auditEvent(Event typedEvent) {
    eventLogger.eventReceived(typedEvent);
    clock.eventReceived(typedEvent);
  }

  private void auditInvocation(Object node, String nodeName, String methodName, Object typedEvent) {
    eventLogger.nodeInvoked(node, nodeName, methodName, typedEvent);
  }

  private void initialiseAuditor(Auditor auditor) {
    auditor.init();
    auditor.nodeRegistered(internalOrderSource_start, "internalOrderSource_start");
    auditor.nodeRegistered(limitReader_start, "limitReader_start");
    auditor.nodeRegistered(marketDataGateway_start, "marketDataGateway_start");
    auditor.nodeRegistered(orderAudit_start, "orderAudit_start");
    auditor.nodeRegistered(orderGateway_start, "orderGateway_start");
    auditor.nodeRegistered(orderProcessor_start, "orderProcessor_start");
    auditor.nodeRegistered(pnlCheck_start, "pnlCheck_start");
    auditor.nodeRegistered(riskManager_start, "riskManager_start");
    auditor.nodeRegistered(internalOrderSource_stop, "internalOrderSource_stop");
    auditor.nodeRegistered(limitReader_stop, "limitReader_stop");
    auditor.nodeRegistered(marketDataGateway_stop, "marketDataGateway_stop");
    auditor.nodeRegistered(orderAudit_stop, "orderAudit_stop");
    auditor.nodeRegistered(orderGateway_stop, "orderGateway_stop");
    auditor.nodeRegistered(orderProcessor_stop, "orderProcessor_stop");
    auditor.nodeRegistered(pnlCheck_stop, "pnlCheck_stop");
    auditor.nodeRegistered(riskManager_stop, "riskManager_stop");
    auditor.nodeRegistered(serviceStatusCache, "serviceStatusCache");
    auditor.nodeRegistered(commandPublisher, "commandPublisher");
  }

  private void afterEvent() {
    commandPublisher.publishCommands();
    eventLogger.processingComplete();
    clock.processingComplete();
    isDirty_clock = false;
    isDirty_internalOrderSource_start = false;
    isDirty_internalOrderSource_stop = false;
    isDirty_limitReader_start = false;
    isDirty_limitReader_stop = false;
    isDirty_marketDataGateway_start = false;
    isDirty_marketDataGateway_stop = false;
    isDirty_orderAudit_start = false;
    isDirty_orderAudit_stop = false;
    isDirty_orderGateway_start = false;
    isDirty_orderGateway_stop = false;
    isDirty_orderProcessor_start = false;
    isDirty_orderProcessor_stop = false;
    isDirty_pnlCheck_start = false;
    isDirty_pnlCheck_stop = false;
    isDirty_riskManager_start = false;
    isDirty_riskManager_stop = false;
  }

  @Override
  public void init() {
    clock.init();
    serviceStatusCache.init();
    internalOrderSource_start.initialise();
    limitReader_stop.initialise();
    marketDataGateway_stop.initialise();
    orderAudit_stop.initialise();
    orderGateway_start.initialise();
    pnlCheck_start.initialise();
    limitReader_start.initialise();
    marketDataGateway_start.initialise();
    orderProcessor_start.initialise();
    orderAudit_start.initialise();
    riskManager_start.initialise();
    riskManager_stop.initialise();
    orderProcessor_stop.initialise();
    internalOrderSource_stop.initialise();
    pnlCheck_stop.initialise();
    orderGateway_stop.initialise();
  }

  @Override
  public void tearDown() {
    clock.tearDown();
    eventLogger.tearDown();
  }

  @Override
  public void batchPause() {}

  @Override
  public void batchEnd() {}
}
